package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SWITCHED_TO_INDIVIDUAL_PROPERTY_ID
import uk.gov.communities.prsdb.webapp.constants.SWITCH_TO_INDIVIDUAL_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController.Companion.SWITCH_TO_INDIVIDUAL_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.SwitchToIndividualJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(SWITCH_TO_INDIVIDUAL_ROUTE)
class SwitchToIndividualController(
    private val switchToIndividualJourneyFactory: SwitchToIndividualJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable stepName: String,
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfUnauthorized(propertyOwnershipId, principal)

        return try {
            val journeyMap = switchToIndividualJourneyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        } catch (_: PropertyOwnershipMismatchException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        }
    }

    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable stepName: String,
        @PathVariable propertyOwnershipId: Long,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView {
        throwExceptionIfUnauthorized(propertyOwnershipId, principal)

        return try {
            val journeyMap = switchToIndividualJourneyFactory.createJourneySteps(propertyOwnershipId)
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        } catch (_: PropertyOwnershipMismatchException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        }
    }

    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getSuccess(
        model: Model,
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        session: HttpSession,
    ): String {
        throwExceptionIfUnauthorized(propertyOwnershipId, principal)

        val switchedId = session.getAttribute(SWITCHED_TO_INDIVIDUAL_PROPERTY_ID) as? Long
        if (switchedId != propertyOwnershipId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val address = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId).address.singleLineAddress
        model.addAttribute("address", address)
        model.addAttribute("propertyDetailsUrl", PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))

        return "switchToIndividualSuccess"
    }

    private fun initializeAndRedirect(
        propertyOwnershipId: Long,
        stepName: String,
    ): ModelAndView {
        val journeyId = switchToIndividualJourneyFactory.initializeJourneyState(propertyOwnershipId)
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
        return ModelAndView("redirect:$redirectUrl")
    }

    private fun throwExceptionIfUnauthorized(
        propertyOwnershipId: Long,
        principal: Principal,
    ) {
        if (!propertyOwnershipService.getIsPrimaryLandlord(propertyOwnershipId, principal.name)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    companion object {
        const val SWITCH_TO_INDIVIDUAL_ROUTE = "/$LANDLORD_PATH_SEGMENT/$SWITCH_TO_INDIVIDUAL_JOURNEY_URL/{propertyOwnershipId}"

        fun getSwitchToIndividualBasePath(propertyOwnershipId: Long): String =
            UriTemplate(SWITCH_TO_INDIVIDUAL_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getSwitchToIndividualFirstStepPath(propertyOwnershipId: Long): String =
            "${getSwitchToIndividualBasePath(propertyOwnershipId)}/${CheckPendingInvitationsStep.ROUTE_SEGMENT}"
    }
}
