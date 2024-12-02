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
        houseNameOrNumber: String,
        postcode: String,
    ): List<AddressDataModel> = responseToAddressList(osPlacesClient.search(houseNameOrNumber, postcode))

    private fun responseToAddressList(response: String): List<AddressDataModel> {
        val jsonResponse = JSONObject(response)

        if (!jsonResponse.has("results")) {
            return emptyList()
        }

        val results = jsonResponse.getJSONArray("results")
        val addresses = mutableListOf<AddressDataModel>()
        for (i in 0 until results.length()) {
            val dataset = results.getJSONObject(i).getJSONObject("DPA")
            addresses.add(
                AddressDataModel(
                    singleLineAddress = dataset.getString("ADDRESS"),
                    custodianCode = dataset.getInt("LOCAL_CUSTODIAN_CODE").toString(),
                    uprn = if (dataset.getString("UPRN").isEmpty()) null else dataset.getString("UPRN").toInt(),
                    organisation = dataset.optString("ORGANISATION_NAME", null),
                    subBuilding = dataset.optString("SUB_BUILDING_NAME", null),
                    buildingName = dataset.optString("BUILDING_NAME", null),
                    buildingNumber =
                        if (dataset.has("BUILDING_NUMBER")) {
                            dataset.getInt("BUILDING_NUMBER").toString()
                        } else {
                            null
                        },
                    streetName = dataset.optString("THOROUGHFARE_NAME", null),
                    locality = dataset.optString("DEPENDENT_LOCALITY", null),
                    townName = dataset.optString("POST_TOWN", null),
                    postcode = dataset.optString("POSTCODE", null),
                ),
            )
        }
        return addresses
    }
}
