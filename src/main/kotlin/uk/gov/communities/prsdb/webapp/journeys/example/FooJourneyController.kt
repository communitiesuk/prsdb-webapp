package uk.gov.communities.prsdb.webapp.journeys.example

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.forms.PageData

@PrsdbController
@RequestMapping("new-journey")
class FooJourneyController(
    val journeyFactory: FooExampleJourneyFactory,
) {
    @GetMapping("{propertyId}/{stepName}")
    fun getStep(
        @PathVariable("propertyId") propertyId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam("journeyId", required = false) journeyId: String?,
    ): ModelAndView {
        val journeyId = journeyId ?: journeyFactory.initializeJourneyState(propertyId)

        return journeyFactory.createJourneySteps(journeyId)[stepName]?.getStepModelAndView()
            ?: throw Exception("Step not found")
    }

    @PostMapping("{propertyId}/{stepName}")
    fun postStep(
        @PathVariable("propertyId") propertyId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam("journeyId", required = false) journeyId: String?,
        @RequestParam formData: PageData,
    ): ModelAndView =
        journeyFactory.createJourneySteps(propertyId.toString())[stepName]?.postStepModelAndView(formData)
            ?: throw Exception("Step not found")
}
