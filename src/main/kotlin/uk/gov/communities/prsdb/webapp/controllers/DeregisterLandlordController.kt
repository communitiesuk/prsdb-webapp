package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.LandlordDeregistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LandlordDeregistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel.Companion.USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import java.security.Principal

@Controller
@RequestMapping("/$DEREGISTER_LANDLORD_JOURNEY_URL")
class DeregisterLandlordController(
    private val landlordDeregistrationJourneyFactory: LandlordDeregistrationJourneyFactory,
    private val landlordService: LandlordService,
    private val journeyDataServiceFactory: JourneyDataServiceFactory,
) {
    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/${CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT}")
    fun checkForRegisteredProperties(
        principal: Principal,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
    ): ModelAndView {
        val formData =
            mutableMapOf(
                USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY
                    to landlordService.getLandlordHasRegisteredProperties(principal.name),
            )

        return landlordDeregistrationJourneyFactory
            .create()
            .completeStep(
                DeregisterLandlordStepId.CheckForUserProperties.urlPathSegment,
                formData,
                subpage,
                principal,
            )
    }

    @PreAuthorize("hasRole('LANDLORD')")
    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        landlordDeregistrationJourneyFactory
            .create()
            .getModelAndViewForStep(
                stepName,
                subpage,
            )

    @PreAuthorize("hasRole('LANDLORD')")
    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): ModelAndView =
        landlordDeregistrationJourneyFactory
            .create()
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
            )

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
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

        val landlordHadRegisteredProperties =
            journeyDataServiceFactory
                .create(DEREGISTER_LANDLORD_JOURNEY_URL)
                .getJourneyDataFromSession()
                .getLandlordUserHasRegisteredProperties()!!

        if (landlordHadRegisteredProperties) {
            // TODO PRSD-707: create this template
            return "deregisterLandlordWithRegisteredPropertiesConfirmation"
        }
        return "deregisterLandlordWithNoPropertiesConfirmation"
    }

    companion object {
        const val CHECK_FOR_REGISTERED_PROPERTIES_PATH_SEGMENT = "check-user-properties"

        val LANDLORD_DEREGISTRATION_PATH = "/$DEREGISTER_LANDLORD_JOURNEY_URL/${LandlordDeregistrationJourney.initialStepId.urlPathSegment}"
    }
}
