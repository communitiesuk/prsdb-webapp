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
        restrictToEngland: Boolean,
    ) = responseToAddressList(osPlacesClient.search(houseNameOrNumber, postcode, restrictToEngland))

    private fun responseToAddressList(response: String): List<AddressDataModel> {
        val jsonResponse = JSONObject(response)

        if (!jsonResponse.has("results")) {
            return emptyList()
        }

        val results = jsonResponse.getJSONArray("results")
        val addresses = mutableListOf<AddressDataModel>()
        for (i in 0 until results.length()) {
            val addressJSON = results.getJSONObject(i).getJSONObject("DPA")
            addresses.add(addressJSON.toAddressDataModel())
        }
        return addresses
    }

    private fun JSONObject.toAddressDataModel(): AddressDataModel =
        AddressDataModel(
            singleLineAddress = this.getString("ADDRESS"),
            localAuthorityId = this.getLocalAuthorityId(),
            uprn = if (this.getString("UPRN").isEmpty()) null else this.getString("UPRN").toLong(),
            organisation = this.optString("ORGANISATION_NAME", null),
            subBuilding = this.optString("SUB_BUILDING_NAME", null),
            buildingName = this.optString("BUILDING_NAME", null),
            buildingNumber =
                if (this.has("BUILDING_NUMBER")) {
                    this.getInt("BUILDING_NUMBER").toString()
                } else {
                    null
                },
            streetName = this.optString("THOROUGHFARE_NAME", null),
            locality = this.optString("DEPENDENT_LOCALITY", null),
            townName = this.optString("POST_TOWN", null),
            postcode = this.optString("POSTCODE", null),
        )

    private fun JSONObject.getLocalAuthorityId(): Int? {
        // We only store English local authorities in the database
        if (!this.isEnglandAddress()) {
            return null
        }

        val custodianCode = this.getInt("LOCAL_CUSTODIAN_CODE").toString()
        val localAuthorityId = localAuthorityService.retrieveLocalAuthorityByCustodianCode(custodianCode)?.id

        if (localAuthorityId == null) {
            println("No local council found for $custodianCode retrieved from OSPlaces")
        }

        return localAuthorityId
    }

    private fun JSONObject.isEnglandAddress(): Boolean = this.getString("COUNTRY_CODE") == "E"
}
