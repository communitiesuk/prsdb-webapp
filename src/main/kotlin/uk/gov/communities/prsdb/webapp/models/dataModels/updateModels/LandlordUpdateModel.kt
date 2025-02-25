package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

data class LandlordUpdateModel(
    val email: String?,
    val fullName: String?,
    val address: AddressDataModel?,
)
