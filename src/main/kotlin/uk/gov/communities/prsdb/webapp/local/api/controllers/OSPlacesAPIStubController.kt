package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("local-mock-os-places")
@RestController
@RequestMapping("/os-places-local")
class OSPlacesAPIStubController {
    @GetMapping("/find")
    fun lookupAddressesByPostcode(): String =
        "{'results':[" +
            "{'DPA':{'ADDRESS':'1, Example Road, EG','POSTCODE':'EG','BUILDING_NUMBER':1}}," +
            "{'DPA':{'ADDRESS':'Main Building, Example Road, EG','POSTCODE':'EG','BUILDING_NAME':'Main Building'}}," +
            "{'DPA':{'ADDRESS':'PO1, Example Road, EG','POSTCODE':'EG','PO_BOX_NUMBER':'PO1'}}," +
            "]}"
}
