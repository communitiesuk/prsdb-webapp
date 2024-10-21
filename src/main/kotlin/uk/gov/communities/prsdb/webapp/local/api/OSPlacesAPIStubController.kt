package uk.gov.communities.prsdb.webapp.local.api

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Profile("local-mock-os-places")
@RestController
class OSPlacesAPIStubController {
    @GetMapping("/postcode")
    fun lookupAddressesByPostcode(
        @RequestParam postcode: String,
    ): String =
        "{'results':[" +
            "{'DPA':{'ADDRESS':'1, Example Road, $postcode'}}," +
            "{'DPA':{'ADDRESS':'2, Example Road, $postcode'}}," +
            "{'DPA':{'ADDRESS':'3, Example Road, $postcode'}}" +
            "]}"
}
