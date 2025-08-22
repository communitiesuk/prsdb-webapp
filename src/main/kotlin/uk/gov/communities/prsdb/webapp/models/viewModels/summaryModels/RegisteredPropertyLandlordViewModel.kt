package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class RegisteredPropertyLandlordViewModel(
    val address: String,
    val registrationNumber: String,
    val recordLink: String,
) {
    companion object {
        fun fromPropertyOwnership(
            propertyOwnership: PropertyOwnership,
            currentUrlKey: Int? = null,
        ): RegisteredPropertyLandlordViewModel =
            RegisteredPropertyLandlordViewModel(
                address = propertyOwnership.property.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(
                            propertyOwnership.registrationNumber,
                        ).toString(),
                recordLink =
                    PropertyDetailsController
                        .getPropertyDetailsPath(propertyOwnership.id, isLaView = false)
                        .overrideBackLinkForUrl(currentUrlKey),
            )
    }
}
