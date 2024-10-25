package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.services.AddressLookupService

/* TODO PRSD-371: Remove this template once there is another way to reach the AddressLookupService
 * This template is for an example page to demo our integration with OS Places and should be removed once
 * there is an integration that belongs to an intended releasable feature.
 */
@Controller
@RequestMapping("/lookup-an-address")
class ExampleAddressLookupController(
    val addressLookupService: AddressLookupService,
) {
    @GetMapping
    fun exampleAddressLookupPage(model: Model): String {
        model.addAttribute("contentHeader", "Address Lookup")
        model.addAttribute("title", "Address Lookup")
        return "exampleLookupAddress"
    }

    class Submission(
        val postcode: String,
    )

    @PostMapping(consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun lookupAddress(
        model: Model,
        body: Submission,
    ): String {
        val addresses = addressLookupService.searchByPostcode(body.postcode)
        val limitedAddresses = if (addresses.size > 5) addresses.subList(0, 5) else addresses

        model.addAttribute("contentHeader", "Address Lookup")
        model.addAttribute("title", "Address Lookup")
        model.addAttribute("addresses", limitedAddresses)
        return "exampleLookupAddress"
    }
}
