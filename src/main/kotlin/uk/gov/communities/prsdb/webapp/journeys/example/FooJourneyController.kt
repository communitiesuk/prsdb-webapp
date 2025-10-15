package uk.gov.communities.prsdb.webapp.journeys.example

import org.springframework.beans.factory.ObjectFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.PageData

@Controller
@RequestMapping("new-journey")
class FooJourneyController(
    val factory: ObjectFactory<FooExampleJourney>,
) {
    @GetMapping("{propertyId}/{stepName}")
    fun getStep(
        @PathVariable("propertyId") propertyId: Long,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView {
        val journey = factory.getObject()
        journey.journeyStateInitialisation(propertyId)
        return journey.buildJourneySteps(propertyId.toString())[stepName]?.getStepModelAndView() ?: throw Exception("Step not found")
    }

    @PostMapping("{propertyId}/{stepName}")
    fun postStep(
        @PathVariable("propertyId") propertyId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView =
        factory.getObject().buildJourneySteps(propertyId.toString())[stepName]?.postStepModelAndView(formData)
            ?: throw Exception("Step not found")
}
