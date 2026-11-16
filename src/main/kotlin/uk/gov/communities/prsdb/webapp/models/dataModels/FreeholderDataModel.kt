package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder

@Serializable
data class FreeholderDataModel(
    // Null until the freeholder has been saved. Lets an update journey tell a new freeholder from an
    // existing one, so that existing rows are updated in place and only new freeholders are contacted.
    val databaseId: Long? = null,
    val detailsKnown: Boolean,
    val name: String? = null,
    val email: String? = null,
    val address: AddressDataModel? = null,
    val addressSearchPostcode: String? = null,
    val addressSearchHouseNameOrNumber: String? = null,
    val selectedAddress: String? = null,
    val manualAddressLineOne: String? = null,
    val manualAddressLineTwo: String? = null,
    val manualTownOrCity: String? = null,
    val manualCounty: String? = null,
    val manualPostcode: String? = null,
) {
    companion object {
        fun fromFreeholder(freeholder: Freeholder): FreeholderDataModel {
            val address = freeholder.address?.let(AddressDataModel::fromAddress)
            val manualAddress = address?.takeIf { it.uprn == null }?.toManualAddressParts()
            return FreeholderDataModel(
                databaseId = freeholder.id,
                detailsKnown = freeholder.detailsKnown(),
                name = freeholder.name,
                email = freeholder.email,
                address = address,
                addressSearchPostcode = address?.postcode,
                addressSearchHouseNameOrNumber =
                    address?.let {
                        it.buildingNumber ?: it.buildingName ?: it.subBuilding
                    } ?: manualAddress?.lineOne,
                selectedAddress = freeholder.address?.getSelectedAddress(),
                manualAddressLineOne = manualAddress?.lineOne,
                manualAddressLineTwo = manualAddress?.lineTwo,
                manualTownOrCity = manualAddress?.townOrCity,
                manualCounty = manualAddress?.county,
                manualPostcode = manualAddress?.postcode,
            )
        }

        private fun AddressDataModel.toManualAddressParts(): ManualAddressParts {
            val components = singleLineAddress.split(",").map(String::trim).filter(String::isNotEmpty)
            val componentsWithoutPostcode =
                if (postcode != null && components.lastOrNull().equals(postcode, ignoreCase = true)) {
                    components.dropLast(1)
                } else {
                    components
                }
            val townIndex =
                townName?.let { town -> componentsWithoutPostcode.indexOfLast { it.equals(town, ignoreCase = true) } }
                    ?: -1
            val addressComponents = if (townIndex >= 0) componentsWithoutPostcode.take(townIndex) else componentsWithoutPostcode
            val countyComponents = if (townIndex >= 0) componentsWithoutPostcode.drop(townIndex + 1) else emptyList()

            return ManualAddressParts(
                lineOne = addressComponents.firstOrNull() ?: singleLineAddress,
                lineTwo = addressComponents.drop(1).joinToString(", ").ifBlank { null },
                townOrCity = townName,
                county = countyComponents.joinToString(", ").ifBlank { null },
                postcode = postcode,
            )
        }

        private data class ManualAddressParts(
            val lineOne: String?,
            val lineTwo: String?,
            val townOrCity: String?,
            val county: String?,
            val postcode: String?,
        )
    }
}
