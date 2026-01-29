package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.ADD_COMPLIANCE_INFORMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MIGRATE_PROPERTY_COMPLIANCE
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.NewPropertyComplianceController.Companion.PROPERTY_COMPLIANCE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.NewPropertyComplianceJourneyFactory
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import java.security.Principal

@PreAuthorize("hasRole('LANDLORD')")
@PrsdbController
@RequestMapping(PROPERTY_COMPLIANCE_ROUTE)
class NewPropertyComplianceController(
    private val propertyComplianceJourneyFactory: NewPropertyComplianceJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val tokenCookieService: TokenCookieService,
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

        return try {
            val journeyMap = propertyComplianceJourneyFactory.createJourneySteps()
            journeyMap[stepName]?.postStepModelAndView(annotatedFormData)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Step not found")
        } catch (_: NoSuchJourneyException) {
            val journeyId = propertyComplianceJourneyFactory.initializeJourneyState(principal)
            val redirectUrl = JourneyStateService.urlWithJourneyState(stepName, journeyId)
            ModelAndView("redirect:$redirectUrl")
        }
    }

    // TODO PDJB-467 - add endpoints for file uploads

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

        private const val PROPERTY_COMPLIANCE_TASK_LIST_ROUTE = "$PROPERTY_COMPLIANCE_ROUTE/$TASK_LIST_PATH_SEGMENT"

        fun getPropertyCompliancePath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_ROUTE).expand(propertyOwnershipId).toASCIIString()

        fun getPropertyComplianceTaskListPath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_COMPLIANCE_TASK_LIST_ROUTE).expand(propertyOwnershipId).toASCIIString()
    }
}
