package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class RegisteredPropertyLocalCouncilViewModel(
    val address: String,
    val registrationNumber: String,
    val localAuthorityName: String,
    val licenseTypeMessageKey: String,
    val isTenantedMessageKey: String,
    val recordLink: String,
) {
    companion object {
        fun fromPropertyOwnership(
            propertyOwnership: PropertyOwnership,
            currentUrlKey: Int? = null,
        ): RegisteredPropertyLocalCouncilViewModel =
            RegisteredPropertyLocalCouncilViewModel(
                address = propertyOwnership.property.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(
                            propertyOwnership.registrationNumber,
                        ).toString(),
                localAuthorityName =
                    propertyOwnership.property.address.localAuthority!!
                        .name,
                licenseTypeMessageKey =
                    MessageKeyConverter.convert(
                        propertyOwnership.license?.licenseType ?: LicensingType.NO_LICENSING,
                    ),
                isTenantedMessageKey = MessageKeyConverter.convert(propertyOwnership.isOccupied),
                recordLink =
                    PropertyDetailsController
                        .getPropertyDetailsPath(propertyOwnership.id, isLaView = true)
                        .overrideBackLinkForUrl(currentUrlKey),
            )
    }
}

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
