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
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.BackwardsDslJourneyDelegate

@PreAuthorize("hasRole('LANDLORD')")
@RequestMapping("new-journey")
@PrsdbRestController
class NewJourneyController(
    private val backwardsDslJourneyDelegate: BackwardsDslJourneyDelegate,
) {
    @GetMapping("{propertyId}/backwards-dsl/{stepName}")
    fun getBackwardsDslJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyId") propertyId: Long,
    ): ModelAndView = backwardsDslJourneyDelegate.getStepModelAndView(stepName, propertyId)

    @PostMapping("{propertyId}/backwards-dsl/{stepName}")
    fun postBackwardsDslJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyId") propertyId: Long,
        @RequestParam formData: PageData,
    ): ModelAndView = backwardsDslJourneyDelegate.postStepModelAndView(stepName, formData, propertyId)
}

enum class Complete {
    COMPLETE,
}

enum class YesOrNo {
    YES,
    NO,
}

enum class EpcStatus {
    AUTOMATCHED,
    NOT_AUTOMATCHED,
    NO_EPC,
}

enum class EpcSearchResult {
    FOUND,
    SUPERSEDED,
    NOT_FOUND,
}
