package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType

data class PropertyOwnershipUpdateModel(
    val ownershipType: OwnershipType?,
)
