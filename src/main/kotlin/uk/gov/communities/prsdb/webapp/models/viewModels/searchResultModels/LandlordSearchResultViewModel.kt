package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
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
    var recordLink: String? = null,
) {
    companion object {
        fun fromLandlordWithListedPropertyCount(
            landlordWithListedPropertyCount: LandlordWithListedPropertyCount,
            currentUrlKey: Int? = null,
        ) = LandlordSearchResultViewModel(
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
            recordLink = getRecordLink(landlordWithListedPropertyCount.landlord.id, currentUrlKey),
        )

        private fun getRecordLink(
            landlordId: Long,
            currentUrlKey: Int?,
        ): String =
            "${LandlordDetailsController.LANDLORD_DETAILS_ROUTE}/$landlordId" +
                (currentUrlKey?.let { "?withBackUrl=$it" } ?: "")
    }
}
