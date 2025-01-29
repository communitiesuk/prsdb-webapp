package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

class PropertyDetailsViewModel(
    private val propertyOwnership: PropertyOwnership,
) {
    val address: String = propertyOwnership.property.address.singleLineAddress

    val primaryLandlordName: String = propertyOwnership.primaryLandlord.name

    val landlordTypeKey: String =
        when (propertyOwnership.landlordType) {
            LandlordType.SOLE -> "propertyDetails.keyDetails.landlordType.sole"
            LandlordType.JOINT -> "propertyDetails.keyDetails.landlordType.joint"
            LandlordType.COMPANY -> "propertyDetails.keyDetails.landlordType.company"
        }

    val isTenantedKey: String =
        if (propertyOwnership.currentNumTenants > 0) {
            "propertyDetails.keyDetails.isTenanted.true"
        } else {
            "propertyDetails.keyDetails.isTenanted.false"
        }
}
