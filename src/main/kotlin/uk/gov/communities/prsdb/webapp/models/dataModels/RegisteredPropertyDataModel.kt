package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.LocalAuthorityDataHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter

data class RegisteredPropertyDataModel(
    val address: String,
    val registrationNumber: String,
    val localAuthorityName: String,
    val propertyLicence: String,
    val isTenantedMessageKey: String,
) {
    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership): RegisteredPropertyDataModel =
            RegisteredPropertyDataModel(
                address = propertyOwnership.property.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(
                            propertyOwnership.registrationNumber,
                        ).toString(),
                localAuthorityName =
                    LocalAuthorityDataHelper
                        .getLocalAuthorityDisplayName(
                            propertyOwnership.property.address.custodianCode,
                        ),
                propertyLicence = getLicenceTypeDisplayName(propertyOwnership.license),
                isTenantedMessageKey = MessageKeyConverter.convert(propertyOwnership.currentNumTenants > 0),
            )

        // TODO PRSD-785 use MessageKeyConverter here and remove display names on LicensingType
        private fun getLicenceTypeDisplayName(licence: License?): String {
            val licenceType = licence?.licenseType ?: LicensingType.NO_LICENSING
            return licenceType.displayName
        }
    }
}
