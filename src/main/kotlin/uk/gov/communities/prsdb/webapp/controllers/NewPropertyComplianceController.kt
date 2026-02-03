package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.apache.commons.io.FilenameUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_PROPERTY_COMPLIANCE
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.NewPropertyComplianceController.Companion.PROPERTY_COMPLIANCE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.helpers.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.helpers.PropertyComplianceJourneyHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.discardRemainingFields
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.NewPropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.UploadService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_COMPLIANCE_ROUTE)
class NewPropertyComplianceController(
    private val propertyComplianceJourneyFactory: NewPropertyComplianceJourneyFactory,
    private val validator: Validator,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val tokenCookieService: TokenCookieService,
    private val uploadService: UploadService,
) {
    @GetMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_COMPLIANCE)
    fun getJourneyStep(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        principal: Principal,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)
        val modelAndView =
            try {
                val journeyMap = propertyComplianceJourneyFactory.createJourneySteps()
                journeyMap[stepName]?.getStepModelAndView()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
            } catch (_: NoSuchJourneyException) {
                val journeyId = propertyComplianceJourneyFactory.initializeJourneyState(principal)
                val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
                ModelAndView("redirect:$redirectUrl")
            }

        addCookieIfStepIsFileUploadStep(stepName, request, response)

        return modelAndView
    }

    @PostMapping("/{stepName}")
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_COMPLIANCE)
    fun postJourneyData(
        @PathVariable propertyOwnershipId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
        principal: Principal,
    ): ModelAndView {
        throwErrorIfUserIsNotAuthorized(principal.name, propertyOwnershipId)

        val annotatedFormData = annotateFormDataForMetadataOnlyFileUpload(formData)

        return postProcessedJourneyData(stepName, annotatedFormData, principal)
    }

    @PostMapping("/{stepName}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @AvailableWhenFeatureEnabled(MIGRATE_PROPERTY_COMPLIANCE)
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
            uploadFileAndReturnFormModel(
                propertyOwnershipId,
                stepName,
                fileInputIterator,
                token,
                request,
                response,
            )

        return postProcessedJourneyData(stepName, formData, principal)
    }

    private fun postProcessedJourneyData(
        stepName: String,
        formData: PageData,
        principal: Principal,
    ): ModelAndView =
        try {
            val journeyMap = propertyComplianceJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(formData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyComplianceJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
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
    }
}
