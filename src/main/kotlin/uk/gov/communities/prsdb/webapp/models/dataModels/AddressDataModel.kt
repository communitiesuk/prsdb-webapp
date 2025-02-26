package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.Address

@Serializable
data class AddressDataModel(
    val singleLineAddress: String,
    val localAuthorityId: Int? = null,
    val uprn: Long? = null,
    val organisation: String? = null,
    val subBuilding: String? = null,
    val buildingName: String? = null,
    val buildingNumber: String? = null,
    val streetName: String? = null,
    val locality: String? = null,
    val townName: String? = null,
    val postcode: String? = null,
) {
    companion object {
        fun fromManualAddressData(
            addressLineOne: String,
            townOrCity: String,
            postcode: String,
            addressLineTwo: String? = null,
            county: String? = null,
            localAuthorityId: Int? = null,
        ): AddressDataModel =
            AddressDataModel(
                singleLineAddress =
                    manualAddressDataToSingleLineAddress(addressLineOne, townOrCity, postcode, addressLineTwo, county),
                townName = townOrCity,
                postcode = postcode,
                localAuthorityId = localAuthorityId,
            )

        fun manualAddressDataToSingleLineAddress(
            addressLineOne: String,
            townOrCity: String,
            postcode: String,
            addressLineTwo: String? = null,
            county: String? = null,
        ) = listOf(addressLineOne, addressLineTwo, townOrCity, county, postcode)
            .filterNot { it.isNullOrBlank() }
            .joinToString(", ")

        fun fromAddress(address: Address) =
            AddressDataModel(
                singleLineAddress = address.singleLineAddress,
                localAuthorityId = address.localAuthority?.id,
                uprn = address.uprn,
                organisation = address.organisation,
                subBuilding = address.subBuilding,
                buildingName = address.buildingName,
                buildingNumber = address.buildingNumber,
                streetName = address.streetName,
                locality = address.locality,
                townName = address.townName,
                postcode = address.postcode,
            )
    }
}
