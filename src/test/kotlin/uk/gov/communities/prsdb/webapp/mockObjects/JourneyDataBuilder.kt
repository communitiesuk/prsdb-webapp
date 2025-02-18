package uk.gov.communities.prsdb.webapp.mockObjects

import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import java.time.LocalDate

class JourneyDataBuilder(
    private val mockAddressDataService: AddressDataService,
    private val mockLocalAuthorityService: LocalAuthorityService,
    initialJourneyData: Map<String, Any?>? = null,
) {
    private val journeyData = initialJourneyData?.toMutableMap() ?: mutableMapOf()

    fun build() = journeyData

    companion object {
        const val DEFAULT_ADDRESS = "4, Example Road, EG"

        private val defaultPropertyJourneyData: Map<String, Any?> =
            mapOf(
                "lookup-address" to
                    mutableMapOf(
                        "postcode" to "EG",
                        "houseNameOrNumber" to "4",
                    ),
                "select-address" to
                    mutableMapOf(
                        "address" to DEFAULT_ADDRESS,
                    ),
                "property-type" to
                    mutableMapOf(
                        "propertyType" to "OTHER",
                        "customPropertyType" to "Bungalow",
                    ),
                "ownership-type" to
                    mutableMapOf(
                        "ownershipType" to "FREEHOLD",
                    ),
                "occupancy" to
                    mutableMapOf(
                        "occupied" to "true",
                    ),
                "number-of-households" to
                    mutableMapOf(
                        "numberOfHouseholds" to "2",
                    ),
                "number-of-people" to
                    mutableMapOf(
                        "numberOfPeople" to "4",
                    ),
                "landlord-type" to
                    mutableMapOf(
                        "landlordType" to "SOLE",
                    ),
                "licensing-type" to
                    mutableMapOf(
                        "licensingType" to "HMO_MANDATORY_LICENCE",
                    ),
                "hmo-mandatory-licence" to
                    mutableMapOf(
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

        private val defaultLandlordJourneyData: Map<String, Any?> =
            mapOf(
                LandlordRegistrationStepId.Name.urlPathSegment to mutableMapOf("name" to "Arthur Dent"),
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment to
                    mutableMapOf(
                        "day" to 6,
                        "month" to 8,
                        "year" to 2000,
                    ),
                LandlordRegistrationStepId.Email.urlPathSegment to mutableMapOf("emailAddress" to "test@example.com"),
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment to mutableMapOf("phoneNumber" to "07123456789"),
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment to mutableMapOf("livesInEnglandOrWales" to true),
                LandlordRegistrationStepId.SelectAddress.urlPathSegment to mutableMapOf("address" to DEFAULT_ADDRESS),
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
        journeyData[lookupAddressKey] = mutableMapOf("houseNameOrNumber" to houseNameOrNumber, "postcode" to postcode)
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

        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mutableMapOf("address" to singleLineAddress)
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
        journeyData[selectAddressKey] = mutableMapOf("address" to MANUAL_ADDRESS_CHOSEN)

        val manualAddressKey = if (isContactAddress) "manual-contact-address" else "manual-address"
        journeyData[manualAddressKey] =
            mutableMapOf(
                "addressLineOne" to addressLineOne,
                "townOrCity" to townOrCity,
                "postcode" to postcode,
            )

        if (localAuthority != null) {
            whenever(mockLocalAuthorityService.retrieveLocalAuthorityById(localAuthority.id)).thenReturn(localAuthority)
        }

        journeyData["local-authority"] =
            mutableMapOf("localAuthorityId" to localAuthority?.id)

        return this
    }

    fun withPropertyType(
        type: PropertyType,
        customType: String = "type",
    ): JourneyDataBuilder {
        journeyData["property-type"] =
            if (type == PropertyType.OTHER) {
                mutableMapOf("propertyType" to type.name, "customPropertyType" to customType)
            } else {
                mutableMapOf("propertyType" to type.name)
            }
        return this
    }

    fun withOwnershipType(ownershipType: OwnershipType): JourneyDataBuilder {
        journeyData["ownership-type"] = mutableMapOf("ownershipType" to ownershipType.name)
        return this
    }

    fun withLicensingType(
        licensingType: LicensingType,
        licenseNumber: String? = null,
    ): JourneyDataBuilder {
        journeyData["licensing-type"] = mutableMapOf("licensingType" to licensingType.name)
        when (licensingType) {
            LicensingType.SELECTIVE_LICENCE ->
                journeyData["selective-licence"] =
                    mutableMapOf("licenceNumber" to licenseNumber)

            LicensingType.HMO_MANDATORY_LICENCE ->
                journeyData["hmo-mandatory-licence"] =
                    mutableMapOf("licenceNumber" to licenseNumber)

            LicensingType.HMO_ADDITIONAL_LICENCE ->
                journeyData["hmo-additional-licence"] =
                    mutableMapOf("licenceNumber" to licenseNumber)

            LicensingType.NO_LICENSING -> {}
        }
        return this
    }

    fun withNoTenants(): JourneyDataBuilder {
        journeyData["occupancy"] =
            mutableMapOf(
                "occupied" to "false",
            )
        journeyData.remove("number-of-households")
        journeyData.remove("number-of-people")
        return this
    }

    fun withTenants(
        households: Int,
        people: Int,
    ): JourneyDataBuilder {
        journeyData["occupancy"] =
            mutableMapOf(
                "occupied" to "true",
            )
        journeyData["number-of-households"] =
            mutableMapOf(
                "numberOfHouseholds" to households.toString(),
            )
        journeyData["number-of-people"] =
            mutableMapOf(
                "numberOfPeople" to people.toString(),
            )

        return this
    }

    fun withLandlordType(type: LandlordType): JourneyDataBuilder {
        journeyData["landlord-type"] =
            mutableMapOf(
                "landlordType" to type.name,
            )
        return this
    }

    fun withVerifiedUser(
        name: String,
        dob: LocalDate,
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment] =
            mutableMapOf(
                "name" to name,
                "birthDate" to dob,
            )
        return this
    }

    fun withUnverifiedUser(
        name: String,
        dob: LocalDate,
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.Name.urlPathSegment] = mutableMapOf("name" to name)
        journeyData[LandlordRegistrationStepId.DateOfBirth.urlPathSegment] =
            mutableMapOf("day" to dob.dayOfMonth, "month" to dob.monthValue, "year" to dob.year)
        return this
    }

    fun withEmailAddress(emailAddress: String): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.Email.urlPathSegment] = mutableMapOf("emailAddress" to emailAddress)
        return this
    }

    fun withPhoneNumber(phoneNumber: String): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.PhoneNumber.urlPathSegment] = mutableMapOf("phoneNumber" to phoneNumber)
        return this
    }

    fun withInternationalAndSelectedContactAddress(
        countryOfResidence: String,
        internationalAddress: String,
        selectedAddress: String,
    ): JourneyDataBuilder =
        this
            .withInternationalAddress(countryOfResidence, internationalAddress)
            .withSelectedAddress(selectedAddress, localAuthority = createLocalAuthority(), isContactAddress = true)

    fun withInternationalAndManualContactAddress(
        countryOfResidence: String,
        internationalAddress: String,
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
    ): JourneyDataBuilder =
        this
            .withInternationalAddress(countryOfResidence, internationalAddress)
            .withManualAddress(addressLineOne, townOrCity, postcode, isContactAddress = true)

    private fun withInternationalAddress(
        countryOfResidence: String,
        internationalAddress: String,
    ): JourneyDataBuilder {
        journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment] =
            mutableMapOf(
                "livesInEnglandOrWales" to false,
                "countryOfResidence" to countryOfResidence,
            )
        journeyData[LandlordRegistrationStepId.InternationalAddress.urlPathSegment] =
            mutableMapOf("internationalAddress" to internationalAddress)
        return this
    }
}
