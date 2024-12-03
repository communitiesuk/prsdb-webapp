package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
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

    fun isSelectAddressSatisfied(pageData: PageData): Boolean {
        val selectedAddress = pageData["address"].toString()
        return selectedAddress == MANUAL_ADDRESS_CHOSEN || getAddressData(selectedAddress) != null
    }
}
