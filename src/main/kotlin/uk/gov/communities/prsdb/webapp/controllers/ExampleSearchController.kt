package uk.gov.communities.prsdb.webapp.controllers

import org.json.JSONObject
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Controller
@RequestMapping("/example-search")
class ExampleSearchController(
    val landlordService: LandlordService,
) {
    @GetMapping("/landlords")
    fun getSearchPage(
        model: Model,
        search: SearchWrapper,
    ): String {
        model.addAttribute("searchWrapper", SearchWrapper())

        return "examples/exampleLandlordSearch"
    }

    @PostMapping("/landlords")
    fun submitSearch(
        model: Model,
        @ModelAttribute search: SearchWrapper,
    ): String {
        val searchTerm = search.searchTerm ?: ""
        val registrationNumber = RegistrationNumberDataModel.parseOrNull(searchTerm)

        val output =
            if (registrationNumber != null && registrationNumber.isType(RegistrationNumberType.LANDLORD)) {
                val landlordOrNull = landlordService.retrieveLandlordByRegNum(registrationNumber)
                mapOf("registration number match" to summariseLandlord(landlordOrNull))
            } else if (searchTerm.length >= 5) {
                val landlordsFound = landlordService.searchForLandlords(searchTerm)
                mapOf("trigram matches" to landlordsFound.map { summariseLandlord(it) })
            } else {
                mapOf()
            }

        model.addAttribute("searchWrapper", SearchWrapper(searchTerm))
        model.addAttribute("searchResults", JSONObject(output))
        return "examples/exampleLandlordSearch"
    }

    fun summariseLandlord(landlord: Landlord?) =
        if (landlord != null) {
            mapOf(
                "name" to landlord.name,
                "email" to landlord.email,
                "phoneNumber" to landlord.phoneNumber,
                "registrationNumber" to
                    RegistrationNumberDataModel(
                        landlord.registrationNumber.type,
                        landlord.registrationNumber.number,
                    ).toString(),
            )
        } else {
            null
        }
}

class SearchWrapper(
    val searchTerm: String? = null,
)
