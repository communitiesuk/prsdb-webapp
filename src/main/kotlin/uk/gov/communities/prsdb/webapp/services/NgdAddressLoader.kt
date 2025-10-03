package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository
import uk.gov.communities.prsdb.webapp.database.repository.NgdAddressLoaderRepository
import uk.gov.communities.prsdb.webapp.helpers.extensions.ZipInputStreamExtensions.Companion.goToEntry
import java.time.Instant
import java.time.ZoneId
import java.util.zip.ZipInputStream

// TODO PRSD-1021: Change annotation to PrsdbProcessService when ExampleOsDownloadsController is deleted
@Service
class NgdAddressLoader(
    private val sessionFactory: SessionFactory,
    private val osDownloadsClient: OsDownloadsClient,
    localAuthorityRepository: LocalAuthorityRepository,
) {
    private val localAuthorityByCustodianCode = localAuthorityRepository.findAll().associateBy { it.custodianCode }

    private lateinit var ngdAddressLoaderRepository: NgdAddressLoaderRepository

    fun loadNewDataPackageVersions() {
        val statelessSession = sessionFactory.openStatelessSession()
        statelessSession.use { session ->
            ngdAddressLoaderRepository = NgdAddressLoaderRepository(session)

            var storedDataPackageVersionId = getStoredDataPackageVersionId() ?: loadInitialDataPackageVersion(session)
            do {
                val nextDataPackageVersionId = getNextDataPackageVersionId(storedDataPackageVersionId)
                if (nextDataPackageVersionId != null) {
                    loadDataPackageVersion(session, nextDataPackageVersionId)
                    storedDataPackageVersionId = nextDataPackageVersionId
                }
            } while (nextDataPackageVersionId != null)

            deleteUnusedInactiveAddresses(session)
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
            csvParser.forEachIndexed { index, record ->
                loadCsvRecord(session, record)
                if ((index + 1) % 1000 == 0) log("Loaded ${index + 1} records")
            }
            setStoredDataPackageVersionId(dataPackageVersionId)
            transaction.commit()
        } catch (exception: Exception) {
            transaction.rollback()
            throw exception
        }
    }

    private fun loadCsvRecord(
        session: StatelessSession,
        csvRecord: CSVRecord,
    ) {
        val changeType = csvRecord.get("changetype")
        val country = csvRecord.get("country")

        if (changeType in deleteChangeTypes || country !in validCountries) {
            loadCsvDeleteRecord(csvRecord)
        } else if (changeType in upsertChangeTypes) {
            loadCsvUpsertRecord(session, csvRecord)
        } else if (changeType in noChangeTypes) {
            return
        } else {
            throw IllegalArgumentException("Unknown change type '$changeType'")
        }
    }

    private fun loadCsvDeleteRecord(csvRecord: CSVRecord) {
        val uprn = csvRecord.get("uprn").toLong()
        ngdAddressLoaderRepository.deactivateAddress(uprn)
    }

    private fun loadCsvUpsertRecord(
        session: StatelessSession,
        csvRecord: CSVRecord,
    ) {
        val uprn = csvRecord.get("uprn").toLong()
        val addressToUpdateId = ngdAddressLoaderRepository.findAddressId(uprn)

        val country = csvRecord.get("country")
        val custodianCode = csvRecord.get("localcustodiancode")
        val localAuthority =
            // We only keep English LA records
            // The custodian code 7655 is for address records maintained by Ordnance Survey rather than an LA
            if (country != "England" || custodianCode == "7655") {
                null
            } else {
                localAuthorityByCustodianCode[custodianCode]
                    ?: throw EntityNotFoundException("No local authority with custodian code $custodianCode found")
            }

        val upsertAddress =
            Address(
                id = addressToUpdateId,
                uprn = uprn,
                singleLineAddress = csvRecord.get("fulladdress"),
                organisation = csvRecord.get("organisationname"),
                subBuilding = csvRecord.get("subname"),
                buildingName = csvRecord.get("name"),
                buildingNumber = csvRecord.get("number"),
                streetName = csvRecord.get("streetname"),
                locality = csvRecord.get("locality"),
                townName = csvRecord.get("townname"),
                postcode = csvRecord.get("postcode"),
                localAuthority = localAuthority,
            )

        if (addressToUpdateId == null) {
            session.insert(upsertAddress)
        } else {
            session.update(upsertAddress)
        }
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
        println("${Instant.now().atZone(ZoneId.systemDefault())} $message")
    }

    companion object {
        const val DATA_PACKAGE_VERSION_COMMENT_PREFIX = "dataPackageVersionId="

        const val DATA_PACKAGE_ID = "15298"
        const val DATA_PACKAGE_FILE_NAME = "add_gb_builtaddress"

        private val deleteChangeTypes =
            listOf("End Of Life", "Moved To A Different Feature Type")
        private val upsertChangeTypes =
            listOf("New", "Moved From A Different Feature Type", "Modified Attributes", "Modified Geometry And Attributes")
        private val noChangeTypes = listOf("Modified Geometry")

        private val validCountries = listOf("England", "Wales", "Unassigned")
    }
}
