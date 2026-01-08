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
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilRepository
import uk.gov.communities.prsdb.webapp.database.repository.NgdAddressLoaderRepository
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setIntOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setStringOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.ZipInputStreamExtensions.Companion.goToEntry
import java.sql.PreparedStatement
import java.time.Instant
import java.time.ZoneId
import java.util.zip.ZipInputStream

@PrsdbTaskService
class NgdAddressLoader(
    private val sessionFactory: SessionFactory,
    private val osDownloadsClient: OsDownloadsClient,
    localCouncilRepository: LocalCouncilRepository,
    environment: Environment,
) {
    private lateinit var ngdAddressLoaderRepository: NgdAddressLoaderRepository

    private val localCouncilCustodianCodeToId by lazy { localCouncilRepository.findAll().associate { it.custodianCode to it.id } }

    private val isLocalEnvironment by lazy { environment.activeProfiles.contains("local") }

    fun loadNewDataPackageVersions() {
        val statelessSession = sessionFactory.openStatelessSession()
        statelessSession.use { session ->
            ngdAddressLoaderRepository = NgdAddressLoaderRepository(session)

            val storedDataPackageVersionIdOrNull = getStoredDataPackageVersionId()
            log("Starting to load new data package versions. Version before load is $storedDataPackageVersionIdOrNull")

            var storedDataPackageVersionId = storedDataPackageVersionIdOrNull ?: loadInitialDataPackageVersion(session)
            do {
                val nextDataPackageVersionId = getNextDataPackageVersionId(storedDataPackageVersionId)
                if (nextDataPackageVersionId != null) {
                    loadDataPackageVersion(session, nextDataPackageVersionId)
                    storedDataPackageVersionId = nextDataPackageVersionId
                }
            } while (nextDataPackageVersionId != null)

            // TODO PRSD-1609: Handle inactive addresses that are still in use
            deleteUnusedInactiveAddresses(session)

            log("New data package versions loaded. Version after load is $storedDataPackageVersionId")
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
    ) {
        try {
            log("Starting to load data package version $dataPackageVersionId")

            val inputStream =
                osDownloadsClient.getDataPackageVersionFile(DATA_PACKAGE_ID, dataPackageVersionId, "$DATA_PACKAGE_FILE_NAME.zip")
            val zipInputStream = ZipInputStream(inputStream)

            zipInputStream.use { zip ->
                zip.goToEntry("$DATA_PACKAGE_FILE_NAME.csv")
                val entryReader = zip.bufferedReader()

                entryReader.use { fileReader ->
                    val csvParser = CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withNullString(""))
                    csvParser.use { parser -> loadCsvRecords(session, dataPackageVersionId, parser) }
                }
            }

            log("Data package version $dataPackageVersionId loaded")
        } catch (exception: Exception) {
            log("Error while loading data package version $dataPackageVersionId:")
            throw exception
        }
    }

    private fun getStoredDataPackageVersionId(): String? {
        val tableComment = ngdAddressLoaderRepository.findCommentOnAddressTable() ?: return null
        val dataPackageVersionId = tableComment.removePrefix(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
        return dataPackageVersionId.ifEmpty { null }
    }

    private fun setStoredDataPackageVersionId(dataPackageVersionId: String) {
        val comment = "$DATA_PACKAGE_VERSION_COMMENT_PREFIX$dataPackageVersionId"
        ngdAddressLoaderRepository.saveCommentOnAddressTable(comment)
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
    ) {
        val transaction = session.beginTransaction()
        try {
            session.doWork { connection ->
                ngdAddressLoaderRepository.getLoadAddressPreparedStatement(connection).use { preparedStatement ->
                    var batchRecordCount = 0
                    csvParser.forEachIndexed { index, record ->
                        val hasRecordBeenAdded = addCsvRecordToBatch(preparedStatement, record)
                        if (hasRecordBeenAdded) batchRecordCount++

                        if (batchRecordCount >= BATCH_SIZE) {
                            preparedStatement.executeBatch()
                            batchRecordCount = 0
                        }

                        if ((index + 1) % 100000 == 0) log("Loaded ${index + 1} records")
                    }
                    if (batchRecordCount > 0) preparedStatement.executeBatch()
                }
            }
            updatePropertyOwnershipAddresses()
            setStoredDataPackageVersionId(dataPackageVersionId)
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

    private fun updatePropertyOwnershipAddresses() {
        log("Starting to update property ownership addresses")
        ngdAddressLoaderRepository.updatePropertyOwnershipAddresses()
        log("Property ownership addresses updated")
    }

    private fun deleteUnusedInactiveAddresses(session: StatelessSession) {
        val transaction = session.beginTransaction()
        try {
            log("Starting to delete unused inactive addresses")
            ngdAddressLoaderRepository.deleteUnusedInactiveAddresses()
            transaction.commit()
            log("Unused inactive addresses deleted")
        } catch (exception: Exception) {
            log("Error while deleting unused inactive addresses:")
            transaction.rollback()
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

        private val deleteChangeTypes =
            listOf("End Of Life", "Moved To A Different Feature Type")
        private val upsertChangeTypes =
            listOf("New", "Moved From A Different Feature Type", "Modified Attributes", "Modified Geometry And Attributes")
        private val noChangeTypes = listOf("Modified Geometry")

        private val validCountries = listOf("England", "Wales", "Unassigned")
    }
}
