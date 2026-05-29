package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class PropertySearchResultViewModel(
    val id: Long,
    val address: String,
    val registrationNumber: String,
    val localCouncil: String?,
    val landlord: PropertySearchResultLandlordViewModel,
    val recordLink: String,
) {
    companion object {
        fun fromPropertyOwnership(
            propertyOwnership: PropertyOwnership,
            currentUrlKey: Int? = null,
        ) = PropertySearchResultViewModel(
            id = propertyOwnership.id,
            address = propertyOwnership.propertyDetails.address.singleLineAddress,
            registrationNumber =
                RegistrationNumberDataModel
                    .fromRegistrationNumber(propertyOwnership.landlordship.registrationNumber)
                    .toString(),
            localCouncil =
                propertyOwnership.propertyDetails.address.localCouncil
                    ?.name,
            landlord =
                PropertySearchResultLandlordViewModel(
                    id = propertyOwnership.landlordship.primaryLandlord.id,
                    name = propertyOwnership.landlordship.primaryLandlord.name,
                    recordLink =
                        LandlordDetailsController
                            .getLandlordDetailsForLocalCouncilUserPath(propertyOwnership.landlordship.primaryLandlord.id)
                            .overrideBackLinkForUrl(currentUrlKey),
                ),
            recordLink =
                PropertyDetailsController
                    .getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true)
                    .overrideBackLinkForUrl(currentUrlKey),
        )
    }
}

data class PropertySearchResultLandlordViewModel(
    val id: Long,
    val name: String,
    val recordLink: String,
)
