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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity.UpdateOrganisationCharityJourneyFactory
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PrsdbController
@RequestMapping(UPDATE_ORG_CHARITY_ROUTE)
@PreAuthorize("hasRole('LANDLORD')")
class UpdateOrganisationLandlordCharityController(
    private val journeyFactory: UpdateOrganisationCharityJourneyFactory,
    private val userToLandlordService: UserToLandlordService,
) {
    @GetMapping("/{*stepPath}")
    fun getUpdateStep(
        principal: Principal,
        @PathVariable stepPath: String,
    ): ModelAndView {
        checkUserIsOrganisationLandlord()
        return dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }
    }

    @PostMapping("/{*stepPath}")
    fun postUpdateStep(
        principal: Principal,
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
    ): ModelAndView {
        checkUserIsOrganisationLandlord()
        return dispatchJourneyStep(stepPath, principal) { postStepModelAndView(formData) }
    }

    private fun checkUserIsOrganisationLandlord() {
        val landlord = userToLandlordService.getCurrentLandlordForUser()
        if (landlord !is OrganisationalLandlord) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only organisation landlords can update their charity registration details",
            )
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
        const val UPDATE_ORG_CHARITY_PATH_SEGMENT = "update-organisation-charity"
        const val UPDATE_ORG_CHARITY_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LANDLORD_DETAILS_PATH_SEGMENT/$UPDATE_ORG_CHARITY_PATH_SEGMENT"
    }
}
