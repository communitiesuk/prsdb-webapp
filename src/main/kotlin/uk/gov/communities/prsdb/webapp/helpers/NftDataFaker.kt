package uk.gov.communities.prsdb.webapp.helpers

import net.datafaker.Faker
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.MAX_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.MIN_REG_NUM
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper.Companion.toInstant
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper.Companion.toLocalDate
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper.Companion.toTimestamp
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

object NftDataFaker {
    private val faker = Faker(Locale.UK)

    fun generateBoolean(probabilityTrue: Double = 0.5): Boolean = faker.random().nextDouble() < probabilityTrue

    fun generateNumberLessThan(max: Int): Int = faker.random().nextInt(max)

    fun generateCreatedDate(after: Timestamp? = null): Timestamp =
        if (after != null) {
            generateDateAfter(after)
        } else {
            Timestamp.from(faker.timeAndDate().past(365, TimeUnit.DAYS))
        }

    fun generateLastModifiedDate(createdDate: Timestamp): Timestamp? =
        if (generateBoolean(probabilityTrue = 0.6)) {
            generateDateAfter(createdDate)
        } else {
            null
        }

    fun generateDateOfBirth(): Date = Date.valueOf(faker.timeAndDate().birthday(18, 120))

    fun generateIncompletePropertyCreatedDate(
        landlordCreatedDate: Timestamp,
        reminderEmailSent: Boolean,
    ): Timestamp {
        val now = LocalDate.now()
        val earliestCreatedDate = landlordCreatedDate.coerceAtLeast(now.minusDays(28).toTimestamp())
        return if (reminderEmailSent) {
            val latestCreatedDate =
                now
                    .minusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong())
                    .toTimestamp()
                    .coerceAtLeast(earliestCreatedDate)
            Timestamp.from(faker.timeAndDate().between(earliestCreatedDate.toInstant(), latestCreatedDate.toInstant()))
        } else {
            generateDateAfter(earliestCreatedDate)
        }
    }

    fun generateLastEmailReminderSentDate(createdDate: Timestamp): Timestamp =
        createdDate.toLocalDateTime().plusDays(INCOMPLETE_PROPERTY_AGE_WHEN_REMINDER_EMAIL_DUE_IN_DAYS.toLong()).toTimestamp()

    fun generateInvitationToken(): UUID = UUID.fromString(faker.internet().uuid())

    fun generateSubjectIdentifier(): String = "urn:fdc:gov.uk:2022:${faker.internet().uuid()}"

    fun generateEmail(name: String? = null): String =
        faker.internet().let { if (name != null) it.safeEmailAddress(generateUsername(name)) else it.safeEmailAddress() }

    fun generateName(): String {
        val middleName = if (generateBoolean(probabilityTrue = 0.05)) faker.name().firstName() else null
        return listOfNotNull(faker.name().firstName(), middleName, faker.name().lastName()).joinToString(" ")
    }

    fun generatePhoneNumber(): String =
        faker.phoneNumber().let { if (generateBoolean(probabilityTrue = 0.75)) it.cellPhone() else it.cellPhoneInternational() }

    fun generateRegistrationNumbers(count: Int): Set<Long> {
        val numbers = mutableSetOf<Long>()
        while (numbers.size < count) {
            numbers.add(faker.random().nextLong(MIN_REG_NUM, MAX_REG_NUM + 1))
        }
        return numbers
    }

    fun generateCoreDetailsForLandlords(landlordIds: List<Int>): List<CoreLandlordDetails> =
        landlordIds.map { id ->
            CoreLandlordDetails(
                id = id.toLong(),
                subjectId = generateSubjectIdentifier(),
                createdDate = generateCreatedDate(),
            )
        }

    fun generateNumberOfPropertiesForLandlord(): Int =
        when (faker.random().nextDouble()) {
            // 30%
            in 0.0..0.30 -> 0

            // 31.5%
            in 0.20..0.515 -> 1

            // 26.6%
            in 0.515..0.781 -> faker.random().nextInt(2, 4)

            // 11.8%
            in 0.781..0.899 -> faker.random().nextInt(5, 10)

            // 0.1%
            else -> faker.random().nextInt(10, 50)
        }

    fun generateLicenceTypeAndNumber(): Pair<LicensingType, String> {
        val licenceType = faker.options().option(*LicensingType.licencedEntries.toTypedArray())
        val licenceNumber =
            when (licenceType) {
                LicensingType.SELECTIVE_LICENCE -> faker.regexify("SL-[A-Z0-9]{7}")
                LicensingType.HMO_MANDATORY_LICENCE -> faker.regexify("HMO-M-[A-Z0-9]{7}")
                else -> faker.regexify("HMO-A-[A-Z0-9]{7}")
            }
        return Pair(licenceType, licenceNumber)
    }

    fun generatePropertyAndOtherType(): Pair<PropertyType, String?> {
        val propertyType = faker.options().option(PropertyType::class.java)
        val otherPropertyType = if (propertyType == PropertyType.OTHER) faker.options().option(*otherPropertyTypes) else null
        return Pair(propertyType, otherPropertyType)
    }

    fun generateOwnershipType(): OwnershipType = faker.options().option(OwnershipType::class.java)

    fun generateNumHouseholdsAndTenants(): Pair<Int, Int> {
        val numHouseholds = faker.random().nextInt(1, 10)
        val numTenants = faker.random().nextInt(numHouseholds, 10)
        return Pair(numHouseholds, numTenants)
    }

    fun generateNumBedrooms(): Int = faker.random().nextInt(1, 10)

    fun generateStandardAndCustomBillsIncluded(): Pair<String?, String?> {
        val includeBills = generateBoolean(probabilityTrue = 0.8)
        if (!includeBills) return Pair(null, null)

        val numStandardBillsIncluded = faker.random().nextInt(1, BillsIncluded.standardEntries.size)
        val standardBillsIncluded = faker.options().subset(numStandardBillsIncluded, *BillsIncluded.standardEntries.toTypedArray())

        val includeCustomBills = generateBoolean(probabilityTrue = 0.05)
        val customBillsIncluded =
            if (includeCustomBills) {
                standardBillsIncluded.add(BillsIncluded.SOMETHING_ELSE)
                val numCustomBillsIncluded = faker.random().nextInt(1, customBills.size)
                faker.options().subset(numCustomBillsIncluded, *customBills)
            } else {
                null
            }

        return Pair(standardBillsIncluded.joinToString(separator = ","), customBillsIncluded?.joinToString(separator = ","))
    }

    fun generateFurnishedStatus(): FurnishedStatus = faker.options().option(FurnishedStatus::class.java)

    fun generateRentDetails(): RentDetails {
        val rentFrequency = faker.options().option(RentFrequency::class.java)
        val customRentFrequency = if (rentFrequency == RentFrequency.OTHER) faker.options().option(*customRentFrequencies) else null

        val rentAmount =
            when (rentFrequency) {
                RentFrequency.WEEKLY -> faker.random().nextDouble(50.0, 500.0)
                RentFrequency.FOUR_WEEKLY -> faker.random().nextDouble(200.0, 2000.0)
                else -> faker.random().nextDouble(200.0, 2000.0)
            }
        val formattedRentAmount = BigDecimal(rentAmount).setScale(2, RoundingMode.HALF_UP)

        return RentDetails(rentFrequency, customRentFrequency, formattedRentAmount)
    }

    fun generateETag(): String = faker.regexify("[a-f0-9]{32}")

    fun generatePropertyComplianceData(createdDateTimestamp: Timestamp): PropertyComplianceData {
        val createdDate = Date.valueOf(createdDateTimestamp.toLocalDateTime().toLocalDate())

        val hasGasSafetyExemption = generateBoolean(probabilityTrue = 0.25)
        val gasSafetyExemptionReason = if (hasGasSafetyExemption) generateGasSafetyExemptionReason() else null

        val gasSafetyMissing = !hasGasSafetyExemption && generateBoolean(probabilityTrue = 0.2)
        val gasSafetyIssueDate =
            if (!hasGasSafetyExemption && !gasSafetyMissing) {
                generateDateBefore(createdDate, (GAS_SAFETY_CERT_VALIDITY_YEARS * 365 * 1.5).toLong())
            } else {
                null
            }

        val gasSafetyExpiryDate =
            gasSafetyIssueDate?.let {
                Date.valueOf(it.toLocalDate().plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()))
            }
        val gasSafetyEngineerNum =
            if (createdDate?.let { gasSafetyExpiryDate?.after(it) } == true) {
                faker.regexify("[0-9]{7}")
            } else {
                null
            }

        val hasEicrExemption = generateBoolean(probabilityTrue = 0.25)
        val eicrExemptionReason = if (hasEicrExemption) generateEicrExemptionReason() else null

        val eicrMissing = !hasEicrExemption && generateBoolean(probabilityTrue = 0.2)
        val eicrIssueDate =
            if (!hasEicrExemption && !eicrMissing) {
                generateDateBefore(createdDate, (EICR_VALIDITY_YEARS * 365 * 1.5).toLong())
            } else {
                null
            }

        val hasEpcExemption = generateBoolean(probabilityTrue = 0.25)
        val epcExemptionReason = if (hasEpcExemption) faker.options().option(EpcExemptionReason::class.java) else null

        val epcMissing = !hasEpcExemption && generateBoolean(probabilityTrue = 0.2)
        val epcNumber =
            if (!hasEpcExemption && !epcMissing) {
                faker.options().option(*epcNumbers)
            } else {
                null
            }

        val epcExpiryDate =
            if (epcNumber != null) {
                val expiryDate =
                    faker.timeAndDate().between(
                        createdDate.toLocalDate().minusYears(3).toInstant(),
                        createdDate.toLocalDate().plusYears(6).toInstant(),
                    )
                Date.valueOf(expiryDate.toLocalDate())
            } else {
                null
            }

        val epcExpired = epcExpiryDate?.before(createdDate) == true
        val tenancyStartedBeforeEpcExpiry =
            if (epcExpired) {
                generateBoolean(probabilityTrue = 0.6)
            } else {
                null
            }

        val epcHasAcceptableRating = epcNumber != null && generateBoolean(probabilityTrue = 0.8)
        val epcEnergyRating =
            if (epcNumber != null) {
                if (epcHasAcceptableRating) {
                    faker.options().option("A", "B", "C", "D", "E")
                } else {
                    faker.options().option("F", "G")
                }
            } else {
                null
            }

        val epcNeedsMeesExemption = (!epcExpired || tenancyStartedBeforeEpcExpiry == true) && !epcHasAcceptableRating
        val epcMeesExemptionReason =
            if (epcNeedsMeesExemption && generateBoolean(probabilityTrue = 0.8)) {
                faker.options().option(MeesExemptionReason::class.java)
            } else {
                null
            }

        return PropertyComplianceData(
            gasSafetyCertIssueDate = gasSafetyIssueDate,
            gasSafetyCertEngineerNum = gasSafetyEngineerNum,
            gasSafetyCertExemptionAndOtherReason = gasSafetyExemptionReason,
            eicrIssueDate = eicrIssueDate,
            eicrExemptionAndOtherReason = eicrExemptionReason,
            epcNumber = epcNumber,
            epcExpiryDate = epcExpiryDate,
            tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry,
            epcEnergyRating = epcEnergyRating,
            epcExemptionReason = epcExemptionReason,
            epcMeesExemptionReason = epcMeesExemptionReason,
        )
    }

    fun generateJourneyId(): String = faker.regexify("[a-z0-9]{7}")

    fun generateIncompletePropertyJourneyState(address: Address): String {
        val propertyType = generatePropertyAndOtherType()
        val journeyState =
            """
            {
                "journeyData": {
                    "lookup-address": {
                        "houseNameOrNumber":"${address.buildingNumber ?: address.buildingNumber}", "postcode":"${address.postcode}"
                    },
                    "select-address": {"address":"${address.singleLineAddress}"},
                    "property-type": {"propertyType":"${propertyType.first}","customPropertyType":"${propertyType.second ?: ""}"}
                },
                "cachedAddresses":"[{"singleLineAddress":"${address.singleLineAddress}"}]",
                "isAddressAlreadyRegistered":"false"
            }
            """
        return journeyState
    }

    private val otherPropertyTypes = arrayOf("Bungalow", "Maisonette", "Studio", "Loft", "Cottage")

    private val customBills = arrayOf("Security System", "Pool Maintenance", "Gym Membership", "Parking Fees", "Waste Disposal")

    private val customRentFrequencies = arrayOf("Fortnightly", "Quarterly", "Yearly")

    private val otherGasSafetyExemptionReasons = arrayOf("Student accommodation", "Live in landlord", "New build")

    private val otherEicrExemptionReasons = arrayOf("Student accommodation", "Live in landlord", "New build")

    private val epcNumbers =
        arrayOf(
            "0000-0000-0000-1050-2867",
            "0000-0000-0000-0554-8410",
            "0000-0000-0000-0000-8410",
            "0000-0000-0000-0892-1563",
            "0000-0000-0000-0961-0832",
            "0000-0000-0000-0438-7749",
        )

    private fun generateDateAfter(date: Timestamp): Timestamp = Timestamp.from(faker.timeAndDate().between(date.toInstant(), Instant.now()))

    private fun generateDateBefore(
        date: Date,
        maxDaysAgo: Long,
    ): Date = Date.valueOf(faker.timeAndDate().past(maxDaysAgo, TimeUnit.DAYS, date.toLocalDate().toInstant()).toLocalDate())

    private fun generateUsername(name: String): String {
        val nameParts = name.split(" ")
        val firstName = nameParts.first().lowercase()
        val lastName = nameParts.last().lowercase()

        return when (faker.random().nextInt(0, 10)) {
            0 -> "$firstName.$lastName"
            1 -> "$firstName$lastName"
            2 -> "$firstName${faker.random().nextInt(1, 999)}"
            3 -> "$firstName.$lastName${faker.random().nextInt(1, 99)}"
            4 -> "$lastName-$firstName"
            5 -> "${firstName.first()}$lastName"
            6 -> "$firstName${lastName.first()}"
            7 -> "$firstName-$lastName"
            8 -> "${firstName}_$lastName"
            9 -> "$lastName$firstName"
            else -> "${firstName.first()}_$lastName"
        }
    }

    private fun generateGasSafetyExemptionReason(): Pair<GasSafetyExemptionReason, String?> {
        val reason = faker.options().option(GasSafetyExemptionReason::class.java)
        val otherReason = if (reason == GasSafetyExemptionReason.OTHER) faker.options().option(*otherGasSafetyExemptionReasons) else null
        return Pair(reason, otherReason)
    }

    private fun generateEicrExemptionReason(): Pair<EicrExemptionReason, String?> {
        val reason = faker.options().option(EicrExemptionReason::class.java)
        val otherReason = if (reason == EicrExemptionReason.OTHER) faker.options().option(*otherEicrExemptionReasons) else null
        return Pair(reason, otherReason)
    }

    data class CoreLandlordDetails(
        val id: Long,
        val subjectId: String,
        val createdDate: Timestamp,
    )

    data class RentDetails(
        val rentFrequency: RentFrequency,
        val customRentFrequency: String?,
        val rentAmount: BigDecimal,
    )

    data class PropertyComplianceData(
        val gasSafetyCertIssueDate: Date?,
        val gasSafetyCertEngineerNum: String?,
        val gasSafetyCertExemptionAndOtherReason: Pair<GasSafetyExemptionReason, String?>?,
        val eicrIssueDate: Date?,
        val eicrExemptionAndOtherReason: Pair<EicrExemptionReason, String?>?,
        val epcNumber: String?,
        val epcExpiryDate: Date?,
        val tenancyStartedBeforeEpcExpiry: Boolean?,
        val epcEnergyRating: String?,
        val epcExemptionReason: EpcExemptionReason?,
        val epcMeesExemptionReason: MeesExemptionReason?,
    ) {
        val eicrExpiryDate
            get() = eicrIssueDate?.let { Date.valueOf(it.toLocalDate().plusYears(EICR_VALIDITY_YEARS.toLong())) }
    }
}
