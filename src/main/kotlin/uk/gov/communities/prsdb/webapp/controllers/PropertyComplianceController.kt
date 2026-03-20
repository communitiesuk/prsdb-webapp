package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
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
import uk.gov.communities.prsdb.webapp.constants.ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CHECK_GAS_SAFE_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_TO_COMPLIANCE_CONFIRMATION_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_FORM_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_FORM_URL
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_LATER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.FIRE_SAFETY_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL
import uk.gov.communities.prsdb.webapp.constants.HOMES_ACT_2018_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSES_IN_MULTIPLE_OCCUPATION_URL
import uk.gov.communities.prsdb.webapp.constants.HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL
import uk.gov.communities.prsdb.webapp.constants.HOW_TO_RENT_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.KEEP_PROPERTY_SAFE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_RESPONSIBILITIES_URL
import uk.gov.communities.prsdb.webapp.constants.LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES
import uk.gov.communities.prsdb.webapp.constants.MEES_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PRS_EXEMPTION_URL
import uk.gov.communities.prsdb.webapp.constants.RESPONSIBILITY_TO_TENANTS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.PROPERTY_COMPLIANCE_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.GiveFeedbackLaterEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_COMPLIANCE_ROUTE)
class PropertyComplianceController(
    private val propertyComplianceJourneyFactory: PropertyComplianceJourneyFactory,
    private val certificateUploadHelper: CertificateUploadHelper,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyComplianceService: PropertyComplianceService,
    private val emailSender: EmailNotificationService<GiveFeedbackLaterEmail>,
    private val landlordService: LandlordService,
    private val session: HttpSession,
) {
    @GetMapping
    fun index(
        model: Model,
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        model.addAttribute("findEpcUrl", FIND_EPC_URL)
        model.addAttribute("landlordResponsibilitiesUrl", LANDLORD_RESPONSIBILITIES_URL)
        model.addAttribute(
            "taskListUrl",
            "${getPropertyCompliancePath(propertyOwnershipId)}/$TASK_LIST_PATH_SEGMENT",
        )
        return "propertyComplianceStartPage"
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        val userShouldSeeFeedbackPage = getCurrentUserShouldSeeFeedbackPages(principal)
        val modelAndView =
            try {
                val journeyMap = propertyComplianceJourneyFactory.createJourneySteps(propertyOwnershipId, userShouldSeeFeedbackPage)
                journeyMap[stepName]?.getStepModelAndView()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
            } catch (_: NoSuchJourneyException) {
                val journeyId = propertyComplianceJourneyFactory.initializeJourneyState(principal)
                val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
                ModelAndView("redirect:$redirectUrl")
            }

        certificateUploadHelper.addCookieIfStepIsFileUploadStep(stepName, request, response, FILE_UPLOAD_COOKIE_NAME)

        return modelAndView
    }

    @PostMapping("/{stepName}")
    fun postJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val annotatedFormData = CertificateUploadHelper.annotateFormDataForMetadataOnlyFileUpload(formData)

        return postProcessedJourneyData(stepName, propertyOwnershipId, annotatedFormData, principal)
    }

    @PostMapping("/{stepName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) token: String,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val formData =
            certificateUploadHelper.uploadFileAndReturnFormModel(
                propertyOwnershipId,
                stepName,
                fileInputIterator,
                token,
                request,
                response,
                FILE_UPLOAD_COOKIE_NAME,
            )

        return postProcessedJourneyData(stepName, propertyOwnershipId, formData, principal)
    }

    @GetMapping("/$FEEDBACK_LATER_PATH_SEGMENT")
    fun sendFeedbackLater(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        throwErrorIfPropertyWasNotAddedThisSession(propertyOwnershipId)

        val landlord = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId).primaryLandlord

        emailSender.sendEmail(landlord.email, GiveFeedbackLaterEmail())
        session.setAttribute(LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES, false)
        landlordService.setHasRespondedToFeedback(landlord)

        return "redirect:$CONFIRMATION_PATH_SEGMENT"
    }

    @GetMapping("/$FEEDBACK_FORM_SEGMENT")
    fun getFeedbackForm(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        throwErrorIfPropertyWasNotAddedThisSession(propertyOwnershipId)

        session.setAttribute(LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES, false)
        landlordService.setHasRespondedToFeedback(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId).primaryLandlord)
        return "redirect:$FEEDBACK_FORM_URL"
    }

    @GetMapping("/$CONTINUE_TO_COMPLIANCE_CONFIRMATION_SEGMENT")
    fun getContinueToComplianceConfirmation(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        throwErrorIfPropertyWasNotAddedThisSession(propertyOwnershipId)

        session.setAttribute(LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES, false)
        landlordService.setHasRespondedToFeedback(propertyOwnershipService.getPropertyOwnership(propertyOwnershipId).primaryLandlord)
        return "redirect:$CONFIRMATION_PATH_SEGMENT"
    }

    @GetMapping("/$FEEDBACK_PATH_SEGMENT")
    fun getPostComplianceFeedback(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        model: Model,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        throwErrorIfPropertyWasNotAddedThisSession(propertyOwnershipId)

        model.addAttribute("completeFeedbackLaterUrl", FEEDBACK_LATER_PATH_SEGMENT)
        model.addAttribute("startSurveyUrl", FEEDBACK_FORM_SEGMENT)
        model.addAttribute("continueToComplianceUrl", CONTINUE_TO_COMPLIANCE_CONFIRMATION_SEGMENT)

        return "postComplianceFeedback"
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        model: Model,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        throwErrorIfPropertyWasNotAddedThisSession(propertyOwnershipId)

        val propertyCompliance =
            propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)
                ?: throw ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No property compliance found for property ownership $propertyOwnershipId",
                )

        val confirmationMessageKeys = PropertyComplianceConfirmationMessageKeys(propertyCompliance)

        model.addAttribute("propertyAddress", propertyCompliance.propertyOwnership.address.singleLineAddress)
        model.addAttribute("confirmationMessageKeys", confirmationMessageKeys)
        model.addAttribute("gasSafeRegisterUrl", CHECK_GAS_SAFE_REGISTER_URL)
        model.addAttribute("electricalSafetyStandardsUrl", ELECTRICAL_SAFETY_STANDARDS_URL)
        model.addAttribute("getNewEpcUrl", GET_NEW_EPC_URL)
        model.addAttribute("registerMeesExemptionUrl", REGISTER_PRS_EXEMPTION_URL)
        model.addAttribute("meesUrl", MEES_EXEMPTION_GUIDE_URL)
        model.addAttribute("findEpcUrl", FIND_EPC_URL)
        model.addAttribute("addComplianceUrl", COMPLIANCE_ACTIONS_URL)
        model.addAttribute("dashboardUrl", LANDLORD_DASHBOARD_URL)

        return if (confirmationMessageKeys.nonCompliantMsgKeys.isEmpty()) {
            "fullyCompliantPropertyConfirmation"
        } else {
            "partiallyCompliantPropertyConfirmation"
        }
    }

    @GetMapping("/$REVIEW_PATH_SEGMENT/$FIRE_SAFETY_PATH_SEGMENT")
    fun getFireSafetyReview(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        model: Model,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        return if (propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId) == null) {
            "redirect:${PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)}"
        } else {
            val propertyComplianceUrl = PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)
            model.addAttribute("backUrl", propertyComplianceUrl)
            model.addAttribute("housesInMultipleOccupationUrl", HOUSES_IN_MULTIPLE_OCCUPATION_URL)
            model.addAttribute("propertyComplianceUrl", propertyComplianceUrl)
            "forms/fireSafetyReview"
        }
    }

    @GetMapping("/$REVIEW_PATH_SEGMENT/$KEEP_PROPERTY_SAFE_PATH_SEGMENT")
    fun getKeepPropertySafeReview(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        model: Model,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        return if (propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId) == null) {
            "redirect:${PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)}"
        } else {
            val propertyComplianceUrl = PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)
            model.addAttribute("backUrl", propertyComplianceUrl)
            model.addAttribute("housingHealthAndSafetyRatingSystemUrl", HOUSING_HEALTH_AND_SAFETY_RATING_SYSTEM_URL)
            model.addAttribute("homesAct2018Url", HOMES_ACT_2018_URL)
            model.addAttribute("propertyComplianceUrl", propertyComplianceUrl)
            "forms/keepPropertySafeReview"
        }
    }

    @GetMapping("/$REVIEW_PATH_SEGMENT/$RESPONSIBILITY_TO_TENANTS_PATH_SEGMENT")
    fun getResponsibilityToTenantsReview(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        model: Model,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        return if (propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId) == null) {
            "redirect:${PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)}"
        } else {
            val propertyComplianceUrl = PropertyDetailsController.getPropertyCompliancePath(propertyOwnershipId)
            model.addAttribute("backUrl", propertyComplianceUrl)
            model.addAttribute("landlordResponsibilitiesUrl", LANDLORD_RESPONSIBILITIES_URL)
            model.addAttribute("governmentApprovedDepositProtectionSchemeUrl", GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL)
            model.addAttribute("howToRentGuideUrl", HOW_TO_RENT_GUIDE_URL)
            model.addAttribute("propertyComplianceUrl", propertyComplianceUrl)
            "forms/responsibilityToTenantsReview"
        }
    }

    private fun postProcessedJourneyData(
        stepName: String,
        propertyOwnershipId: Long,
        formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap =
                propertyComplianceJourneyFactory
                    .createJourneySteps(propertyOwnershipId, getCurrentUserShouldSeeFeedbackPages(principal))
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyComplianceJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }

    private fun throwErrorIfPropertyWasNotAddedThisSession(propertyOwnershipId: Long) {
        if (!propertyComplianceService.wasPropertyComplianceAddedThisSession(propertyOwnershipId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No property compliance was added for property ownership $propertyOwnershipId in this session",
            )
        }
    }

    private fun throwErrorIfUserIsNotAuthorized(
        baseUserId: String,
        propertyOwnershipId: Long,
    ) {
        if (!propertyOwnershipService.getIsPrimaryLandlord(propertyOwnershipId, baseUserId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User $baseUserId is not authorized to provide compliance for property ownership $propertyOwnershipId",
            )
        }
    }

    private fun getCurrentUserShouldSeeFeedbackPages(principal: Principal): Boolean {
        val userShouldSeeFeebackSessionValue = session.getAttribute(LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES) as Boolean?
        if (userShouldSeeFeebackSessionValue != null) return userShouldSeeFeebackSessionValue

        val userShouldSeeFeebackDatabaseValue = landlordService.getLandlordUserShouldSeeFeedbackPages(principal.name)
        session.setAttribute(LOGGED_IN_LANDLORD_SHOULD_SEE_FEEDBACK_PAGES, userShouldSeeFeebackDatabaseValue)
        return userShouldSeeFeebackDatabaseValue
    }

    companion object {
        const val PROPERTY_COMPLIANCE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT/{propertyOwnershipId}"

        const val FILE_UPLOAD_COOKIE_NAME = "file-upload-cookie"

        private const val PROPERTY_COMPLIANCE_TASK_LIST_ROUTE = "$PROPERTY_COMPLIANCE_ROUTE/$TASK_LIST_PATH_SEGMENT"

        private const val REVIEW_PATH_SEGMENT = "review"

        fun getPropertyCompliancePath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getPropertyComplianceTaskListPath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_TASK_LIST_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getReviewPropertyComplianceStepPath(
            propertyOwnershipId: Long,
            stepName: String,
        ): String = "${getPropertyCompliancePath(propertyOwnershipId)}/$REVIEW_PATH_SEGMENT/$stepName"

        fun getPropertyComplianceConfirmationPath(propertyOwnershipId: Long): String =
            UriTemplate("$PROPERTY_COMPLIANCE_ROUTE/$CONFIRMATION_PATH_SEGMENT").expand(propertyOwnershipId).toASCIIString()

        fun getPropertyComplianceFeedbackPath(propertyOwnershipId: Long): String =
            UriTemplate("$PROPERTY_COMPLIANCE_ROUTE/$FEEDBACK_PATH_SEGMENT").expand(propertyOwnershipId).toASCIIString()
    }
}
