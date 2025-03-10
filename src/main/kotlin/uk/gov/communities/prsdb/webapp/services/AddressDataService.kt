package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
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
        val newJourneyData =
            journeyData + ("address-data" to Json.encodeToString(addressDataList.associateBy { it.singleLineAddress }))
        journeyDataService.setJourneyDataInSession(newJourneyData)
    }

    fun getCachedAddressRegisteredResult(uprn: Long): Boolean? {
        val cachedResults = objectToStringKeyedMap(session.getAttribute("addressRegisteredResults")) ?: mapOf()
        return cachedResults[uprn.toString()].toString().toBooleanStrictOrNull()
    }

    fun setCachedAddressRegisteredResult(
        uprn: Long,
        result: Boolean,
    ) {
        val cachedResults = objectToStringKeyedMap(session.getAttribute("addressRegisteredResults")) ?: mapOf()

        val newCachedResult = cachedResults + (uprn.toString() to result)

        session.setAttribute("addressRegisteredResults", newCachedResult)
    }

    fun cacheSingleLineAddressForPropertyOwnershipId(
        propertyOwnershipId: Long,
        singleLineAddress: String,
    ) {
        session.setAttribute(getSingleLineAddressForPropertyOwnershipIdKey(propertyOwnershipId), singleLineAddress)
    }

    fun getCachedSingleLineAddressForPropertyOwnershipId(propertyOwnershipId: Long): String? =
        session.getAttribute(getSingleLineAddressForPropertyOwnershipIdKey(propertyOwnershipId))?.toString()

    // TODO: PRSD-697 - call this to clear the address from the cache when the property ownership is deleted
    fun clearCachedSingleLineAddressForPropertyOwnershipId(propertyOwnershipId: Long) =
        session.removeAttribute(getSingleLineAddressForPropertyOwnershipIdKey(propertyOwnershipId))

    private fun getSingleLineAddressForPropertyOwnershipIdKey(propertyOwnershipId: Long): String =
        "singleLineAddress_propertyOwnershipId_$propertyOwnershipId"
}
