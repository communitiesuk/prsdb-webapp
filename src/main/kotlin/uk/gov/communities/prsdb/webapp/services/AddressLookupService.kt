package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

interface AddressLookupService {
    fun search(
        houseNameOrNumber: String,
        postcode: String,
        restrictToEngland: Boolean = false,
    ): List<AddressDataModel>
}
