package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Service
class AddressDataService(
    private val session: HttpSession,
    private val journeyDataService: JourneyDataService,
) {
    fun getAddressData(singleLineAddress: String): AddressDataModel? {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val addressData = journeyData["address-data"] as String?
        if (addressData == null) {
            return null
        } else {
            return Json.decodeFromString<Map<String, AddressDataModel>>(addressData)[singleLineAddress]
        }
    }

    fun setAddressData(addressDataList: List<AddressDataModel>) {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        journeyData["address-data"] = Json.encodeToString(addressDataList.associateBy { it.singleLineAddress })
        journeyDataService.setJourneyData(journeyData)
    }

    fun getCachedAddressRegisteredResult(uprn: Long): Boolean? {
        val cachedResults = objectToStringKeyedMap(session.getAttribute("addressRegisteredResults")) ?: mutableMapOf()
        return cachedResults[uprn.toString()].toString().toBooleanStrictOrNull()
    }

    fun setCachedAddressRegisteredResult(
        uprn: Long,
        result: Boolean,
    ) {
        val cachedResults =
            objectToStringKeyedMap(session.getAttribute("addressRegisteredResults")) ?: mutableMapOf()

        cachedResults[uprn.toString()] = result

        session.setAttribute("addressRegisteredResults", cachedResults)
    }
}
