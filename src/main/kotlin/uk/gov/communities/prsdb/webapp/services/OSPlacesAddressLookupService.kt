package uk.gov.communities.prsdb.webapp.services

import org.json.JSONObject
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@PrsdbWebService
class OSPlacesAddressLookupService(
    private val osPlacesClient: OSPlacesClient,
    private val localAuthorityService: LocalAuthorityService,
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
                    localAuthorityId = getLocalAuthorityId(dataset),
                    uprn = if (dataset.getString("UPRN").isEmpty()) null else dataset.getString("UPRN").toLong(),
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

    private fun getLocalAuthorityId(dataset: JSONObject): Int? {
        val custodianCode = dataset.getInt("LOCAL_CUSTODIAN_CODE").toString()
        try {
            return localAuthorityService.retrieveLocalAuthorityByCustodianCode(custodianCode)!!.id
        } catch (exception: NullPointerException) {
            println("No local authority found for $custodianCode retrieved from OSPlaces")
            return null
        }
    }
}
