package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateElectricalSafetyController.Companion.LETTING_AGENT_UPDATE_ELECTRICAL_SAFETY_ROUTE
import uk.gov.communities.prsdb.webapp.helpers.CertificateFilenameHelper
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety.UpdateElectricalSafetyJourneyFactory
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.util.UUID

@PrsdbController
@RequestMapping(LETTING_AGENT_UPDATE_ELECTRICAL_SAFETY_ROUTE)
class LettingAgentUpdateElectricalSafetyController(
    private val journeyFactory: UpdateElectricalSafetyJourneyFactory,
    lettingAgentAccessService: LettingAgentAccessService,
    propertyOwnershipService: PropertyOwnershipService,
    private val certificateUploadHelper: CertificateUploadHelper,
) : AbstractLettingAgentUpdateController(lettingAgentAccessService, propertyOwnershipService) {
    override fun createJourneySteps(
        propertyOwnershipId: Long,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator> = journeyFactory.createJourneySteps(propertyOwnershipId, returnUrl)

    override fun initialiseJourneyState(
        token: UUID,
        propertyOwnershipId: Long,
    ): String = journeyFactory.initialiseJourneyState(token, propertyOwnershipId)

    @AvailableWhenFeatureEnabled(DELEGATE_TO_LETTING_AGENT)
    @PostMapping("/{*stepPath}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadStep(
        @PathVariable token: UUID,
        @PathVariable stepPath: String,
        @RequestParam(JourneyIdProvider.PARAMETER_NAME) journeyId: String,
        @RequestParam(CollectionKeyParameterService.PARAMETER_NAME) memberId: String?,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) uploadToken: String,
        request: HttpServletRequest,
    ): ModelAndView {
        val stepName = stepPath.trimStart('/')
        val formData =
            certificateUploadHelper.uploadFileAndReturnFormModel(
                CertificateFilenameHelper.getCertFilename(journeyId, stepName, memberId),
                fileInputIterator,
                uploadToken,
                request,
            )

        return dispatchJourneyStep(stepPath, token) { postStepModelAndView(formData) }
    }

    companion object {
        const val LETTING_AGENT_UPDATE_ELECTRICAL_SAFETY_ROUTE =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/{token}/update-electrical-safety"

        fun getUpdateElectricalSafetyRoute(token: UUID): String =
            UriTemplate(LETTING_AGENT_UPDATE_ELECTRICAL_SAFETY_ROUTE).expand(token).toASCIIString()
    }
}
