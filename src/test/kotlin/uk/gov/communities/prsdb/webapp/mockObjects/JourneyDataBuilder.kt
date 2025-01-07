package uk.gov.communities.prsdb.webapp.mockObjects

import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITIES
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService

class JourneyDataBuilder(
    private val mockAddressDataService: AddressDataService,
    initialJourneyData: Map<String, Any?>? = null,
) {
    private val journeyData = initialJourneyData?.toMutableMap() ?: mutableMapOf()

    fun build() = journeyData

    companion object {
        private val defaultAddress = "4, Example Road, EG"
        private val defaultJourneyData: Map<String, Any?> =
            mapOf(
                "lookup-address" to
                    mutableMapOf(
                        "postcode" to "EG",
                        "houseNameOrNumber" to "4",
                    ),
                "select-address" to
                    mutableMapOf(
                        "address" to defaultAddress,
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

        fun default(addressService: AddressDataService) =
            JourneyDataBuilder(addressService, defaultJourneyData).withSelectedAddress(defaultAddress, 709902, 22)
    }

    fun withSelectedAddress(
        addressName: String,
        uprn: Long,
        localAuthorityIndex: Int,
    ): JourneyDataBuilder {
        whenever(mockAddressDataService.getAddressData(addressName)).thenReturn(
            AddressDataModel(
                addressName,
                LOCAL_AUTHORITIES[localAuthorityIndex].custodianCode,
                uprn = uprn,
            ),
        )
        journeyData["select-address"] =
            mutableMapOf(
                "address" to addressName,
            )
        return this
    }

    fun withManualAddress(
        manualAddressMap: MutableMap<String, String>,
        localAuthorityIndex: Int,
    ): JourneyDataBuilder {
        journeyData["select-address"] = mutableMapOf("address" to "MANUAL")
        journeyData["manual-address"] = manualAddressMap
        journeyData["local-authority"] =
            mutableMapOf("localAuthorityCustodianCode" to LOCAL_AUTHORITIES[localAuthorityIndex].custodianCode)
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
            LicensingType.SELECTIVE_LICENCE -> journeyData["selective-licence"] = mutableMapOf("licenceNumber" to licenseNumber)
            LicensingType.HMO_MANDATORY_LICENCE -> journeyData["hmo-mandatory-licence"] = mutableMapOf("licenceNumber" to licenseNumber)
            LicensingType.HMO_ADDITIONAL_LICENCE -> journeyData["hmo-additional-licence"] = mutableMapOf("licenceNumber" to licenseNumber)
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
        journeyData[ "number-of-people" ] =
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
}
