package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.annotations.PrsdbRestController
import uk.gov.communities.prsdb.webapp.local.api.MockOSPlacesAPIResponses

@Profile("local-mock-os-places")
@PrsdbRestController
@RequestMapping("/local/os-places")
class OSPlacesAPIStubController {
    @GetMapping("/find")
    fun lookupAddressesByPostcode(
        @RequestParam query: String,
    ): String {
        try {
            val addressListSize = query.split(",")[0].toInt()
            return MockOSPlacesAPIResponses.createResponseOfSize(addressListSize)
        } catch (exception: NumberFormatException) {
            return MockOSPlacesAPIResponses.createResponseOfSize(1)
        }
    }
}
