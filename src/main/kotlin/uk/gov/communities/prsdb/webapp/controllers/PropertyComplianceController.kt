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
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.NRLA_UK_REGULATIONS_URL
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_COMPLIANCE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.PROPERTY_COMPLIANCE_ROUTE
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.helpers.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.discardRemainingFields
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.FileUploader
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
            .create(propertyOwnershipId)
            .getModelAndViewForTaskList()
    }

    @GetMapping("/{stepName}")
    fun getJourneyStep(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val stepModelAndView =
            propertyComplianceJourneyFactory
                .create(propertyOwnershipId)
                .getModelAndViewForStep(stepName, subpage)

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
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        // We must ensure that we can distinguish between a metadata-only file upload and a normal file upload when
        // postJourneyData() is used for a file upload endpoint.
        val annotatedFormData = formData + (UploadCertificateFormModel::isMetadataOnly.name to true)

        return propertyComplianceJourneyFactory
            .create(propertyOwnershipId)
            .completeStep(stepName, annotatedFormData, subpage, principal)
    }

    @PostMapping("/{stepName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam(value = "subpage", required = false) subpage: Int?,
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
            .create(propertyOwnershipId)
            .completeStep(
                stepName,
                formData,
                subpage,
                principal,
            )
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
        const val PROPERTY_COMPLIANCE_ROUTE = "/$PROPERTY_COMPLIANCE_PATH_SEGMENT/{propertyOwnershipId}"

        private const val PROPERTY_COMPLIANCE_TASK_LIST_ROUTE = "$PROPERTY_COMPLIANCE_ROUTE/$TASK_LIST_PATH_SEGMENT"

        fun getPropertyCompliancePath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getPropertyComplianceTaskListPath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_TASK_LIST_ROUTE).expand(propertyOwnershipId).toASCIIString()

        const val FILE_UPLOAD_COOKIE_NAME = "file-upload-cookie"
    }
}
