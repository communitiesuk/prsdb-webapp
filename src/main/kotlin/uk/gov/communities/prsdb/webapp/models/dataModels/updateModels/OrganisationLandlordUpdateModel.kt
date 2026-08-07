package uk.gov.communities.prsdb.webapp.models.dataModels.updateModels

import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

data class OrganisationLandlordUpdateModel(
    val name: String? = null,
    val address: AddressDataModel? = null,
)
