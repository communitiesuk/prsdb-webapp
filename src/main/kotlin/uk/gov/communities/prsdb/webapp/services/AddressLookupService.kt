package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

interface AddressLookupService {
    fun search(
        buildingNameOrNumber: String,
        postcode: String,
    ): List<AddressDataModel>
}
