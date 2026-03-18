package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
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
import uk.gov.communities.prsdb.webapp.constants.CHECKING_ANSWERS_FOR_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.PropertyComplianceUpdateJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbController
@PreAuthorize("hasRole('LANDLORD')")
@RequestMapping(PropertyComplianceController.PROPERTY_COMPLIANCE_ROUTE)
class LegacyPropertyComplianceController(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val certificateUploadHelper: CertificateUploadHelper,
    private val propertyComplianceUpdateJourneyFactory: PropertyComplianceUpdateJourneyFactory,
) {
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

        certificateUploadHelper.addCookieIfStepIsFileUploadStep(stepName, request, response, FILE_UPLOAD_COOKIE_NAME)

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

        val annotatedFormData = CertificateUploadHelper.annotateFormDataForMetadataOnlyFileUpload(formData)

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
            certificateUploadHelper.uploadFileAndReturnFormModel(
                propertyOwnershipId,
                stepName,
                fileInputIterator,
                token,
                request,
                response,
                FILE_UPLOAD_COOKIE_NAME,
            )

        return propertyComplianceUpdateJourneyFactory
            .create(stepName, propertyOwnershipId, checkingAnswersForStep)
            .completeStep(formData, principal, checkingAnswersForStep)
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

    companion object {
        private const val UPDATE_PROPERTY_COMPLIANCE_ROUTE =
            "${PropertyComplianceController.PROPERTY_COMPLIANCE_ROUTE}/$UPDATE_PATH_SEGMENT"

        // TODO PDJB-546 - new to new controller
        fun getUpdatePropertyComplianceBasePath(propertyOwnershipId: Long): String =
            UriTemplate(UPDATE_PROPERTY_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()

        // TODO PDJB-546 - new to new controller
        fun getUpdatePropertyComplianceStepPath(
            propertyOwnershipId: Long,
            stepId: PropertyComplianceStepId,
        ): String = "${getUpdatePropertyComplianceBasePath(propertyOwnershipId)}/${stepId.urlPathSegment}"
    }
}
