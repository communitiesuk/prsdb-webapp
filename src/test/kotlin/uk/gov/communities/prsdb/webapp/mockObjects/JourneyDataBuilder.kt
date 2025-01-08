package uk.gov.communities.prsdb.webapp.mockObjects

import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import java.time.LocalDate

class JourneyDataBuilder(
    private val mockAddressDataService: AddressDataService,
    initialJourneyData: Map<String, Any?>? = null,
) {
    private val journeyData = initialJourneyData?.toMutableMap() ?: mutableMapOf()

    fun build() = journeyData

    companion object {
        const val DEFAULT_NAME = "Arthur Dent"

        val DEFAULT_DOB = LocalDate.of(2000, 6, 8)

        const val DEFAULT_ADDRESS = "4, Example Road, EG"

        const val DEFAULT_PHONE_NUMBER = "07123456789"

        const val DEFAULT_EMAIL_ADDRESS = "test@example.com"

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

        fun propertyDefault(addressService: AddressDataService) =
            JourneyDataBuilder(addressService, defaultPropertyJourneyData).withSelectedAddress(
                DEFAULT_ADDRESS,
                709902,
                22,
            )

        // Unverified, National, Selected Address
        private val defaultLandlordJourneyData: Map<String, Any?> =
            mapOf(
                LandlordRegistrationStepId.Name.urlPathSegment to mutableMapOf("name" to DEFAULT_NAME),
                LandlordRegistrationStepId.DateOfBirth.urlPathSegment to
                    mutableMapOf(
                        "day" to DEFAULT_DOB.dayOfMonth,
                        "month" to DEFAULT_DOB.monthValue,
                        "year" to DEFAULT_DOB.year,
                    ),
                LandlordRegistrationStepId.Email.urlPathSegment to mutableMapOf("emailAddress" to DEFAULT_EMAIL_ADDRESS),
                LandlordRegistrationStepId.PhoneNumber.urlPathSegment to mutableMapOf("phoneNumber" to DEFAULT_PHONE_NUMBER),
                LandlordRegistrationStepId.CountryOfResidence.urlPathSegment to mutableMapOf("livesInUK" to true),
                LandlordRegistrationStepId.SelectAddress.urlPathSegment to mutableMapOf("address" to DEFAULT_ADDRESS),
            )

        fun landlordDefault(addressService: AddressDataService) =
            JourneyDataBuilder(addressService, defaultLandlordJourneyData).withSelectedAddress(
                DEFAULT_ADDRESS,
                709902,
                22,
            )
    }

    fun withSelectedAddress(
        singleLineAddress: String,
        uprn: Long? = null,
        localAuthorityIndex: Int? = null,
        isContactAddress: Boolean = false,
    ): JourneyDataBuilder {
        whenever(mockAddressDataService.getAddressData(singleLineAddress)).thenReturn(
            AddressDataModel(
                singleLineAddress,
                custodianCode = localAuthorityIndex?.let { LOCAL_AUTHORITIES[it].custodianCode },
                uprn = uprn,
            ),
        )

        val selectAddressKey = if (isContactAddress) "select-contact-address" else "select-address"
        journeyData[selectAddressKey] = mutableMapOf("address" to singleLineAddress)
        return this
    }

    fun withManualAddress(
        addressLineOne: String,
        townOrCity: String,
        postcode: String,
        localAuthorityIndex: Int? = null,
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

        journeyData["local-authority"] =
            mutableMapOf("localAuthorityCustodianCode" to localAuthorityIndex?.let { LOCAL_AUTHORITIES[it].custodianCode })

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

    fun withInternationalAndSelectedContactAddress(
        countryOfResidence: String,
        internationalAddress: String,
        selectedAddress: String,
    ): JourneyDataBuilder =
        this
            .withInternationalAddress(countryOfResidence, internationalAddress)
            .withSelectedAddress(selectedAddress, isContactAddress = true)

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
                "livesInUK" to false,
                "countryOfResidence" to countryOfResidence,
            )
        journeyData[LandlordRegistrationStepId.InternationalAddress.urlPathSegment] =
            mutableMapOf("internationalAddress" to internationalAddress)
        return this
    }
}
