package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.apache.commons.io.FilenameUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.Validator
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
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.CHECK_GAS_SAFE_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_TO_COMPLIANCE_CONFIRMATION_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_FORM_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_FORM_URL
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_LATER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FEEDBACK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
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
import uk.gov.communities.prsdb.webapp.constants.PRIVATE_RENTING_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PRS_EXEMPTION_URL
import uk.gov.communities.prsdb.webapp.constants.RESPONSIBILITY_TO_TENANTS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REVIEW_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.RIGHT_TO_RENT_CHECKS_URL
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.COMPLIANCE_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.PROPERTY_COMPLIANCE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.discardRemainingFields
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.GiveFeedbackLaterEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService
import java.security.Principal

@PrsdbController
@PreAuthorize("hasRole('LANDLORD')")
@RequestMapping(PROPERTY_COMPLIANCE_ROUTE)
class PropertyComplianceController(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val tokenCookieService: TokenCookieService,
    private val uploadService: UploadService,
    private val propertyComplianceJourneyFactory: PropertyComplianceJourneyFactory,
    private val propertyComplianceUpdateJourneyFactory: PropertyComplianceUpdateJourneyFactory,
    private val validator: Validator,
    private val propertyComplianceService: PropertyComplianceService,
    private val emailSender: EmailNotificationService<GiveFeedbackLaterEmail>,
    private val landlordService: LandlordService,
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
        model.addAttribute("taskListUrl", "${getPropertyCompliancePath(propertyOwnershipId)}/$TASK_LIST_PATH_SEGMENT")
        return "propertyComplianceStartPage"
    }

    @GetMapping("/$TASK_LIST_PATH_SEGMENT")
    fun getTaskList(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        return propertyComplianceJourneyFactory
            .create(TASK_LIST_PATH_SEGMENT, propertyOwnershipId)
            .getModelAndViewForTaskList()
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val stepModelAndView =
            propertyComplianceJourneyFactory
                .create(stepName, propertyOwnershipId, checkingAnswersForStep)
                .getModelAndViewForStep(stepName, subpage, checkingAnswersForStep = checkingAnswersForStep)

        addCookieIfStepIsFileUploadStep(stepName, request, response)

        return stepModelAndView
    }

    @PostMapping("/{stepName}", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val annotatedFormData = annotateFormDataForMetadataOnlyFileUpload(formData)

        return propertyComplianceJourneyFactory
            .create(stepName, propertyOwnershipId, checkingAnswersForStep)
            .completeStep(stepName, annotatedFormData, subpage, principal, checkingAnswersForStep)
    }

    @PostMapping("/{stepName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) token: String,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val formData =
            uploadFileAndReturnFormModel(
                propertyOwnershipId,
                stepName,
                fileInputIterator,
                token,
                request,
                response,
            )

        return propertyComplianceJourneyFactory
            .create(stepName, propertyOwnershipId, checkingAnswersForStep)
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
                checkingAnswersForStep,
            )
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

        model.addAttribute("propertyAddress", propertyCompliance.propertyOwnership.property.address.singleLineAddress)
        model.addAttribute("confirmationMessageKeys", confirmationMessageKeys)
        model.addAttribute("gasSafeRegisterUrl", CHECK_GAS_SAFE_REGISTER_URL)
        model.addAttribute("rcpElectricalInfoUrl", RCP_ELECTRICAL_INFO_URL)
        model.addAttribute("rcpElectricalRegisterUrl", RCP_ELECTRICAL_REGISTER_URL)
        model.addAttribute("electricalSafetyStandardsUrl", ELECTRICAL_SAFETY_STANDARDS_URL)
        model.addAttribute("getNewEpcUrl", GET_NEW_EPC_URL)
        model.addAttribute("registerMeesExemptionUrl", REGISTER_PRS_EXEMPTION_URL)
        model.addAttribute("addComplianceUrl", COMPLIANCE_ACTIONS_URL)
        model.addAttribute("dashboardUrl", LANDLORD_DASHBOARD_URL)

        return if (confirmationMessageKeys.nonCompliantMsgKeys.isEmpty()) {
            "fullyCompliantPropertyConfirmation"
        } else {
            "partiallyCompliantPropertyConfirmation"
        }
    }

    @GetMapping("/$UPDATE_PATH_SEGMENT/{stepName}")
    fun getUpdateJourneyStep(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val stepModelAndView =
            propertyComplianceUpdateJourneyFactory
                .create(stepName, propertyOwnershipId, checkingAnswersForStep)
                .getModelAndViewForStep(checkingAnswersForStep = checkingAnswersForStep)

        addCookieIfStepIsFileUploadStep(stepName, request, response)

        return stepModelAndView
    }

    @PostMapping("/$UPDATE_PATH_SEGMENT/{stepName}", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postUpdateJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val annotatedFormData = annotateFormDataForMetadataOnlyFileUpload(formData)

        return propertyComplianceUpdateJourneyFactory
            .create(stepName, propertyOwnershipId, checkingAnswersForStep)
            .completeStep(annotatedFormData, principal, checkingAnswersForStep)
    }

    @PostMapping("/$UPDATE_PATH_SEGMENT/{stepName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadUpdateJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = CHECKING_ANSWERS_FOR_PARAMETER_NAME, required = false) checkingAnswersForStep: String? = null,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) token: String,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val formData =
            uploadFileAndReturnFormModel(
                propertyOwnershipId,
                stepName,
                fileInputIterator,
                token,
                request,
                response,
            )

        return propertyComplianceUpdateJourneyFactory
            .create(stepName, propertyOwnershipId, checkingAnswersForStep)
            .completeStep(formData, principal, checkingAnswersForStep)
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
            model.addAttribute("privateRentingGuideUrl", PRIVATE_RENTING_GUIDE_URL)
            model.addAttribute("rightToRentChecksUrl", RIGHT_TO_RENT_CHECKS_URL)
            model.addAttribute("governmentApprovedDepositProtectionSchemeUrl", GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL)
            model.addAttribute("howToRentGuideUrl", HOW_TO_RENT_GUIDE_URL)
            model.addAttribute("propertyComplianceUrl", propertyComplianceUrl)
            "forms/responsibilityToTenantsReview"
        }
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

    private fun uploadFileAndReturnFormModel(
        propertyOwnershipId: Long,
        stepName: String,
        fileInputIterator: FileItemInputIterator,
        token: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): PageData {
        if (tokenCookieService.isTokenForCookieValue(token, request.requestURI)) {
            tokenCookieService.useToken(token)
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        }

        val file =
            fileInputIterator.getFirstFileField()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid multipart file upload request")

        val fileUploadId =
            if (isFileValid(file, request.contentLengthLong)) {
                val uploadFileName = PropertyComplianceJourneyHelper.getCertFilename(propertyOwnershipId, stepName)
                uploadFile(uploadFileName, file, request.contentLengthLong)?.id
            } else {
                null
            }

        fileInputIterator.discardRemainingFields()

        if (fileUploadId == null) {
            val cookie = tokenCookieService.createCookieForValue(FILE_UPLOAD_COOKIE_NAME, request.requestURI)
            response.addCookie(cookie)
        }

        return UploadCertificateFormModel
            .fromUploadedFile(
                file,
                request.contentLengthLong,
                fileUploadId,
            ).toPageData()
    }

    private fun isFileValid(
        file: FileItemInput,
        fileLength: Long,
    ): Boolean {
        val fileFormModel = UploadCertificateFormModel.fromUploadedFileMetadata(file, fileLength)
        return !validator.validateObject(fileFormModel).hasErrors()
    }

    private fun uploadFile(
        uploadFileName: String,
        file: FileItemInput,
        fileLength: Long,
    ): FileUpload? =
        uploadService.uploadFile(
            uploadFileName,
            file.inputStream.withMaxLength(fileLength),
            FilenameUtils.getExtension(file.name),
        )

    private fun addCookieIfStepIsFileUploadStep(
        stepName: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        if (stepName.contains(FILE_UPLOAD_URL_SUBSTRING)) {
            val cookie = tokenCookieService.createCookieForValue(FILE_UPLOAD_COOKIE_NAME, request.requestURI)
            response.addCookie(cookie)
        }
    }

    private fun annotateFormDataForMetadataOnlyFileUpload(formData: PageData): PageData {
        // We must ensure that we can distinguish between a metadata-only file upload and a normal file upload when
        // postJourneyData() is used for a file upload endpoint.
        return formData + (UploadCertificateFormModel::isUserSubmittedMetadataOnly.name to true)
    }

    companion object {
        const val PROPERTY_COMPLIANCE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT/{propertyOwnershipId}"

        private const val UPDATE_PROPERTY_COMPLIANCE_ROUTE = "$PROPERTY_COMPLIANCE_ROUTE/$UPDATE_PATH_SEGMENT"

        private const val PROPERTY_COMPLIANCE_TASK_LIST_ROUTE = "$PROPERTY_COMPLIANCE_ROUTE/$TASK_LIST_PATH_SEGMENT"

        fun getPropertyCompliancePath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getPropertyComplianceTaskListPath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_TASK_LIST_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getUpdatePropertyComplianceBasePath(propertyOwnershipId: Long): String =
            UriTemplate(UPDATE_PROPERTY_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getUpdatePropertyComplianceStepPath(
            propertyOwnershipId: Long,
            stepId: PropertyComplianceStepId,
        ): String = "${getUpdatePropertyComplianceBasePath(propertyOwnershipId)}/${stepId.urlPathSegment}"

        fun getReviewPropertyComplianceStepPath(
            propertyOwnershipId: Long,
            stepId: PropertyComplianceStepId,
        ): String = "${getPropertyCompliancePath(propertyOwnershipId)}/$REVIEW_PATH_SEGMENT/${stepId.urlPathSegment}"

        const val FILE_UPLOAD_COOKIE_NAME = "file-upload-cookie"
    }
}
