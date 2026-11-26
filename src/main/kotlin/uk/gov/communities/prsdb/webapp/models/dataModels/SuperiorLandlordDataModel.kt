package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord

@Serializable
data class SuperiorLandlordDataModel(
    // Null until the superior landlord has been saved. Lets an update journey tell a new superior
    // landlord from an existing one, so that existing rows are updated in place and only new superior
    // landlords are contacted.
    val databaseId: Long? = null,
    val detailsKnown: Boolean,
    val name: String? = null,
    val email: String? = null,
    val address: AddressDataModel? = null,
    val addressInUk: Boolean? = null,
    val addressSearchPostcode: String? = null,
    val addressSearchHouseNameOrNumber: String? = null,
    val selectedAddress: String? = null,
    val manualAddressLineOne: String? = null,
    val manualAddressLineTwo: String? = null,
    val manualTownOrCity: String? = null,
    val manualCounty: String? = null,
    val manualPostcode: String? = null,
    val manualCountry: String? = null,
) {
    // We must post to a superior landlord with no email, but we are not allowed to store their address. So a named
    // superior landlord loaded from the database will have neither, while one added in the current journey will
    // still have the address they entered.
    fun hasUnstoredPostalAddress() = detailsKnown && email == null && address == null

    companion object {
        fun fromSuperiorLandlord(superiorLandlord: SuperiorLandlord) =
            SuperiorLandlordDataModel(
                databaseId = superiorLandlord.id,
                detailsKnown = superiorLandlord.detailsKnown(),
                name = superiorLandlord.name,
                email = superiorLandlord.email,
            )
    }
}
