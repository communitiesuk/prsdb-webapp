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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_LANDLORD_DEREGISTRATION
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.NewLandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordDeregistrationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@RequestMapping(LANDLORD_DEREGISTRATION_ROUTE)
class NewDeregisterLandlordController(
    private val landlordDeregistrationJourneyFactory: NewLandlordDeregistrationJourneyFactory,
    private val landlordService: LandlordService,
    private val landlordDeregistrationService: LandlordDeregistrationService,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_DEREGISTRATION)
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        principal: Principal,
    ): ModelAndView =
        try {
            val hasProperties = propertyOwnershipService.doesLandlordHaveRegisteredProperties(principal.name)
            val journeyMap = landlordDeregistrationJourneyFactory.createJourneySteps(hasProperties)
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(principal, stepName)
        }

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_DEREGISTRATION)
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val hasProperties = propertyOwnershipService.doesLandlordHaveRegisteredProperties(principal.name)
            val journeyMap = landlordDeregistrationJourneyFactory.createJourneySteps(hasProperties)
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            initializeAndRedirect(principal, stepName)
        }

    private fun initializeAndRedirect(
        principal: Principal,
        stepName: String,
    ): ModelAndView {
        val journeyId = landlordDeregistrationJourneyFactory.initializeJourneyState()
        val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
        return ModelAndView("redirect:$redirectUrl")
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    @AvailableWhenFeatureEnabled(MIGRATE_LANDLORD_DEREGISTRATION)
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        if (landlordService.retrieveLandlordByBaseUserId(principal.name) != null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Landlord with one-login id ${principal.name} was found in the database",
            )
        }

        val landlordHadRegisteredProperties = landlordDeregistrationService.getLandlordHadActivePropertiesFromSession()

        return if (landlordHadRegisteredProperties) {
            "deregisterLandlordWithRegisteredPropertiesConfirmation"
        } else {
            "deregisterLandlordWithNoPropertiesConfirmation"
        }
    }
}
