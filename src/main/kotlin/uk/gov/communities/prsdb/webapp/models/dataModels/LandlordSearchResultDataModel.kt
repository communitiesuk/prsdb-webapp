package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.database.entity.Landlord

data class LandlordSearchResultDataModel(
    val id: Long,
    val name: String,
    val registrationNumber: String,
    val contactAddress: String,
    val email: String,
    val phoneNumber: String,
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
    }
}
