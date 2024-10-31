package uk.gov.communities.prsdb.webapp.local.api.controller

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Profile("local-mock-os-places")
@RestController
@RequestMapping("/os-places-local")
class OSPlacesAPIStubController {
    @GetMapping("/postcode")
    fun lookupAddressesByPostcode(
        @RequestParam postcode: String,
    ): String =
        "{'results':[" +
            "{'DPA':{'ADDRESS':'1, Example Road, $postcode','POSTCODE':'$postcode','BUILDING_NUMBER':1}}," +
            "{'DPA':{'ADDRESS':'Main Building, Example Road, $postcode','POSTCODE':'$postcode','BUILDING_NAME':'Main Building'}}," +
            "{'DPA':{'ADDRESS':'PO1, Example Road, $postcode','POSTCODE':'$postcode','PO_BOX_NUMBER':'PO1'}}," +
            "]}"
}
