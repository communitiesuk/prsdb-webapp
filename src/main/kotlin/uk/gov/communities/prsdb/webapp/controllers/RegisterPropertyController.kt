package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationConfirmationService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_REGISTRATION_ROUTE)
class RegisterPropertyController(
    private val propertyRegistrationJourneyFactory: PropertyRegistrationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyRegistrationConfirmationService: PropertyRegistrationConfirmationService,
    private val certificateUploadHelper: CertificateUploadHelper,
) {
    @GetMapping
    fun index(model: Model): String {
        model.addAttribute(
            "registerPropertyInitialStep",
            "$PROPERTY_REGISTRATION_ROUTE/$TASK_LIST_PATH_SEGMENT",
        )
        model.addAttribute("backUrl", LANDLORD_DASHBOARD_URL)

        return "registerPropertyStartPage"
    }

    @GetMapping("/$RESUME_PAGE_PATH_SEGMENT")
    fun getResume(
        @RequestParam(value = CONTEXT_ID_URL_PARAMETER, required = true) journeyId: String,
    ): String {
        val redirectUrl = JourneyStateService.urlWithJourneyState(TASK_LIST_PATH_SEGMENT, journeyId)
        return "redirect:$redirectUrl"
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(model: Model): String {
        val propertyRegistrationNumber =
            propertyRegistrationConfirmationService.getLastPrnRegisteredThisSession()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No registered property was found in the session")

        val propertyOwnership =
            propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No property ownership with registration number $propertyRegistrationNumber was found in the database",
                )

        model.addAttribute("singleLineAddress", propertyOwnership.address.singleLineAddress)
        model.addAttribute(
            "prn",
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
        )
        model.addAttribute("isOccupied", propertyOwnership.isOccupied)
        model.addAttribute("propertyComplianceUrl", PropertyComplianceController.getPropertyCompliancePath(propertyOwnership.id))
        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)

        return "registerPropertyConfirmation"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable("stepName") stepName: String,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = propertyRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.getStepModelAndView()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyRegistrationJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView {
        val annotatedFormData = CertificateUploadHelper.annotateFormDataForMetadataOnlyFileUpload(formData)

        return postProcessedJourneyData(stepName, annotatedFormData, principal)
    }

    @PostMapping("/{stepName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadJourneyData(
        @PathVariable("stepName") stepName: String,
        @RequestParam(JourneyIdProvider.PARAMETER_NAME) journeyId: String,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) token: String,
        principal: Principal,
        request: HttpServletRequest,
    ): ModelAndView {
        val formData =
            certificateUploadHelper.uploadFileAndReturnFormModel(
                PropertyComplianceJourneyHelper.getCertFilename(journeyId, stepName),
                fileInputIterator,
                token,
                request,
            )

        return postProcessedJourneyData(stepName, formData, principal)
    }

    private fun postProcessedJourneyData(
        stepName: String,
        formData: FormData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = propertyRegistrationJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyRegistrationJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    companion object {
        const val PROPERTY_REGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$REGISTER_PROPERTY_JOURNEY_URL"
        const val RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE =
            "$PROPERTY_REGISTRATION_ROUTE/$RESUME_PAGE_PATH_SEGMENT" +
                "?$CONTEXT_ID_URL_PARAMETER={contextId}"

        fun getResumePropertyRegistrationPath(journeyId: String): String =
            UriTemplate(RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE).expand(journeyId).toASCIIString()
    }
}
