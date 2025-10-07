package uk.gov.communities.prsdb.webapp.forms.newJourneys

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.FooExampleJourney

@PreAuthorize("hasRole('LANDLORD')")
@RequestMapping("new-journey")
@PrsdbRestController
class NewJourneyController(
    private val fooExampleJourney: FooExampleJourney,
) {
    @GetMapping("{propertyId}/{stepName}")
    fun getBackwardsDslJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyId") propertyId: Long,
    ): ModelAndView = fooExampleJourney.getStepModelAndView(stepName, propertyId)

    @PostMapping("{propertyId}/{stepName}")
    fun postBackwardsDslJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyId") propertyId: Long,
        @RequestParam formData: PageData,
    ): ModelAndView = fooExampleJourney.postStepModelAndView(stepName, formData, propertyId)
}
