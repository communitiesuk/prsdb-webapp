package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee.UpdateLeadTrusteeJourneyFactory
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_LEAD_TRUSTEE_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateLeadTrusteeController(
    private val journeyFactory: UpdateLeadTrusteeJourneyFactory,
    private val userToLandlordService: UserToLandlordService,
) {
    @GetMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(ORGANISATION_LANDLORD_REGISTRATION)
    fun getUpdateStep(
        principal: Principal,
        @PathVariable stepPath: String,
    ): ModelAndView {
        checkUserIsOrganisationLandlordTrust()
        return dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }
    }

    @PostMapping("/{*stepPath}")
    @AvailableWhenFeatureEnabled(ORGANISATION_LANDLORD_REGISTRATION)
    fun postUpdateStep(
        principal: Principal,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView {
        checkUserIsOrganisationLandlordTrust()
        return dispatchJourneyStep(stepPath, principal) { postStepModelAndView(formData) }
    }

    private fun checkUserIsOrganisationLandlordTrust() {
        val landlord = userToLandlordService.getCurrentLandlordForUser()
        if (landlord !is OrganisationalLandlord || !landlord.isTrust) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only organisation landlord trusts can update their lead trustee")
        }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { journeyFactory.createJourneySteps() },
            initialiseJourney = { journeyFactory.initializeJourneyState(principal) },
            dispatch = dispatch,
        )

    companion object {
        const val UPDATE_LEAD_TRUSTEE_PATH_SEGMENT = "update-lead-trustee"
        const val UPDATE_LEAD_TRUSTEE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/$UPDATE_LEAD_TRUSTEE_PATH_SEGMENT"
    }
}
