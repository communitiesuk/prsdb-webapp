package uk.gov.communities.prsdb.webapp.examples

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient

@Controller
@RequestMapping("example/epc")
class ExampleEpcController(
    private val client: EpcRegisterClient,
) {
    @GetMapping
    @ResponseBody
    fun getGoodExample(): String = client.getByRrn("0000-0000-0000-0554-8410")

    @GetMapping("uprn/{uprn}")
    @ResponseBody
    fun getEpcByUprn(
        @PathVariable uprn: Long,
    ): String = client.getByUprn(uprn)

    @GetMapping("rrn/{rrn}")
    @ResponseBody
    fun getEpcByRrn(
        @PathVariable rrn: String,
    ): String = client.getByRrn(rrn)
}
