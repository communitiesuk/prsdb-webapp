package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

data class LandlordUpdateModel(
    val email: String?,
    val name: String?,
    val phoneNumber: String?,
    val address: AddressDataModel?,
)
