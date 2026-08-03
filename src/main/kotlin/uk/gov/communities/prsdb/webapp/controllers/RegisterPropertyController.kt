package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import kotlinx.datetime.toJavaLocalDate
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
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID_URL_PARAMETER
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_SURVEY_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.RESUME_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.helpers.CertificateFilenameHelper
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.helpers.CompleteByDateHelper
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationConfirmationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal
import java.time.format.DateTimeFormatter
import java.util.Locale

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_REGISTRATION_ROUTE)
class RegisterPropertyController(
    private val propertyRegistrationJourneyFactory: PropertyRegistrationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyRegistrationConfirmationService: PropertyRegistrationConfirmationService,
    private val certificateUploadHelper: CertificateUploadHelper,
    private val propertyComplianceService: PropertyComplianceService,
    private val backUrlStorageService: BackUrlStorageService,
    private val userToLandlordService: UserToLandlordService,
) {
    @GetMapping
    fun index(model: Model): String {
        val backUrlKey = backUrlStorageService.storeCurrentUrlReturningKey()
        model.addAttribute(
            "registerPropertyInitialStep",
            "$PROPERTY_REGISTRATION_ROUTE/$TASK_LIST_PATH_SEGMENT".overrideBackLinkForUrl(backUrlKey),
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
    fun getConfirmation(
        model: Model,
        principal: Principal,
    ): String {
        val propertyRegistrationNumber =
            propertyRegistrationConfirmationService.getLastPrnRegisteredThisSession()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No registered property was found in the session")

        val propertyOwnership =
            propertyOwnershipService.retrievePropertyOwnership(propertyRegistrationNumber)
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No property ownership with registration number $propertyRegistrationNumber was found in the database",
                )

        model.addAttribute("addressParts", propertyOwnership.address.toMultiLineAddress().split("\n"))
        model.addAttribute(
            "prn",
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
        )

        val actionRequiredForCompliance =
            if (propertyOwnership.isOccupied) {
                val compliance = propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnership.id)
                compliance == null || compliance.isGasSafetyCertMissing || compliance.isElectricalSafetyMissing || compliance.epcHasFaults
            } else {
                false
            }
        model.addAttribute("actionRequiredForCompliance", actionRequiredForCompliance)

        if (actionRequiredForCompliance) {
            val completeByDate =
                CompleteByDateHelper.getIncompletePropertyCompleteByDateFromCreatedDate(propertyOwnership.createdDate)
            val formattedDate =
                completeByDate.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK))
            model.addAttribute("completeByDate", formattedDate)
        }

        val landlord = userToLandlordService.getCurrentLandlordForUser()
        val propertyCount = propertyOwnershipService.getPropertyCountForLandlord(landlord)
        if (propertyCount == 0L) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Landlord ${principal.name} has no property ownerships at confirmation",
            )
        }
        model.addAttribute("propertyRegistrationSurveyUrl", PROPERTY_REGISTRATION_SURVEY_URL)
        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)

        return "registerPropertyConfirmation"
    }

    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        principal: Principal,
    ): ModelAndView = dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }

    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @RequestParam formData: FormData,
        principal: Principal,
    ): ModelAndView {
        val annotatedFormData = CertificateUploadHelper.annotateFormDataForMetadataOnlyFileUpload(formData)

        return dispatchJourneyStep(stepPath, principal) { postStepModelAndView(annotatedFormData) }
    }

    @PostMapping("/{*stepPath}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadJourneyData(
        @PathVariable stepPath: String,
        @RequestParam(JourneyIdProvider.PARAMETER_NAME) journeyId: String,
        @RequestParam(CollectionKeyParameterService.PARAMETER_NAME) memberId: String?,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) token: String,
        principal: Principal,
        request: HttpServletRequest,
    ): ModelAndView {
        val stepName = stepPath.trimStart('/')
        val formData =
            certificateUploadHelper.uploadFileAndReturnFormModel(
                CertificateFilenameHelper.getCertFilename(journeyId, stepName, memberId),
                fileInputIterator,
                token,
                request,
            )

        return dispatchJourneyStep(stepPath, principal) { postStepModelAndView(formData) }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        principal: Principal,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { propertyRegistrationJourneyFactory.createJourneySteps() },
            initialiseJourney = { propertyRegistrationJourneyFactory.initializeJourneyState(principal) },
            dispatch = dispatch,
        )

    companion object {
        const val PROPERTY_REGISTRATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$REGISTER_PROPERTY_JOURNEY_URL"
        const val RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE =
            "$PROPERTY_REGISTRATION_ROUTE/$RESUME_PAGE_PATH_SEGMENT" +
                "?$CONTEXT_ID_URL_PARAMETER={contextId}"

        fun getResumePropertyRegistrationPath(journeyId: String): String =
            UriTemplate(RESUME_PROPERTY_REGISTRATION_JOURNEY_ROUTE).expand(journeyId).toASCIIString()
    }
}
