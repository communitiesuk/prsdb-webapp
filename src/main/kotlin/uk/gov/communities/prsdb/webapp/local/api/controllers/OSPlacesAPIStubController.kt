package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.communities.prsdb.webapp.constants.MAX_ADDRESSES
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService
import kotlin.math.min

@Profile("local-mock-os-places, !web-server-deactivated")
@RestController
@RequestMapping("/local/os-places")
class OSPlacesAPIStubController(
    private val localAuthorityService: LocalAuthorityService,
) {
    @GetMapping("/find")
    fun lookupAddressesByPostcode(
        @RequestParam query: String,
    ): String {
        try {
            val addressListSize = query.split(",")[0].toInt()
            return if (addressListSize < 1) {
                "{}"
            } else {
                (1..min(addressListSize, MAX_ADDRESSES)).joinToString(
                    ",",
                    "{'results':[",
                    "]}",
                ) {
                    "{'DPA':{'ADDRESS':'$it, Example Road, EG'," +
                        "'LOCAL_CUSTODIAN_CODE':${getCustodianCode(it)},'UPRN':'${it}123456','BUILDING_NUMBER':$it,'POSTCODE':'EG'}}"
                }
            }
        } catch (exception: Exception) {
            println(exception.message)
            return "{'results':[{'DPA':{'ADDRESS':'1, Example Road, EG'," +
                "'LOCAL_CUSTODIAN_CODE':${getCustodianCode(1)},'UPRN':'0123456','BUILDING_NUMBER':1,'POSTCODE':'EG'}}]}"
        }
    }

    private fun getCustodianCode(index: Int) = localAuthorityService.retrieveAllLocalAuthorities().let { it[index % it.size].custodianCode }
}
