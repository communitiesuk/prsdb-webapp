package uk.gov.communities.prsdb.webapp.services

import org.json.JSONObject
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Service
class OSPlacesAddressLookupService(
    val osPlacesClient: OSPlacesClient,
) : AddressLookupService {
    override fun search(
        buildingNameOrNumber: String,
        postcode: String,
    ): List<AddressDataModel> = responseToAddressList(osPlacesClient.search(buildingNameOrNumber, postcode))

    private fun responseToAddressList(response: String): List<AddressDataModel> {
        val results = JSONObject(response).getJSONArray("results")
        val addresses = mutableListOf<AddressDataModel>()
        for (i in 0 until results.length()) {
            val dataset = results.getJSONObject(i).getJSONObject("DPA")
            addresses.add(
                AddressDataModel(
                    dataset.getString("ADDRESS"),
                    dataset.getString("POSTCODE"),
                    if (dataset.has("BUILDING_NUMBER")) dataset.getInt("BUILDING_NUMBER") else null,
                    dataset.optString("BUILDING_NAME", null),
                    dataset.optString("PO_BOX_NUMBER", null),
                ),
            )
        }
        return addresses
    }
}
