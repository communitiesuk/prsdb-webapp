package uk.gov.communities.prsdb.webapp.controllers

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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LEAVE_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController.Companion.LEAVE_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.leaveProperty.LeavePropertyJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.leaveProperty.stepConfig.ConfirmStep
import uk.gov.communities.prsdb.webapp.services.LeavePropertyService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(LEAVE_PROPERTY_ROUTE)
class LeavePropertyController(
    private val leavePropertyJourneyFactory: LeavePropertyJourneyFactory,
    private val leavePropertyService: LeavePropertyService,
) {
    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        principal: Principal,
    ): ModelAndView {
        leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId, principal.name)

        return try {
            val journeyMap = getJourneySteps(propertyOwnershipId, principal.name)
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        } catch (_: PropertyOwnershipMismatchException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        }
    }

    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView {
        leavePropertyService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId, principal.name)

        return try {
            val journeyMap = getJourneySteps(propertyOwnershipId, principal.name)
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        } catch (_: PropertyOwnershipMismatchException) {
            initializeAndRedirect(propertyOwnershipId, stepName)
        }
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun getConfirmation(
        model: Model,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
    ): String {
        val leftProperties = leavePropertyService.getLeftPropertyOwnershipsFromSession()
        if (propertyOwnershipId !in leftProperties) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "PropertyOwnershipId $propertyOwnershipId was not found in the list of properties left in the session",
            )
        }

        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)
        model.addAttribute(
            "address",
            leftProperties[propertyOwnershipId],
        )

        return "leavePropertyConfirmation"
    }

    private fun getJourneySteps(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): Map<String, StepLifecycleOrchestrator> = leavePropertyJourneyFactory.createJourneySteps(propertyOwnershipId, baseUserId)

    private fun initializeAndRedirect(
        propertyOwnershipId: Long,
        stepName: String,
    ): ModelAndView {
        val journeyId = leavePropertyJourneyFactory.initializeJourneyState(propertyOwnershipId)
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
        return ModelAndView("redirect:$redirectUrl")
    }

    companion object {
        const val LEAVE_PROPERTY_ROUTE = "/$LANDLORD_PATH_SEGMENT/$LEAVE_PROPERTY_JOURNEY_URL/{propertyOwnershipId}"

        fun getLeavePropertyBasePath(propertyOwnershipId: Long): String =
            UriTemplate(LEAVE_PROPERTY_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getLeavePropertyPath(propertyOwnershipId: Long): String =
            "${getLeavePropertyBasePath(propertyOwnershipId)}/${ConfirmStep.ROUTE_SEGMENT}"
    }
}
