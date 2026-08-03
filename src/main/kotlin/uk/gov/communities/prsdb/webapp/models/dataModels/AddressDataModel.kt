package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.Address

@Serializable
data class AddressDataModel(
    val singleLineAddress: String,
    val localCouncilId: Int? = null,
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
    fun toMultiLineAddress(): String =
        if (hasAddressComponents()) {
            buildMultiLineAddressFromComponents()
        } else {
            singleLineAddress.replace(", ", "\n")
        }

    private fun hasAddressComponents(): Boolean = streetName != null || buildingName != null || buildingNumber != null

    private fun buildMultiLineAddressFromComponents(): String =
        listOfNotNull(
            organisation,
            subBuilding,
            listOfNotNull(buildingNumber, streetName).joinToString(" ").ifBlank { null },
            buildingName,
            locality,
            townName,
            postcode,
        ).joinToString("\n")

    companion object {
        fun fromManualAddressData(
            addressLineOne: String,
            townOrCity: String,
            postcode: String,
            addressLineTwo: String? = null,
            county: String? = null,
            localCouncilId: Int? = null,
        ): AddressDataModel =
            AddressDataModel(
                singleLineAddress =
                    manualAddressDataToSingleLineAddress(addressLineOne, townOrCity, postcode, addressLineTwo, county),
                townName = townOrCity,
                postcode = postcode,
                localCouncilId = localCouncilId,
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
                localCouncilId = address.localCouncil?.id,
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
