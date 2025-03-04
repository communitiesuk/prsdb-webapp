package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Profile("local")
@RestController
class ExampleAPIStubController {
    @GetMapping("/example-api")
    fun example(): String = "Hello World!"
}
