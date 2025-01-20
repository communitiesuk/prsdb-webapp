package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.database.entity.Landlord

data class LandlordSearchResultDataModel(
    val id: Long,
    val name: String,
    val registrationNumber: String,
    val contactAddress: String,
    val email: String,
    val phoneNumber: String,
    var listedPropertyCount: Int = 0,
) {
    companion object {
        fun fromLandlord(landlord: Landlord) =
            LandlordSearchResultDataModel(
                id = landlord.id,
                name = landlord.name,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(landlord.registrationNumber)
                        .toString(),
                contactAddress = landlord.address.singleLineAddress,
                email = landlord.email,
                phoneNumber = landlord.phoneNumber,
            )

        fun fromLandlordWithListedProperties(
            landlord: Landlord,
            listedPropertyCount: Int,
        ): LandlordSearchResultDataModel {
            val dataModel = fromLandlord(landlord)
            dataModel.listedPropertyCount = listedPropertyCount
            return dataModel
        }
    }
}
