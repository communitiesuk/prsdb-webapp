package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class RegisteredPropertyViewModel(
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
            isLaView: Boolean = false,
        ): RegisteredPropertyViewModel =
            RegisteredPropertyViewModel(
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
                recordLink = PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLaView),
            )
    }
}
