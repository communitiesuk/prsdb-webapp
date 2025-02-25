package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class LandlordSearchResultViewModel(
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
            LandlordSearchResultViewModel(
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
