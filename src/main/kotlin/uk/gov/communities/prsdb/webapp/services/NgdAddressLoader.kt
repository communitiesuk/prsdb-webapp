package uk.gov.communities.prsdb.webapp.services

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.core.env.Environment
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.database.dao.NgdAddressLoaderDao
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilRepository
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setIntOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setStringOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.ZipInputStreamExtensions.Companion.goToEntry
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.time.Instant
import java.time.ZoneId
import java.util.zip.ZipInputStream

data class DataPackageProgress(
    val dataPackageVersionId: String,
    val rowOffset: Int,
    val isComplete: Boolean = true,
)

@PrsdbTaskService
class NgdAddressLoader(
    private val sessionFactory: SessionFactory,
    private val osDownloadsClient: OsDownloadsClient,
    localCouncilRepository: LocalCouncilRepository,
    environment: Environment,
) {
    private lateinit var ngdAddressLoaderDao: NgdAddressLoaderDao

    private val localCouncilCustodianCodeToId by lazy { localCouncilRepository.findAll().associate { it.custodianCode to it.id } }

    private val isLocalEnvironment by lazy { environment.activeProfiles.contains("local") }

    fun loadNewDataPackageVersions() {
        val statelessSession = sessionFactory.openStatelessSession()
        statelessSession.use { session ->
            ngdAddressLoaderDao = NgdAddressLoaderDao(session)

            val storedProgress = getStoredProgress()
            if (storedProgress != null) {
                log(
                    "Starting to load new data package versions. Current progress: " +
                        "version=${storedProgress.dataPackageVersionId}, " +
                        "rowOffset=${storedProgress.rowOffset}, complete=${storedProgress.isComplete}",
                )
            } else {
                log("Starting to load new data package versions. No previous progress found.")
            }

            var currentVersionId: String

            if (storedProgress != null && !storedProgress.isComplete) {
                log("Resuming in-progress version ${storedProgress.dataPackageVersionId} from row offset ${storedProgress.rowOffset}")
                loadDataPackageVersion(session, storedProgress.dataPackageVersionId, storedProgress.rowOffset)
                currentVersionId = storedProgress.dataPackageVersionId
            } else {
                currentVersionId = storedProgress?.dataPackageVersionId ?: loadInitialDataPackageVersion(session)
            }

            do {
                val nextDataPackageVersionId = getNextDataPackageVersionId(currentVersionId)
                if (nextDataPackageVersionId != null) {
                    loadDataPackageVersion(session, nextDataPackageVersionId)
                    currentVersionId = nextDataPackageVersionId
                }
            } while (nextDataPackageVersionId != null)

            // TODO PRSD-1609: Handle inactive addresses that are still in use
            deleteUnusedInactiveAddresses(session)

            log("New data package versions loaded. Version after load is $currentVersionId")
        }
    }

    private fun loadInitialDataPackageVersion(session: StatelessSession): String {
        val initialDataPackageVersionId = getInitialDataPackageVersionId()
        loadDataPackageVersion(session, initialDataPackageVersionId)
        return initialDataPackageVersionId
    }

    private fun loadDataPackageVersion(
        session: StatelessSession,
        dataPackageVersionId: String,
        initialRowOffset: Int = 0,
    ) {
        if (initialRowOffset == 0) {
            val markerTransaction = session.beginTransaction()
            try {
                saveProgress(DataPackageProgress(dataPackageVersionId, 0, isComplete = false))
                markerTransaction.commit()
            } catch (exception: Exception) {
                markerTransaction.rollback()
                throw exception
            }
        }

        var rowOffset = initialRowOffset
        var retriesRemaining = MAX_STREAM_RETRIES

        while (true) {
            try {
                log("Starting to load data package version $dataPackageVersionId from row offset $rowOffset")

                val inputStream =
                    osDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, dataPackageVersionId, "$DATA_PACKAGE_FILE_NAME.zip")
                val zipInputStream = ZipInputStream(inputStream)

                zipInputStream.use { zip ->
                    zip.goToEntry("$DATA_PACKAGE_FILE_NAME.csv")
                    val entryReader = zip.bufferedReader()

                    entryReader.use { fileReader ->
                        val csvParser = CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withNullString(""))
                        csvParser.use { parser ->
                            if (rowOffset > 0) {
                                log("Skipping $rowOffset previously processed rows")
                                val iterator = parser.iterator()
                                repeat(rowOffset) {
                                    if (iterator.hasNext()) iterator.next()
                                }
                            }
                            loadCsvRecords(session, dataPackageVersionId, parser, rowOffset)
                        }
                    }
                }

                log("Data package version $dataPackageVersionId loaded")
                return
            } catch (exception: Exception) {
                if (!isRetryableStreamException(exception)) {
                    log("Error while loading data package version $dataPackageVersionId:")
                    throw exception
                }
                val progress = getStoredProgress()
                if (progress?.isComplete == true && progress.dataPackageVersionId == dataPackageVersionId) {
                    log("Data package version $dataPackageVersionId completed before stream error, skipping retry")
                    return
                }
                rowOffset = progress?.rowOffset ?: rowOffset
                if (retriesRemaining <= 0) {
                    log("Error while loading data package version $dataPackageVersionId (retries exhausted):")
                    throw exception
                }
                retriesRemaining--
                log(
                    "Stream error while loading data package version $dataPackageVersionId " +
                        "at row offset $rowOffset. Retries remaining: $retriesRemaining. Reopening stream.",
                )
            }
        }
    }

    private fun getStoredProgress(): DataPackageProgress? {
        val tableComment = ngdAddressLoaderDao.findCommentOnAddressTable()
        return parseDataPackageProgress(tableComment)
    }

    private fun saveProgress(progress: DataPackageProgress) {
        ngdAddressLoaderDao.saveCommentOnAddressTable(formatProgressComment(progress))
    }

    private fun getInitialDataPackageVersionId(): String {
        try {
            val versionHistoryData = osDownloadsClient.getDataPackageVersionHistory(DATA_PACKAGE_ID)
            val versionHistory = JSONArray(versionHistoryData)

            val initialVersion = versionHistory.single { (it as JSONObject).optString("reason") == "INITIAL" }
            val initialVersionId = (initialVersion as JSONObject).getString("id")
            return initialVersionId
        } catch (exception: Exception) {
            log("Error while getting initial data package version:")
            throw exception
        }
    }

    private fun getNextDataPackageVersionId(currentDataPackageVersionId: String): String? {
        try {
            val versionDetailsData = osDownloadsClient.getDataPackageVersionDetails(DATA_PACKAGE_ID, currentDataPackageVersionId)
            val versionDetails = JSONObject(versionDetailsData)

            val nextVersionUrl = versionDetails.optString("nextVersionUrl") ?: return null
            val nextVersionId = nextVersionUrl.substringAfterLast("/", missingDelimiterValue = "")
            return nextVersionId.ifEmpty { null }
        } catch (exception: Exception) {
            log("Error while getting data package version after $currentDataPackageVersionId:")
            throw exception
        }
    }

    private fun loadCsvRecords(
        session: StatelessSession,
        dataPackageVersionId: String,
        csvParser: CSVParser,
        startRowOffset: Int = 0,
    ) {
        // var is captured by the doWork closure; safe because doWork executes synchronously
        // and StatelessSession transactions share the underlying JDBC connection
        var transaction = session.beginTransaction()
        try {
            session.doWork { connection: Connection ->
                ngdAddressLoaderDao.getLoadAddressPreparedStatement(connection).use { preparedStatement ->
                    var batchRecordCount = 0
                    val upsertedAddressUprns = mutableSetOf<Long>()

                    csvParser.forEachIndexed { index, record ->
                        val hasRecordBeenAdded = addCsvRecordToBatch(preparedStatement, record)
                        if (hasRecordBeenAdded) {
                            batchRecordCount++
                            val uprn = record.get("uprn").toLong()
                            upsertedAddressUprns.add(uprn)
                        }

                        if (batchRecordCount >= BATCH_SIZE) {
                            preparedStatement.executeBatch()
                            batchRecordCount = 0
                        }

                        if (upsertedAddressUprns.size >= UPRN_BATCH_SIZE) {
                            if (batchRecordCount > 0) {
                                preparedStatement.executeBatch()
                                batchRecordCount = 0
                            }

                            ngdAddressLoaderDao.updatePropertyOwnershipAddresses(upsertedAddressUprns)
                            upsertedAddressUprns.clear()
                            // Row offset saved within the same transaction as the data, ensuring atomic progress tracking
                            val committedRowOffset = startRowOffset + index + 1
                            saveProgress(DataPackageProgress(dataPackageVersionId, committedRowOffset, isComplete = false))
                            transaction.commit()
                            transaction = session.beginTransaction()
                        }

                        if ((startRowOffset + index + 1) % 100000 == 0) log("Loaded ${startRowOffset + index + 1} records")
                    }
                    if (batchRecordCount > 0) preparedStatement.executeBatch()
                    if (upsertedAddressUprns.isNotEmpty()) ngdAddressLoaderDao.updatePropertyOwnershipAddresses(upsertedAddressUprns)
                }
            }
            saveProgress(DataPackageProgress(dataPackageVersionId, 0, isComplete = true))
            transaction.commit()
        } catch (exception: Exception) {
            transaction.rollback()
            throw exception
        }
    }

    private fun addCsvRecordToBatch(
        preparedStatement: PreparedStatement,
        csvRecord: CSVRecord,
    ): Boolean {
        val changeType = csvRecord.get("changetype")
        val country = csvRecord.get("country")
        val isAddressActive =
            if (changeType in deleteChangeTypes || country !in validCountries) {
                false
            } else if (changeType in upsertChangeTypes) {
                true
            } else if (changeType in noChangeTypes) {
                // Skip record
                return false
            } else {
                throw IllegalArgumentException("Unknown change type '$changeType'")
            }

        val custodianCode = csvRecord.get("localcustodiancode")
        val localCouncilId =
            // We only keep English LC records
            // The custodian code 7655 is for address records maintained by Ordnance Survey rather than an LC
            if (country != "England" || custodianCode == "7655") {
                null
            } else {
                localCouncilCustodianCodeToId[custodianCode]
                // TODO PRSD-1643: Handle addresses in England with non-English custodian codes
                // ?: throw EntityNotFoundException("No local council with custodian code $custodianCode found")
            }

        preparedStatement.setLong(1, csvRecord.get("uprn").toLong())
        preparedStatement.setString(2, csvRecord.get("fulladdress"))
        preparedStatement.setStringOrNull(3, csvRecord.get("organisationname"))
        preparedStatement.setStringOrNull(4, csvRecord.get("subname"))
        preparedStatement.setStringOrNull(5, csvRecord.get("name"))
        preparedStatement.setStringOrNull(6, csvRecord.get("number"))
        preparedStatement.setString(7, csvRecord.get("streetname"))
        preparedStatement.setStringOrNull(8, csvRecord.get("locality"))
        preparedStatement.setStringOrNull(9, csvRecord.get("townname"))
        preparedStatement.setString(10, csvRecord.get("postcode"))
        preparedStatement.setIntOrNull(11, localCouncilId)
        preparedStatement.setBoolean(12, isAddressActive)

        preparedStatement.addBatch()
        return true
    }

    private fun deleteUnusedInactiveAddresses(session: StatelessSession) {
        val transaction = session.beginTransaction()
        try {
            log("Starting to delete unused inactive addresses")
            ngdAddressLoaderDao.deleteUnusedInactiveAddresses()
            transaction.commit()
            log("Unused inactive addresses deleted")
        } catch (exception: Exception) {
            transaction.rollback()
            log("Error while deleting unused inactive addresses:")
            throw exception
        }
    }

    private fun log(message: String) {
        val messagePrefix = if (isLocalEnvironment) "${Instant.now().atZone(ZoneId.systemDefault())} " else ""
        println("$messagePrefix$message")
    }

    companion object {
        const val DATA_PACKAGE_VERSION_COMMENT_PREFIX = "dataPackageVersionId="

        const val DATA_PACKAGE_ID = "15298"
        const val DATA_PACKAGE_FILE_NAME = "add_gb_builtaddress"

        const val BATCH_SIZE = 5000
        const val UPRN_BATCH_SIZE = 20000
        const val MAX_STREAM_RETRIES = 3

        private val deleteChangeTypes =
            listOf("End Of Life", "Moved To A Different Feature Type")
        private val upsertChangeTypes =
            listOf("New", "Moved From A Different Feature Type", "Modified Attributes", "Modified Geometry And Attributes")
        private val noChangeTypes = listOf("Modified Geometry")

        private val validCountries = listOf("England", "Wales", "Unassigned")

        fun parseDataPackageProgress(comment: String?): DataPackageProgress? {
            if (comment == null) return null
            val versionId = comment.removePrefix(DATA_PACKAGE_VERSION_COMMENT_PREFIX).substringBefore(";")
            if (versionId.isEmpty()) return null
            val rowOffsetMatch = Regex("""rowOffset=(\d+)""").find(comment)
            val rowOffset = rowOffsetMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val isComplete = rowOffsetMatch == null
            return DataPackageProgress(versionId, rowOffset, isComplete)
        }

        fun formatProgressComment(progress: DataPackageProgress): String {
            val base = "$DATA_PACKAGE_VERSION_COMMENT_PREFIX${progress.dataPackageVersionId}"
            return if (progress.isComplete) base else "$base;rowOffset=${progress.rowOffset}"
        }

        fun isRetryableStreamException(exception: Exception): Boolean =
            exception is IOException ||
                (exception is IllegalStateException && exception.cause is IOException)
    }
}
