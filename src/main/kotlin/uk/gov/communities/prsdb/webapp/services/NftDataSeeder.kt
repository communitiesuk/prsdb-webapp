package uk.gov.communities.prsdb.webapp.services

import org.hibernate.SessionFactory
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.dao.NftDataSeederDao
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilRepository
import uk.gov.communities.prsdb.webapp.helpers.NftDataFaker
import uk.gov.communities.prsdb.webapp.helpers.NftDataFaker.CoreLandlordDetails
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setBigDecimalOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setBooleanOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setDateOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setIntOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setLongOrNull
import uk.gov.communities.prsdb.webapp.helpers.extensions.PreparedStatementExtensions.Companion.setStringOrNull
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.math.ceil
import kotlin.math.min

@PrsdbTaskService
class NftDataSeeder(
    private val sessionFactory: SessionFactory,
    private val localCouncilRepository: LocalCouncilRepository,
    private val addressRepository: AddressRepository,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) {
    private lateinit var nftDataSeederDao: NftDataSeederDao

    private val registrationNumberGenerator = RegistrationNumberGenerator()
    private val addressGenerator = AddressGenerator()
    private val availableAddressGenerator = AddressGenerator(restrictToAvailable = true)

    fun seedDatabase() {
        val statelessSession = sessionFactory.openStatelessSession()
        statelessSession.use { session ->
            val transaction = session.beginTransaction()
            try {
                session.doWork { connection: Connection ->
                    nftDataSeederDao = NftDataSeederDao(session, connection)
                    seedSystemOperatorData()
                    seedLocalCouncilData()
                    seedLandlordData()
                    nftDataSeederDao.updateIdSequences()
                }
                transaction.commit()
            } catch (e: Exception) {
                transaction.rollback()
                throw e
            }
        }
    }

    private fun seedSystemOperatorData() {
        log("Starting to seed system operator data")

        val oneLoginUserStmt = nftDataSeederDao.prepareOneLoginUserStatement()
        val systemOperatorStmt = nftDataSeederDao.prepareSystemOperatorStatement()

        try {
            repeat(NUM_OF_SYSTEM_OPERATORS) { addSystemOperatorToBatch(oneLoginUserStmt, systemOperatorStmt) }

            oneLoginUserStmt.executeBatch()
            systemOperatorStmt.executeBatch()

            log("Seeded $NUM_OF_SYSTEM_OPERATORS system operators")
        } finally {
            oneLoginUserStmt.close()
            systemOperatorStmt.close()
        }

        log("Finished seeding system operator data")
    }

    private fun seedLocalCouncilData() {
        log("Starting to seed local council data")

        val oneLoginUserStmt = nftDataSeederDao.prepareOneLoginUserStatement()
        val localCouncilUserStmt = nftDataSeederDao.prepareLocalCouncilUserStatement()
        val localCouncilInvitationStmt = nftDataSeederDao.prepareLocalCouncilInvitationStatement()

        try {
            val localCouncilIds = localCouncilRepository.findAllId()

            repeat(NUM_OF_LC_USERS) {
                val isManager = NftDataFaker.generateBoolean(probabilityTrue = 0.2)
                val localCouncilId = localCouncilIds.random()

                val hasUserRegistered = NftDataFaker.generateBoolean(probabilityTrue = 0.75)
                if (hasUserRegistered) {
                    addLcUserToBatch(oneLoginUserStmt, localCouncilUserStmt, isManager, localCouncilId)
                } else {
                    addLcInvitationToBatch(localCouncilInvitationStmt, isManager, localCouncilId)
                }
            }

            oneLoginUserStmt.executeBatch()
            localCouncilUserStmt.executeBatch()
            localCouncilInvitationStmt.executeBatch()

            log("Seeded $NUM_OF_LC_USERS local council users/invitations")
        } finally {
            oneLoginUserStmt.close()
            localCouncilUserStmt.close()
            localCouncilInvitationStmt.close()
        }

        log("Finished seeding local council data")
    }

    private fun seedLandlordData() {
        log("Starting to seed landlord data")

        val oneLoginUserStmt = nftDataSeederDao.prepareOneLoginUserStatement()
        val registrationNumberStmt = nftDataSeederDao.prepareRegistrationNumberStatement()
        val landlordStmt = nftDataSeederDao.prepareLandlordStatement()

        val licenceStmt = nftDataSeederDao.prepareLicenceStatement()
        val propertyOwnershipStmt = nftDataSeederDao.preparePropertyOwnershipStatement()

        val fileUploadStmt = nftDataSeederDao.prepareFileUploadStatement()
        val certificateUploadStmt = nftDataSeederDao.prepareCertificateUploadStatement()
        val propertyComplianceStmt = nftDataSeederDao.preparePropertyComplianceStatement()

        val reminderEmailSentStmt = nftDataSeederDao.prepareReminderEmailSentStatement()
        val savedJourneyStateStmt = nftDataSeederDao.prepareSavedJourneyStateStatement()
        val incompletePropertyStmt = nftDataSeederDao.prepareLandlordIncompletePropertyStatement()

        try {
            var registrationNumbersAdded = 0

            var licencesAdded = 0
            var propertyOwnershipsAdded = 0

            var fileUploadsAdded = 0

            var reminderEmailsAdded = 0
            var incompletePropertiesAdded = 0

            fun propertyRegistrationsAdded() = propertyOwnershipsAdded + incompletePropertiesAdded

            val numOfLandlordBatches = ceil(NUM_OF_LANDLORDS.toFloat() / BATCH_SIZE).toInt()
            for (landlordBatchNum in 1..numOfLandlordBatches) {
                val landlordIdRange = ((landlordBatchNum - 1) * BATCH_SIZE + 1)..min(landlordBatchNum * BATCH_SIZE, NUM_OF_LANDLORDS)
                val coreDetailsForLandlords = NftDataFaker.generateCoreDetailsForLandlords(landlordIdRange.toList())

                coreDetailsForLandlords.forEach {
                    addLandlordToBatch(
                        oneLoginUserStmt,
                        registrationNumberStmt,
                        landlordStmt,
                        it,
                        registrationNumberId = (++registrationNumbersAdded).toLong(),
                    )
                }

                oneLoginUserStmt.executeBatch()
                registrationNumberStmt.executeBatch()
                landlordStmt.executeBatch()
                registrationNumberGenerator.forgetUsedValues()
                addressGenerator.forgetUsedValues()

                log("Seeded ${landlordIdRange.last} landlords")

                coreDetailsForLandlords.forEach { landlord ->
                    val numOfPropertiesLeft = NUM_OF_PROPERTIES - propertyRegistrationsAdded()
                    val numOfPropertiesForLandlord = NftDataFaker.generateNumberOfPropertiesForLandlord().coerceAtMost(numOfPropertiesLeft)

                    repeat(numOfPropertiesForLandlord) {
                        val isRegistrationComplete = NftDataFaker.generateBoolean(probabilityTrue = 0.9)
                        if (isRegistrationComplete) {
                            val isOccupied = NftDataFaker.generateBoolean(probabilityTrue = 0.8)
                            val hasLicence = NftDataFaker.generateBoolean(probabilityTrue = 0.2)
                            val propertyOwnershipId = (++propertyOwnershipsAdded).toLong()

                            val propertyOwnershipCreatedDate =
                                addPropertyOwnershipToBatchReturningCreatedDate(
                                    registrationNumberStmt,
                                    propertyOwnershipStmt,
                                    licenceStmt,
                                    isOccupied,
                                    registrationNumberId = (++registrationNumbersAdded).toLong(),
                                    licenceIdIfHasLicence = if (hasLicence) (++licencesAdded).toLong() else null,
                                    propertyOwnershipId,
                                    landlord,
                                )

                            val probabilityOfComplianceRecord = if (isOccupied) 0.9 else 0.1
                            if (NftDataFaker.generateBoolean(probabilityTrue = probabilityOfComplianceRecord)) {
                                fileUploadsAdded =
                                    addPropertyComplianceToBatchReturningUpdatedFileUploadsAdded(
                                        fileUploadStmt,
                                        certificateUploadStmt,
                                        propertyComplianceStmt,
                                        propertyOwnershipId,
                                        propertyOwnershipCreatedDate,
                                        fileUploadsAdded,
                                    )
                            }
                        } else {
                            val hasReminderEmailBeenSent = NftDataFaker.generateBoolean(probabilityTrue = 0.25)
                            addIncompletePropertyToBatch(
                                reminderEmailSentStmt,
                                savedJourneyStateStmt,
                                incompletePropertyStmt,
                                reminderEmailSentIdIfSent = if (hasReminderEmailBeenSent) (++reminderEmailsAdded).toLong() else null,
                                savedJourneyStateId = (++incompletePropertiesAdded).toLong(),
                                landlord,
                            )
                        }

                        if (propertyOwnershipsAdded % BATCH_SIZE == 0 || propertyRegistrationsAdded() == NUM_OF_PROPERTIES) {
                            registrationNumberStmt.executeBatch()
                            licenceStmt.executeBatch()
                            propertyOwnershipStmt.executeBatch()
                            registrationNumberGenerator.forgetUsedValues()
                            availableAddressGenerator.forgetUsedValues()

                            fileUploadStmt.executeBatch()
                            certificateUploadStmt.executeBatch()
                            propertyComplianceStmt.executeBatch()
                        }
                        if (incompletePropertiesAdded % BATCH_SIZE == 0 || propertyRegistrationsAdded() == NUM_OF_PROPERTIES) {
                            reminderEmailSentStmt.executeBatch()
                            savedJourneyStateStmt.executeBatch()
                            incompletePropertyStmt.executeBatch()
                        }

                        if (propertyRegistrationsAdded() % BATCH_SIZE == 0 || propertyRegistrationsAdded() == NUM_OF_PROPERTIES) {
                            log("Seeded ${propertyRegistrationsAdded()} property ownerships/incomplete properties")
                        }
                    }
                }
            }
        } finally {
            oneLoginUserStmt.close()
            registrationNumberStmt.close()
            landlordStmt.close()

            licenceStmt.close()
            propertyOwnershipStmt.close()

            fileUploadStmt.close()
            certificateUploadStmt.close()
            propertyComplianceStmt.close()

            reminderEmailSentStmt.close()
            savedJourneyStateStmt.close()
            incompletePropertyStmt.close()
        }

        log("Finished seeding landlord data")
    }

    private fun addSystemOperatorToBatch(
        oneLoginUserStmt: PreparedStatement,
        systemOperatorStmt: PreparedStatement,
    ) {
        val subjectIdentifier = NftDataFaker.generateSubjectIdentifier()
        val createdDate = NftDataFaker.generateCreatedDate()

        oneLoginUserStmt.setString(1, subjectIdentifier)
        oneLoginUserStmt.setTimestamp(2, createdDate)
        oneLoginUserStmt.addBatch()

        systemOperatorStmt.setTimestamp(1, createdDate)
        systemOperatorStmt.setTimestamp(2, NftDataFaker.generateLastModifiedDate(createdDate))
        systemOperatorStmt.setString(3, subjectIdentifier)
        systemOperatorStmt.addBatch()
    }

    private fun addLcUserToBatch(
        oneLoginUserStmt: PreparedStatement,
        localCouncilUserStmt: PreparedStatement,
        isManager: Boolean,
        localCouncilId: Int,
    ) {
        val subjectIdentifier = NftDataFaker.generateSubjectIdentifier()
        val createdDate = NftDataFaker.generateCreatedDate()

        oneLoginUserStmt.setString(1, subjectIdentifier)
        oneLoginUserStmt.setTimestamp(2, createdDate)
        oneLoginUserStmt.addBatch()

        val name = NftDataFaker.generateName()

        localCouncilUserStmt.setTimestamp(1, createdDate)
        localCouncilUserStmt.setTimestamp(2, NftDataFaker.generateLastModifiedDate(createdDate))
        localCouncilUserStmt.setString(3, subjectIdentifier)
        localCouncilUserStmt.setBoolean(4, isManager)
        localCouncilUserStmt.setString(5, name)
        localCouncilUserStmt.setString(6, NftDataFaker.generateEmail(name))
        localCouncilUserStmt.setInt(7, localCouncilId)
        localCouncilUserStmt.addBatch()
    }

    private fun addLcInvitationToBatch(
        localCouncilInvitationStmt: PreparedStatement,
        isManager: Boolean,
        localCouncilId: Int,
    ) {
        localCouncilInvitationStmt.setTimestamp(1, NftDataFaker.generateCreatedDate())
        localCouncilInvitationStmt.setObject(2, NftDataFaker.generateInvitationToken())
        localCouncilInvitationStmt.setString(3, NftDataFaker.generateEmail())
        localCouncilInvitationStmt.setBoolean(4, isManager)
        localCouncilInvitationStmt.setInt(5, localCouncilId)
        localCouncilInvitationStmt.addBatch()
    }

    private fun addLandlordToBatch(
        oneLoginUserStmt: PreparedStatement,
        registrationNumberStmt: PreparedStatement,
        landlordStmt: PreparedStatement,
        coreDetails: CoreLandlordDetails,
        registrationNumberId: Long,
    ) {
        oneLoginUserStmt.setString(1, coreDetails.subjectId)
        oneLoginUserStmt.setTimestamp(2, coreDetails.createdDate)
        oneLoginUserStmt.addBatch()

        registrationNumberStmt.setLong(1, registrationNumberId)
        registrationNumberStmt.setTimestamp(2, coreDetails.createdDate)
        registrationNumberStmt.setLong(3, registrationNumberGenerator.next())
        registrationNumberStmt.setInt(4, RegistrationNumberType.LANDLORD.ordinal)
        registrationNumberStmt.addBatch()

        val name = NftDataFaker.generateName()
        val isVerified = NftDataFaker.generateBoolean(probabilityTrue = 0.8)
        val hasRespondedToFeedback = NftDataFaker.generateBoolean(probabilityTrue = 0.4)

        landlordStmt.setLong(1, coreDetails.id)
        landlordStmt.setTimestamp(2, coreDetails.createdDate)
        landlordStmt.setTimestamp(3, NftDataFaker.generateLastModifiedDate(coreDetails.createdDate))
        landlordStmt.setString(4, coreDetails.subjectId)
        landlordStmt.setString(5, name)
        landlordStmt.setString(6, NftDataFaker.generateEmail(name))
        landlordStmt.setString(7, NftDataFaker.generatePhoneNumber())
        landlordStmt.setLong(8, addressGenerator.next().id)
        landlordStmt.setDate(9, NftDataFaker.generateDateOfBirth())
        landlordStmt.setLong(10, registrationNumberId)
        landlordStmt.setBoolean(11, hasRespondedToFeedback)
        landlordStmt.setBoolean(12, isVerified)
        landlordStmt.addBatch()
    }

    private fun addPropertyOwnershipToBatchReturningCreatedDate(
        registrationNumberStmt: PreparedStatement,
        propertyOwnershipStmt: PreparedStatement,
        licenceStmt: PreparedStatement,
        isOccupied: Boolean,
        registrationNumberId: Long,
        licenceIdIfHasLicence: Long?,
        propertyOwnershipId: Long,
        landlordDetails: CoreLandlordDetails,
    ): Timestamp {
        val createdDate = NftDataFaker.generateCreatedDate(after = landlordDetails.createdDate)

        registrationNumberStmt.setLong(1, registrationNumberId)
        registrationNumberStmt.setTimestamp(2, createdDate)
        registrationNumberStmt.setLong(3, registrationNumberGenerator.next())
        registrationNumberStmt.setInt(4, RegistrationNumberType.PROPERTY.ordinal)
        registrationNumberStmt.addBatch()

        if (licenceIdIfHasLicence != null) {
            val licenceTypeAndNumber = NftDataFaker.generateLicenceTypeAndNumber()

            licenceStmt.setLong(1, licenceIdIfHasLicence)
            licenceStmt.setTimestamp(2, createdDate)
            licenceStmt.setTimestamp(3, NftDataFaker.generateLastModifiedDate(createdDate))
            licenceStmt.setInt(4, licenceTypeAndNumber.first.ordinal)
            licenceStmt.setString(5, licenceTypeAndNumber.second)
            licenceStmt.addBatch()
        }

        val numHouseholdsAndTenants = if (isOccupied) NftDataFaker.generateNumHouseholdsAndTenants() else Pair(0, 0)
        val numBedrooms = if (isOccupied) NftDataFaker.generateNumBedrooms() else null
        val standardAndCustomBillsIncluded = if (isOccupied) NftDataFaker.generateStandardAndCustomBillsIncluded() else null
        val furnishedStatus = if (isOccupied) NftDataFaker.generateFurnishedStatus() else null
        val rentDetails = if (isOccupied) NftDataFaker.generateRentDetails() else null

        propertyOwnershipStmt.setLong(1, propertyOwnershipId)
        propertyOwnershipStmt.setTimestamp(2, createdDate)
        propertyOwnershipStmt.setTimestamp(3, NftDataFaker.generateLastModifiedDate(createdDate))
        propertyOwnershipStmt.setInt(4, NftDataFaker.generateOwnershipType().ordinal)
        propertyOwnershipStmt.setInt(5, numHouseholdsAndTenants.first)
        propertyOwnershipStmt.setInt(6, numHouseholdsAndTenants.second)
        propertyOwnershipStmt.setLong(7, registrationNumberId)
        propertyOwnershipStmt.setLong(8, landlordDetails.id)
        propertyOwnershipStmt.setLongOrNull(9, licenceIdIfHasLicence)
        // TODO PDJB-239: probabilistically add incomplete compliance form (after migration to saved journey state)
        propertyOwnershipStmt.setLongOrNull(10, null)
        propertyOwnershipStmt.setInt(11, NftDataFaker.generatePropertyAndOtherType().first.ordinal)
        propertyOwnershipStmt.setLong(12, availableAddressGenerator.next().id)
        propertyOwnershipStmt.setIntOrNull(13, numBedrooms)
        propertyOwnershipStmt.setStringOrNull(14, standardAndCustomBillsIncluded?.first)
        propertyOwnershipStmt.setStringOrNull(15, standardAndCustomBillsIncluded?.second)
        propertyOwnershipStmt.setIntOrNull(16, furnishedStatus?.ordinal)
        propertyOwnershipStmt.setIntOrNull(17, rentDetails?.rentFrequency?.ordinal)
        propertyOwnershipStmt.setStringOrNull(18, rentDetails?.customRentFrequency)
        propertyOwnershipStmt.setBigDecimalOrNull(19, rentDetails?.rentAmount)
        propertyOwnershipStmt.addBatch()

        return createdDate
    }

    private fun addIncompletePropertyToBatch(
        reminderEmailSentStmt: PreparedStatement,
        savedJourneyStateStmt: PreparedStatement,
        landlordIncompletePropertiesStmt: PreparedStatement,
        reminderEmailSentIdIfSent: Long?,
        savedJourneyStateId: Long,
        landlordDetails: CoreLandlordDetails,
    ) {
        val reminderEmailSent = reminderEmailSentIdIfSent != null
        val createdDate = NftDataFaker.generateIncompletePropertyCreatedDate(landlordDetails.createdDate, reminderEmailSent)

        if (reminderEmailSent) {
            reminderEmailSentStmt.setLong(1, reminderEmailSentIdIfSent)
            reminderEmailSentStmt.setTimestamp(2, NftDataFaker.generateLastEmailReminderSentDate(createdDate))
            reminderEmailSentStmt.addBatch()
        }

        val address = availableAddressGenerator.next()

        savedJourneyStateStmt.setLong(1, savedJourneyStateId)
        savedJourneyStateStmt.setTimestamp(2, createdDate)
        savedJourneyStateStmt.setTimestamp(3, NftDataFaker.generateLastModifiedDate(createdDate))
        savedJourneyStateStmt.setString(4, NftDataFaker.generateJourneyId())
        savedJourneyStateStmt.setString(5, NftDataFaker.generateIncompletePropertyJourneyState(address))
        savedJourneyStateStmt.setString(6, landlordDetails.subjectId)
        savedJourneyStateStmt.setLongOrNull(7, reminderEmailSentIdIfSent)
        savedJourneyStateStmt.addBatch()

        landlordIncompletePropertiesStmt.setLong(1, landlordDetails.id)
        landlordIncompletePropertiesStmt.setLong(2, savedJourneyStateId)
        landlordIncompletePropertiesStmt.addBatch()
    }

    private fun addPropertyComplianceToBatchReturningUpdatedFileUploadsAdded(
        fileUploadStmt: PreparedStatement,
        certificateUploadStmt: PreparedStatement,
        propertyComplianceStmt: PreparedStatement,
        propertyOwnershipId: Long,
        propertyOwnershipCreatedDate: Timestamp,
        currentFileUploadCount: Int,
    ): Int {
        val createdDate = NftDataFaker.generateCreatedDate(after = propertyOwnershipCreatedDate)
        val complianceData = NftDataFaker.generatePropertyComplianceData(createdDate)

        var updatedFileUploadCount = currentFileUploadCount
        var gasSafetyUploadId: Long? = null
        var eicrUploadId: Long? = null

        if (complianceData.gasSafetyCertEngineerNum != null) {
            gasSafetyUploadId = (++updatedFileUploadCount).toLong()
            addFileUploadToBatch(
                fileUploadStmt,
                certificateUploadStmt,
                propertyOwnershipId,
                createdDate,
                FileCategory.GasSafetyCert,
                gasSafetyUploadId,
            )
        }
        if (complianceData.eicrExpiryDate?.after(createdDate) == true) {
            eicrUploadId = (++updatedFileUploadCount).toLong()
            addFileUploadToBatch(
                fileUploadStmt,
                certificateUploadStmt,
                propertyOwnershipId,
                createdDate,
                FileCategory.Eicr,
                eicrUploadId,
            )
        }

        propertyComplianceStmt.setTimestamp(1, createdDate)
        propertyComplianceStmt.setTimestamp(2, NftDataFaker.generateLastModifiedDate(createdDate))
        propertyComplianceStmt.setLong(3, propertyOwnershipId)
        propertyComplianceStmt.setLongOrNull(4, gasSafetyUploadId)
        propertyComplianceStmt.setDateOrNull(5, complianceData.gasSafetyCertIssueDate)
        propertyComplianceStmt.setStringOrNull(6, complianceData.gasSafetyCertEngineerNum)
        propertyComplianceStmt.setIntOrNull(7, complianceData.gasSafetyCertExemptionAndOtherReason?.first?.ordinal)
        propertyComplianceStmt.setStringOrNull(8, complianceData.gasSafetyCertExemptionAndOtherReason?.second)
        propertyComplianceStmt.setLongOrNull(9, eicrUploadId)
        propertyComplianceStmt.setDateOrNull(10, complianceData.eicrIssueDate)
        propertyComplianceStmt.setIntOrNull(11, complianceData.eicrExemptionAndOtherReason?.first?.ordinal)
        propertyComplianceStmt.setStringOrNull(12, complianceData.eicrExemptionAndOtherReason?.second)
        propertyComplianceStmt.setStringOrNull(13, complianceData.epcNumber?.let { epcCertificateUrlProvider.getEpcCertificateUrl(it) })
        propertyComplianceStmt.setDateOrNull(14, complianceData.epcExpiryDate)
        propertyComplianceStmt.setBooleanOrNull(15, complianceData.tenancyStartedBeforeEpcExpiry)
        propertyComplianceStmt.setStringOrNull(16, complianceData.epcEnergyRating)
        propertyComplianceStmt.setIntOrNull(17, complianceData.epcExemptionReason?.ordinal)
        propertyComplianceStmt.setIntOrNull(18, complianceData.epcMeesExemptionReason?.ordinal)
        propertyComplianceStmt.addBatch()

        return updatedFileUploadCount
    }

    // TODO PDJB-239: Upload files to S3
    private fun addFileUploadToBatch(
        fileUploadStmt: PreparedStatement,
        certificateUploadStmt: PreparedStatement,
        propertyOwnershipId: Long,
        createdDate: Timestamp,
        fileCategory: FileCategory,
        fileUploadId: Long,
    ) {
        fileUploadStmt.setLong(1, fileUploadId)
        fileUploadStmt.setTimestamp(2, createdDate)
        fileUploadStmt.setTimestamp(3, NftDataFaker.generateLastModifiedDate(createdDate))
        fileUploadStmt.setString(4, PropertyComplianceJourneyHelper.getCertFilename(propertyOwnershipId, fileCategory))
        fileUploadStmt.setString(5, NftDataFaker.generateETag())
        fileUploadStmt.addBatch()

        certificateUploadStmt.setTimestamp(1, createdDate)
        certificateUploadStmt.setTimestamp(2, NftDataFaker.generateLastModifiedDate(createdDate))
        certificateUploadStmt.setInt(3, fileCategory.ordinal)
        certificateUploadStmt.setLong(4, propertyOwnershipId)
        certificateUploadStmt.setLong(5, fileUploadId)
        certificateUploadStmt.addBatch()
    }

    private fun log(message: String) {
        println("${LocalDateTime.now()} $message")
    }

    companion object {
        const val NUM_OF_SYSTEM_OPERATORS = 150
        const val NUM_OF_LC_USERS = 3000
        const val NUM_OF_LANDLORDS = 2820000
        const val NUM_OF_PROPERTIES = 4700000
        const val BATCH_SIZE = 10000
    }

    private abstract class Generator<T> {
        protected var values = emptyList<T>()
        private var index = 0

        abstract fun replenishValues()

        fun next(): T {
            while (index == values.size) {
                replenishValues()
            }
            return values[index++]
        }

        fun forgetUsedValues() {
            values = values.drop(index)
            index = 0
        }
    }

    private inner class RegistrationNumberGenerator : Generator<Long>() {
        override fun replenishValues() {
            val valueSet = values.toSet()
            val potentialNewNumbers = NftDataFaker.generateRegistrationNumbers(BATCH_SIZE * 5)
            val alreadyUsedNumbers = nftDataSeederDao.findRegistrationNumbersIn(potentialNewNumbers).toSet()
            val newNumbers = potentialNewNumbers - alreadyUsedNumbers
            values = (valueSet + newNumbers).toList()
        }
    }

    private val addressCount by lazy { addressRepository.count().toInt() }

    private inner class AddressGenerator(
        private val restrictToAvailable: Boolean = false,
    ) : Generator<Address>() {
        private val replenishmentSize = BATCH_SIZE * 5

        override fun replenishValues() {
            val valueSet = values.toSet()
            val newAddresses =
                nftDataSeederDao.findAddresses(
                    limit = replenishmentSize,
                    offset = NftDataFaker.generateNumberLessThan(addressCount - replenishmentSize),
                    restrictToAvailable,
                )
            values = (valueSet + newAddresses.shuffled()).toList()
        }
    }
}
