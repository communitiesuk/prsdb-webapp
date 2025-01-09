package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_OWNERSHIP_ID
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@Controller
@RequestMapping("/$REGISTER_PROPERTY_JOURNEY_URL")
class RegisterPropertyController(
    private val propertyRegistrationJourney: PropertyRegistrationJourney,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val session: HttpSession,
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute(
            "registerPropertyInitialStep",
            "/$REGISTER_PROPERTY_JOURNEY_URL/${propertyRegistrationJourney.initialStepId.urlPathSegment}",
        )
        model.addAttribute("backUrl", "/")

        return "registerPropertyStartPage"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        model: Model,
    ): String =
        propertyRegistrationJourney.populateModelAndGetViewName(
            propertyRegistrationJourney.getStepId(stepName),
            model,
            subpage,
        )

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam formData: PageData,
        model: Model,
        principal: Principal,
    ): String =
        propertyRegistrationJourney.updateJourneyDataAndGetViewNameOrRedirect(
            propertyRegistrationJourney.getStepId(stepName),
            formData,
            model,
            subpage,
            principal,
        )

    @GetMapping("/$CONFIRMATION_PAGE_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        val propertyOwnershipID =
            session.getAttribute(PROPERTY_OWNERSHIP_ID)?.toString()?.toLong()
                ?: throw AccessDeniedException("$PROPERTY_OWNERSHIP_ID was not found in the session")
        val propertyOwnership =
            propertyOwnershipService.retrievePropertyOwnership(propertyOwnershipID)
                ?: throw AccessDeniedException("No property ownership with id $propertyOwnershipID was found in the database")

        model.addAttribute("singleLineAddress", propertyOwnership.property.address.singleLineAddress)
        model.addAttribute(
            "prn",
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
        )

        return "registerPropertyConfirmation"
    }

    companion object {
        const val CONFIRMATION_PAGE_PATH_SEGMENT = "confirmation"
    }
}
