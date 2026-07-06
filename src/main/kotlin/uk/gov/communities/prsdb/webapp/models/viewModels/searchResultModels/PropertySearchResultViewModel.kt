package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class PropertySearchResultViewModel(
    val id: Long,
    val address: String,
    val registrationNumber: String,
    val localCouncil: String?,
    val recordLink: String,
) {
    companion object {
        fun fromPropertyOwnership(
            propertyOwnership: PropertyOwnership,
            currentUrlKey: Int? = null,
        ) = PropertySearchResultViewModel(
            id = propertyOwnership.id,
            address = propertyOwnership.address.singleLineAddress,
            registrationNumber =
                RegistrationNumberDataModel
                    .fromRegistrationNumber(propertyOwnership.registrationNumber)
                    .toString(),
            localCouncil =
                propertyOwnership.address.localCouncil
                    ?.name,
            recordLink =
                PropertyDetailsController
                    .getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true)
                    .overrideBackLinkForUrl(currentUrlKey),
        )
    }
}
