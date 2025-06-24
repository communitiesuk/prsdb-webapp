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
    val localAuthority: String?,
    val landlord: PropertySearchResultLandlordViewModel,
    val recordLink: String,
) {
    companion object {
        fun fromPropertyOwnership(
            propertyOwnership: PropertyOwnership,
            currentUrlKey: Int? = null,
        ) = PropertySearchResultViewModel(
            id = propertyOwnership.id,
            address = propertyOwnership.property.address.singleLineAddress,
            registrationNumber =
                RegistrationNumberDataModel
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
            localAuthority =
                propertyOwnership.property.address.localAuthority
                    ?.name,
            landlord =
                PropertySearchResultLandlordViewModel(
                    id = propertyOwnership.primaryLandlord.id,
                    name = propertyOwnership.primaryLandlord.name,
                    recordLink =
                        LandlordDetailsController
                            .getLandlordDetailsForLaUserPath(propertyOwnership.primaryLandlord.id)
                            .overrideBackLinkForUrl(currentUrlKey),
                ),
            recordLink =
                PropertyDetailsController
                    .getPropertyDetailsPath(propertyOwnership.id, isLaView = true)
                    .overrideBackLinkForUrl(currentUrlKey),
        )
    }
}

data class PropertySearchResultLandlordViewModel(
    val id: Long,
    val name: String,
    val recordLink: String,
)
