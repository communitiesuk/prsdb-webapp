package uk.gov.communities.prsdb.webapp.services

import org.json.JSONObject
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient

@Service
class OSPlacesAddressLookupService(
    val osPlacesClient: OSPlacesClient,
) : AddressLookupService {
    override fun searchByPostcode(postcode: String): List<String> {
        val response = osPlacesClient.searchByPostcode(postcode)
        val results = JSONObject(response).getJSONArray("results")
        val addresses = mutableListOf<String>()
        for (i in 0 until results.length()) {
            addresses.add(results.getJSONObject(i).getJSONObject("LPI").getString("ADDRESS"))
        }
        return addresses
    }
}
