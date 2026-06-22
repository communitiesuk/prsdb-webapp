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
import uk.gov.communities.prsdb.webapp.constants.NO_LONGER_A_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.NoLongerALandlordController.Companion.NO_LONGER_A_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.NoLongerALandlordJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.stepConfig.ConfirmStep
import uk.gov.communities.prsdb.webapp.services.NoLongerALandlordService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(NO_LONGER_A_LANDLORD_ROUTE)
class NoLongerALandlordController(
    private val noLongerALandlordJourneyFactory: NoLongerALandlordJourneyFactory,
    private val noLongerALandlordService: NoLongerALandlordService,
) {
    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(JOINT_LANDLORDS)
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        principal: Principal,
    ): ModelAndView {
        noLongerALandlordService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId, principal.name)

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
        noLongerALandlordService.getPropertyOwnershipIfUserCanLeave(propertyOwnershipId, principal.name)

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
        val leftProperties = noLongerALandlordService.getLeftPropertyOwnershipsFromSession()
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

        return "noLongerALandlordConfirmation"
    }

    private fun getJourneySteps(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): Map<String, StepLifecycleOrchestrator> = noLongerALandlordJourneyFactory.createJourneySteps(propertyOwnershipId, baseUserId)

    private fun initializeAndRedirect(
        propertyOwnershipId: Long,
        stepName: String,
    ): ModelAndView {
        val journeyId = noLongerALandlordJourneyFactory.initializeJourneyState(propertyOwnershipId)
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
        return ModelAndView("redirect:$redirectUrl")
    }

    companion object {
        const val NO_LONGER_A_LANDLORD_ROUTE = "/$LANDLORD_PATH_SEGMENT/$NO_LONGER_A_LANDLORD_JOURNEY_URL/{propertyOwnershipId}"

        fun getNoLongerALandlordBasePath(propertyOwnershipId: Long): String =
            UriTemplate(NO_LONGER_A_LANDLORD_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getNoLongerALandlordPath(propertyOwnershipId: Long): String =
            "${getNoLongerALandlordBasePath(propertyOwnershipId)}/${ConfirmStep.ROUTE_SEGMENT}"
    }
}
