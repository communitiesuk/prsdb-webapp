package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount

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
        fun fromLandlordWithListedPropertyCount(landlordWithListedPropertyCount: LandlordWithListedPropertyCount) =
            LandlordSearchResultDataModel(
                id = landlordWithListedPropertyCount.landlord.id,
                name = landlordWithListedPropertyCount.landlord.name,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(landlordWithListedPropertyCount.landlord.registrationNumber)
                        .toString(),
                contactAddress = landlordWithListedPropertyCount.landlord.address.singleLineAddress,
                email = landlordWithListedPropertyCount.landlord.email,
                phoneNumber = landlordWithListedPropertyCount.landlord.phoneNumber,
                listedPropertyCount = landlordWithListedPropertyCount.listedPropertyCount,
            )
    }
}
