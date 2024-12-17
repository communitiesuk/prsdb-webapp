package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class AddressDataService(
    private val session: HttpSession,
) {
    fun getAddressData(singleLineAddress: String): AddressDataModel? =
        Json.decodeFromString<Map<String, AddressDataModel>>(
            session.getAttribute("addressData").toString(),
        )[singleLineAddress]

    fun setAddressData(addressDataList: List<AddressDataModel>) =
        session.setAttribute(
            "addressData",
            Json.encodeToString(addressDataList.associateBy { it.singleLineAddress }),
        )

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

    fun getManualAddress(
        journeyDataService: JourneyDataService,
        journeyData: JourneyData,
        manualAddressPathSegment: String,
    ): AddressDataModel? {
        val addressLineOne =
            journeyDataService.getFieldStringValue(
                journeyData,
                manualAddressPathSegment,
                "addressLineOne",
            ) ?: return null

        val townOrCity =
            journeyDataService.getFieldStringValue(
                journeyData,
                manualAddressPathSegment,
                "townOrCity",
            ) ?: return null

        val postcode =
            journeyDataService.getFieldStringValue(
                journeyData,
                manualAddressPathSegment,
                "postcode",
            ) ?: return null

        val addressLineTwo =
            journeyDataService.getFieldStringValue(
                journeyData,
                manualAddressPathSegment,
                "addressLineTwo",
            )

        val county =
            journeyDataService.getFieldStringValue(
                journeyData,
                manualAddressPathSegment,
                "county",
            )

        return AddressDataModel.fromManualAddressData(
            addressLineOne,
            townOrCity,
            postcode,
            addressLineTwo,
            county,
        )
    }
}
