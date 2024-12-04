package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Controller
@RequestMapping("/example-search")
class ExampleSearchController(
    val landlordService: LandlordService,
) {
    @PostMapping("/landlords")
    @ResponseBody
    fun submitSearch(
        @RequestParam searchTerm: String = "",
    ): Any {
        val registrationNumber = RegistrationNumberDataModel.parseOrNull(searchTerm)
        return if (registrationNumber != null && registrationNumber.isType(RegistrationNumberType.LANDLORD)) {
            val landlord = landlordService.retrieveLandlordByRegNum(registrationNumber)
            if (landlord != null) listOf(landlord) else listOf()
        } else {
            mapOf<String, Any>()
        }
    }
}
