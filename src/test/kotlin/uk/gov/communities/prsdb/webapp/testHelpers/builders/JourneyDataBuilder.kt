package uk.gov.communities.prsdb.webapp.testHelpers.builders

import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import java.time.LocalDate

class JourneyDataBuilder(
    private val mockAddressDataService: AddressDataService,
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

        fun propertyDefault(
            addressService: AddressDataService,
            localAuthorityService: LocalAuthorityService,
        ) = JourneyDataBuilder(addressService, localAuthorityService, defaultPropertyJourneyData).withSelectedAddress(
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
                LandlordRegistrationStepId.LookupAddress.urlPathSegment to
                    mapOf(
                        "houseNameOrNumber" to "44",
                        "postcode" to "EG1 1GE",
                    ),
                LandlordRegistrationStepId.SelectAddress.urlPathSegment to mapOf("address" to DEFAULT_ADDRESS),
            )

        fun landlordDefault(
            addressService: AddressDataService,
            localAuthorityService: LocalAuthorityService,
        ) = JourneyDataBuilder(addressService, localAuthorityService, defaultLandlordJourneyData).withSelectedAddress(
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
        localAuthority: LocalAuthority,
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        whenever(mockAddressDataService.getAddressData(singleLineAddress)).thenReturn(
            AddressDataModel(
                singleLineAddress,
                localAuthorityId = localAuthority.id,
                uprn = uprn,
            ),
        )

        whenever(mockLocalAuthorityService.retrieveLocalAuthorityById(localAuthority.id)).thenReturn(localAuthority)

        if (!isContactAddress) {
            withEnglandOrWalesResidence()
        }

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
            LicensingType.SELECTIVE_LICENCE ->
                journeyData["selective-licence"] =
                    mapOf("licenceNumber" to licenseNumber)

            LicensingType.HMO_MANDATORY_LICENCE ->
                journeyData["hmo-mandatory-licence"] =
                    mapOf("licenceNumber" to licenseNumber)

            LicensingType.HMO_ADDITIONAL_LICENCE ->
                journeyData["hmo-additional-licence"] =
                    mapOf("licenceNumber" to licenseNumber)

            LicensingType.NO_LICENSING -> {}
        }
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
            .withSelectedAddress(selectedAddress, localAuthority = createLocalAuthority(), isContactAddress = true)

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
}
