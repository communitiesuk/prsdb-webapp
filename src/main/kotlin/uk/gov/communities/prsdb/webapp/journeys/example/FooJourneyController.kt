package uk.gov.communities.prsdb.webapp.journeys.example

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import java.security.Principal

@PrsdbController
@RequestMapping("new-journey")
class FooJourneyController(
    val journeyFactory: NewPropertyRegistrationJourneyFactory,
) {
    @GetMapping("{stepName}")
    fun getStep(
        @PathVariable("stepName") stepName: String,
        principal: Principal,
    ): ModelAndView =
        try {
            println("Getting step $stepName")
            val journeyMap = journeyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = journeyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    @PostMapping("{stepName}")
    fun postStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            println("Posting step $stepName with data $formData")
            journeyFactory.createJourneySteps()[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = PropertyRegistrationJourneyState.generateJourneyId(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
}
