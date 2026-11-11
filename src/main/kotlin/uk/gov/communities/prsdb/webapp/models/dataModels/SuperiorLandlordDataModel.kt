package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord

@Serializable
data class SuperiorLandlordDataModel(
    // Null until the superior landlord has been saved. Lets an update journey tell a new superior
    // landlord from an existing one, so that existing rows are updated in place and only new superior
    // landlords are contacted.
    val databaseId: Long? = null,
    // Set when the user goes through the superior landlord's details in this journey. One they never
    // started editing cannot have changed, so this stops them being written back needlessly even when
    // their address has no UPRN to compare on.
    val editedInJourney: Boolean = false,
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
        // TODO PDJB-1994: use this to pre-populate the superior landlords list from the database when
        //  the update journey starts, leaving editedInJourney false so superior landlords the user
        //  never opens are not written back.
        fun fromSuperiorLandlord(superiorLandlord: SuperiorLandlord) =
            SuperiorLandlordDataModel(
                databaseId = superiorLandlord.id,
                detailsKnown = superiorLandlord.detailsKnown(),
                name = superiorLandlord.name,
                email = superiorLandlord.email,
                address = superiorLandlord.address?.let { AddressDataModel.fromAddress(it) },
            )
    }
}
