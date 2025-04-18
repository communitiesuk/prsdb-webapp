package uk.gov.communities.prsdb.webapp.testHelpers.builders

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import java.time.LocalDate

class JourneyDataBuilder(
    private val mockLocalAuthorityService: LocalAuthorityService,
    initialJourneyData: JourneyData? = null,
) {
    private val journeyData = initialJourneyData?.toMutableMap() ?: mutableMapOf()

    fun build() = journeyData

    companion object {
        const val DEFAULT_ADDRESS = "4, Example Road, EG"

        private val defaultPropertyJourneyData: JourneyData =
            mapOf(
                "lookup-address" to
                    mapOf(
                        "postcode" to "EG",
                        "houseNameOrNumber" to "4",
                    ),
                "select-address" to
                    mapOf(
                        "address" to DEFAULT_ADDRESS,
                    ),
                "property-type" to
                    mapOf(
                        "propertyType" to "OTHER",
                        "customPropertyType" to "Bungalow",
                    ),
                "ownership-type" to
                    mapOf(
                        "ownershipType" to "FREEHOLD",
                    ),
                "occupancy" to
                    mapOf(
                        "occupied" to "true",
                    ),
                "number-of-households" to
                    mapOf(
                        "numberOfHouseholds" to "2",
                    ),
                "number-of-people" to
                    mapOf(
                        "numberOfPeople" to "4",
                    ),
                "licensing-type" to
                    mapOf(
                        "licensingType" to "HMO_MANDATORY_LICENCE",
                    ),
                "hmo-mandatory-licence" to
                    mapOf(
                        "licenceNumber" to "test1234",
                    ),
            )

        fun propertyDefault(localAuthorityService: LocalAuthorityService) =
            JourneyDataBuilder(localAuthorityService, defaultPropertyJourneyData).withSelectedAddress(
                DEFAULT_ADDRESS,
                709902,
                createLocalAuthority(),
            )

        private val defaultLandlordJourneyData: JourneyData =
            mapOf(
                LandlordRegistrationStepId.VerifyIdentity.urlPathSegment to mapOf(),
                LandlordRegistrationStepId.Name.urlPathSegment to mapOf("name" to "Arthur Dent"),
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment to
                    mapOf(
                        "day" to 6,
                        "month" to 8,
                        "year" to 2000,
                    ),
                LandlordRegistrationStepId.Email.urlPathSegment to mapOf("emailAddress" to "test@example.com"),
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment to mapOf("phoneNumber" to "07123456789"),
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment to mapOf("livesInEnglandOrWales" to true),
                LandlordRegistrationStepId.LookupAddress.urlPathSegment to mapOf("houseNameOrNumber" to "44", "postcode" to "EG1 1GE"),
                LandlordRegistrationStepId.SelectAddress.urlPathSegment to mapOf("address" to DEFAULT_ADDRESS),
            )

        fun landlordDefault(localAuthorityService: LocalAuthorityService) =
            JourneyDataBuilder(localAuthorityService, defaultLandlordJourneyData).withSelectedAddress(
                DEFAULT_ADDRESS,
                709902,
                createLocalAuthority(),
            )
    }

    fun withLookupAddress(
        houseNameOrNumber: String,
        postcode: String,
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        val lookupAddressKey = if (isContactAddress) "lookup-contact-address" else "lookup-address"
        journeyData[lookupAddressKey] = mapOf("houseNameOrNumber" to houseNameOrNumber, "postcode" to postcode)
        return this
    }

    fun withSelectedAddress(
        singleLineAddress: String,
        uprn: Long? = null,
        localAuthority: LocalAuthority? = null,
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        localAuthority?.let {
            whenever(mockLocalAuthorityService.retrieveLocalAuthorityById(localAuthority.id)).thenReturn(localAuthority)
        }

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        journeyData[LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY] =
            Json.encodeToString(listOf(AddressDataModel(singleLineAddress, localAuthorityId = localAuthority?.id, uprn = uprn)))

        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to singleLineAddress)
        return this
    }

    fun withManualAddress(
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
        localAuthority: LocalAuthority? = null,
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mapOf("address" to MANUAL_ADDRESS_CHOSEN)

        val manualAddressKey = if (isContactAddress) "manual-contact-address" else "manual-address"
        journeyData[manualAddressKey] =
            mapOf(
                "addressLineOne" to addressLineOne,
                "townOrCity" to townOrCity,
                "postcode" to postcode,
            )

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

        if (localAuthority != null) {
            whenever(mockLocalAuthorityService.retrieveLocalAuthorityById(localAuthority.id)).thenReturn(localAuthority)
        }

        journeyData["local-authority"] =
            mapOf("localAuthorityId" to localAuthority?.id)

        return this
    }

    private fun withEnglandOrWalesResidence(): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment] =
            mapOf(
                "livesInEnglandOrWales" to true,
            )
        return this
    }

    fun withPropertyType(
        type: PropertyType,
        customType: String = "type",
    ): JourneyDataBuilder {
        journeyData["property-type"] =
            if (type == PropertyType.OTHER) {
                mapOf("propertyType" to type.name, "customPropertyType" to customType)
            } else {
                mapOf("propertyType" to type.name)
            }
        return this
    }

    fun withOwnershipType(ownershipType: OwnershipType): JourneyDataBuilder {
        journeyData["ownership-type"] = mapOf("ownershipType" to ownershipType.name)
        return this
    }

    fun withLicensingType(
        licensingType: LicensingType,
        licenseNumber: String? = null,
    ): JourneyDataBuilder {
        journeyData["licensing-type"] = mapOf("licensingType" to licensingType.name)
        when (licensingType) {
            LicensingType.SELECTIVE_LICENCE -> withLicenceNumber("selective-licence", licenseNumber)
            LicensingType.HMO_MANDATORY_LICENCE -> withLicenceNumber("hmo-mandatory-licence", licenseNumber)
            LicensingType.HMO_ADDITIONAL_LICENCE -> withLicenceNumber("hmo-additional-licence", licenseNumber)
            LicensingType.NO_LICENSING -> {}
        }
        return this
    }

    fun withLicenceNumber(
        urlPathSegment: String,
        licenceNumber: String?,
    ): JourneyDataBuilder {
        journeyData[urlPathSegment] = mapOf("licenceNumber" to licenceNumber)
        return this
    }

    fun withNoTenants(): JourneyDataBuilder {
        journeyData.remove("number-of-households")
        journeyData.remove("number-of-people")
        return withOccupiedSetToFalse()
    }

    fun withOccupiedSetToFalse(): JourneyDataBuilder {
        journeyData["occupancy"] =
            mapOf(
                "occupied" to "false",
            )
        return this
    }

    fun withTenants(
        households: Int,
        people: Int,
    ): JourneyDataBuilder {
        journeyData["occupancy"] =
            mapOf(
                "occupied" to "true",
            )
        journeyData["number-of-households"] =
            mapOf(
                "numberOfHouseholds" to households.toString(),
            )
        journeyData["number-of-people"] =
            mapOf(
                "numberOfPeople" to people.toString(),
            )

        return this
    }

    fun withVerifiedUser(
        name: String,
        dob: LocalDate,
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment] =
            mapOf(
                "name" to name,
                "birthDate" to dob,
            )
        return this
    }

    fun withUnverifiedUser(
        name: String,
        dob: LocalDate,
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.Name.urlPathSegment] = mapOf("name" to name)
        journeyData[LandlordRegistrationStepId.DateOfBirth.urlPathSegment] =
            mapOf("day" to dob.dayOfMonth, "month" to dob.monthValue, "year" to dob.year)
        return this
    }

    fun withEmailAddress(emailAddress: String): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.Email.urlPathSegment] = mapOf("emailAddress" to emailAddress)
        return this
    }

    fun withPhoneNumber(phoneNumber: String): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.PhoneNumber.urlPathSegment] = mapOf("phoneNumber" to phoneNumber)
        return this
    }

    fun withNonEnglandOrWalesAndSelectedContactAddress(
        countryOfResidence: String,
        nonEnglandOrWalesAddress: String,
        selectedAddress: String,
    ): JourneyDataBuilder =
        this
            .withNonEnglandOrWalesAddress(countryOfResidence, nonEnglandOrWalesAddress)
            .withSelectedAddress(selectedAddress, isContactAddress = true)

    fun withNonEnglandOrWalesAndManualContactAddress(
        countryOfResidence: String,
        nonEnglandOrWalesAddress: String,
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
    ): JourneyDataBuilder =
        this
            .withNonEnglandOrWalesAddress(countryOfResidence, nonEnglandOrWalesAddress)
            .withManualAddress(addressLineOne, townOrCity, postcode, isContactAddress = true)

    private fun withNonEnglandOrWalesAddress(
        countryOfResidence: String,
        nonEnglandOrWalesAddress: String,
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment] =
            mapOf(
                "livesInEnglandOrWales" to false,
                "countryOfResidence" to countryOfResidence,
            )
        journeyData[LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment] =
            mapOf("nonEnglandOrWalesAddress" to nonEnglandOrWalesAddress)
        return this
    }

    fun withEmailAddressUpdate(newEmail: String): JourneyDataBuilder {
        journeyData[UpdateLandlordDetailsStepId.UpdateEmail.urlPathSegment] = mapOf("emailAddress" to newEmail)
        return this
    }

    fun withNameUpdate(newName: String): JourneyDataBuilder {
        journeyData[UpdateLandlordDetailsStepId.UpdateName.urlPathSegment] = mapOf("name" to newName)
        return this
    }

    fun withDateOfBirthUpdate(dateOfBirth: LocalDate): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.DateOfBirth.urlPathSegment] =
            mapOf("day" to dateOfBirth.dayOfMonth, "month" to dateOfBirth.monthValue, "year" to dateOfBirth.year)
        return this
    }

    fun withOwnershipTypeUpdate(ownershipType: OwnershipType): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment] =
            mutableMapOf("ownershipType" to ownershipType.name)
        return this
    }

    fun withIsOccupiedUpdate(isOccupied: Boolean): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment] =
            mutableMapOf("occupied" to isOccupied)
        return this
    }

    fun withNumberOfHouseholdsUpdate(numberOfHouseholds: Int): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment] =
            mutableMapOf("numberOfHouseholds" to numberOfHouseholds)
        return this
    }

    fun withNumberOfPeopleUpdate(numberOfPeople: Int): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment] =
            mutableMapOf("numberOfPeople" to numberOfPeople)
        return this
    }

    fun withLicensingTypeUpdate(licensingType: LicensingType): JourneyDataBuilder {
        journeyData[UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment] = mutableMapOf("licensingType" to licensingType.name)
        return this
    }

    fun withLicenceNumberUpdate(
        licenceNumber: String,
        licensingType: LicensingType,
    ): JourneyDataBuilder {
        val licenseNumberUpdateStepIdUrlPathSegment =
            when (licensingType) {
                LicensingType.SELECTIVE_LICENCE -> UpdatePropertyDetailsStepId.UpdateSelectiveLicence.urlPathSegment
                LicensingType.HMO_MANDATORY_LICENCE -> UpdatePropertyDetailsStepId.UpdateHmoMandatoryLicence.urlPathSegment
                LicensingType.HMO_ADDITIONAL_LICENCE -> UpdatePropertyDetailsStepId.UpdateHmoAdditionalLicence.urlPathSegment
                LicensingType.NO_LICENSING -> ""
            }
        journeyData[licenseNumberUpdateStepIdUrlPathSegment] = mapOf("licenceNumber" to licenceNumber)
        return this
    }

    fun withLicenceUpdate(
        licensingType: LicensingType,
        licenceNumber: String,
    ): JourneyDataBuilder =
        this
            .withLicensingTypeUpdate(licensingType)
            .withLicenceNumberUpdate(licenceNumber, licensingType)

    fun withOriginalData(
        originalDataKey: String,
        originalData: JourneyData,
    ): JourneyDataBuilder {
        journeyData[originalDataKey] = originalData
        return this
    }

    fun withOriginalNumberOfHouseholdsData(
        originalDataKey: String,
        originalNumberOfHouseholds: Int,
    ): JourneyDataBuilder {
        journeyData[originalDataKey] =
            mapOf(
                UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment to
                    mapOf("numberOfHouseholds" to originalNumberOfHouseholds),
            )
        return this
    }

    fun withGasSafetyCertStatus(hasGasSafetyCert: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafety.urlPathSegment] =
            mapOf(GasSafetyFormModel::hasCert.name to hasGasSafetyCert)
        return this
    }

    fun withGasSafetyIssueDate(issueDate: LocalDate): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment] =
            mapOf(
                TodayOrPastDateFormModel::day.name to issueDate.dayOfMonth,
                TodayOrPastDateFormModel::month.name to issueDate.monthValue,
                TodayOrPastDateFormModel::year.name to issueDate.year,
            )
        return this
    }

    fun withGasSafetyCertExemptionStatus(hasGasSafetyCertExemption: Boolean): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyExemption.urlPathSegment] =
            mapOf(GasSafetyExemptionFormModel::hasExemption.name to hasGasSafetyCertExemption)
        return this
    }

    fun withGasSafetyCertExemptionReason(gasSafetyCertExemptionReason: GasSafetyExemptionReason): JourneyDataBuilder {
        journeyData[PropertyComplianceStepId.GasSafetyExemptionReason.urlPathSegment] =
            mapOf(GasSafetyExemptionReasonFormModel::exemptionReason.name to gasSafetyCertExemptionReason)
        return this
    }
}
