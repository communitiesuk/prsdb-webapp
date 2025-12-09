package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class LandlordSearchResultViewModel(
    val id: Long,
    val name: String,
    val registrationNumber: String,
    val contactAddress: String,
    val email: String,
    val phoneNumber: String,
    val recordLink: String,
    val propertyCount: Int,
) {
    companion object {
        fun fromLandlord(
            landlord: Landlord,
            currentUrlKey: Int? = null,
        ) = LandlordSearchResultViewModel(
            id = landlord.id,
            name = landlord.name,
            registrationNumber =
                RegistrationNumberDataModel
                    .fromRegistrationNumber(landlord.registrationNumber)
                    .toString(),
            contactAddress = landlord.address.singleLineAddress,
            email = landlord.email,
            phoneNumber = landlord.phoneNumber,
            propertyCount = landlord.propertyOwnerships.count { it.isActive },
            recordLink =
                LandlordDetailsController
                    .getLandlordDetailsForLocalCouncilUserPath(landlord.id)
                    .overrideBackLinkForUrl(currentUrlKey),
        )
    }
}
