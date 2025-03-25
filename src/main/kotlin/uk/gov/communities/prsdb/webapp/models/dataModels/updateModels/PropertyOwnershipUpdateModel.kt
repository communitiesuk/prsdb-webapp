package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType

data class PropertyOwnershipUpdateModel(
    val ownershipType: OwnershipType?,
    val numberOfHouseholds: Int?,
    val numberOfPeople: Int?,
    val licensingType: LicensingType?,
    val licenceNumber: String?,
)
