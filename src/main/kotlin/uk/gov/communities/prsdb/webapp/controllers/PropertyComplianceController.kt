package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
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
import uk.gov.communities.prsdb.webapp.constants.CHANGE_ANSWER_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ELECTRICAL_SAFETY_STANDARDS_URL
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFE_REGISTER
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.NRLA_UK_REGULATIONS_URL
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_COMPLIANCE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PRS_EXEMPTION_URL
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.INCOMPLETE_COMPLIANCES_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.PROPERTY_COMPLIANCE_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.helpers.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.discardRemainingFields
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.PropertyComplianceConfirmationMessageKeys
import uk.gov.communities.prsdb.webapp.services.FileUploader
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import java.security.Principal
import kotlin.reflect.KClass

@PrsdbController
@PreAuthorize("hasRole('LANDLORD')")
@RequestMapping(PROPERTY_COMPLIANCE_ROUTE)
class PropertyComplianceController(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val tokenCookieService: TokenCookieService,
    private val fileUploader: FileUploader,
    private val propertyComplianceJourneyFactory: PropertyComplianceJourneyFactory,
    private val validator: Validator,
    private val propertyComplianceService: PropertyComplianceService,
) {
    @GetMapping
    fun index(
        model: Model,
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        model.addAttribute("nrlaUkRegulationsUrl", NRLA_UK_REGULATIONS_URL)
        model.addAttribute(
            "taskListUrl",
            "${getPropertyCompliancePath(propertyOwnershipId)}/$TASK_LIST_PATH_SEGMENT",
        )
        return "propertyComplianceStartPage"
    }

    @GetMapping("/$TASK_LIST_PATH_SEGMENT")
    fun getTaskList(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        return propertyComplianceJourneyFactory
            .create(propertyOwnershipId, isChangingAnswer = false)
            .getModelAndViewForTaskList()
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHANGE_ANSWER_FOR_PARAMETER_NAME, required = false) changingAnswerFor: String? = null,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val stepModelAndView =
            propertyComplianceJourneyFactory
                .create(propertyOwnershipId, isChangingAnswer = changingAnswerFor != null)
                .getModelAndViewForStep(stepName, subpage, changingAnswersForStep = changingAnswerFor)

        if (stepName.contains(FILE_UPLOAD_URL_SUBSTRING)) {
            val cookie = tokenCookieService.createCookieForValue(FILE_UPLOAD_COOKIE_NAME, request.requestURI)
            response.addCookie(cookie)
        }

        return stepModelAndView
    }

    @PostMapping("/{stepName}", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun postJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHANGE_ANSWER_FOR_PARAMETER_NAME, required = false) changingAnswerFor: String? = null,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        // We must ensure that we can distinguish between a metadata-only file upload and a normal file upload when
        // postJourneyData() is used for a file upload endpoint.
        val annotatedFormData = formData + (UploadCertificateFormModel::isMetadataOnly.name to true)

        return propertyComplianceJourneyFactory
            .create(propertyOwnershipId, isChangingAnswer = changingAnswerFor != null)
            .completeStep(stepName, annotatedFormData, subpage, principal, changingAnswerFor)
    }

    @PostMapping("/{stepName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        @RequestParam(value = CHANGE_ANSWER_FOR_PARAMETER_NAME, required = false) changingAnswerFor: String? = null,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) token: String,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        if (tokenCookieService.isTokenForCookieValue(token, request.requestURI)) {
            tokenCookieService.useToken(token)
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid upload token")
        }

        val file =
            fileInputIterator.getFirstFileField()
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a valid multipart file upload request")

        val formModelClass = PropertyComplianceJourneyHelper.getUploadCertificateFormModelClass(stepName)

        val isUploadSuccessfulOrNull =
            if (isFileValid(formModelClass, file, request.contentLengthLong)) {
                val uploadFileName = PropertyComplianceJourneyHelper.getCertFilename(propertyOwnershipId, stepName, file.name)
                uploadFile(uploadFileName, file, request.contentLengthLong)
            } else {
                null
            }

        fileInputIterator.discardRemainingFields()

        if (isUploadSuccessfulOrNull != true) {
            val cookie = tokenCookieService.createCookieForValue(FILE_UPLOAD_COOKIE_NAME, request.requestURI)
            response.addCookie(cookie)
        }

        val formData =
            UploadCertificateFormModel
                .fromFileItemInput(
                    formModelClass,
                    file,
                    request.contentLengthLong,
                    isUploadSuccessfulOrNull,
                ).toPageData()

        return propertyComplianceJourneyFactory
            .create(propertyOwnershipId, isChangingAnswer = changingAnswerFor != null)
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
                changingAnswerFor,
            )
    }

    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        @PathVariable propertyOwnershipId: Long,
        principal: Principal,
        model: Model,
    ): String {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        if (!propertyComplianceService.wasPropertyComplianceAddedThisSession(propertyOwnershipId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No property compliance was added for property ownership $propertyOwnershipId in this session",
            )
        }

        val propertyCompliance =
            propertyComplianceService.getComplianceForProperty(propertyOwnershipId) ?: throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No property compliance found for property ownership $propertyOwnershipId",
            )

        val confirmationMessageKeys = PropertyComplianceConfirmationMessageKeys(propertyCompliance)

        model.addAttribute("propertyAddress", propertyCompliance.propertyOwnership.property.address.singleLineAddress)
        model.addAttribute("confirmationMessageKeys", confirmationMessageKeys)
        model.addAttribute("gasSafeRegisterUrl", GAS_SAFE_REGISTER)
        model.addAttribute("rcpElectricalInfoUrl", RCP_ELECTRICAL_INFO_URL)
        model.addAttribute("rcpElectricalRegisterUrl", RCP_ELECTRICAL_REGISTER_URL)
        model.addAttribute("electricalSafetyStandardsUrl", ELECTRICAL_SAFETY_STANDARDS_URL)
        model.addAttribute("getNewEpcUrl", GET_NEW_EPC_URL)
        model.addAttribute("registerMeesExemptionUrl", REGISTER_PRS_EXEMPTION_URL)
        model.addAttribute("propertiesWithoutComplianceUrl", INCOMPLETE_COMPLIANCES_URL)
        model.addAttribute("dashboardUrl", LANDLORD_DASHBOARD_URL)

        return if (confirmationMessageKeys.nonCompliantMsgKeys.isEmpty()) {
            "fullyCompliantPropertyConfirmation"
        } else {
            "partiallyCompliantPropertyConfirmation"
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

    private fun isFileValid(
        formModelClass: KClass<out UploadCertificateFormModel>,
        file: FileItemInput,
        fileLength: Long,
    ): Boolean {
        val fileFormModel = UploadCertificateFormModel.fromFileItemInput(formModelClass, file, fileLength)
        return !validator.validateObject(fileFormModel).hasErrors()
    }

    private fun uploadFile(
        uploadFileName: String,
        file: FileItemInput,
        fileLength: Long,
    ): Boolean = fileUploader.uploadFile(uploadFileName, file.inputStream.withMaxLength(fileLength))

    companion object {
        const val PROPERTY_COMPLIANCE_ROUTE = "/$LANDLORD_PATH_SEGMENT/$PROPERTY_COMPLIANCE_PATH_SEGMENT/{propertyOwnershipId}"

        private const val PROPERTY_COMPLIANCE_TASK_LIST_ROUTE = "$PROPERTY_COMPLIANCE_ROUTE/$TASK_LIST_PATH_SEGMENT"

        fun getPropertyCompliancePath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getPropertyComplianceTaskListPath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_TASK_LIST_ROUTE).expand(propertyOwnershipId).toASCIIString()

        const val FILE_UPLOAD_COOKIE_NAME = "file-upload-cookie"
    }
}
