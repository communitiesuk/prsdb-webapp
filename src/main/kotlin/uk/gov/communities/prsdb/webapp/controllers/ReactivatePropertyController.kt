package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.springframework.http.MediaType
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriTemplate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AllowIfLandlord
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REACTIVATE_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.PROPERTY_REACTIVATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.helpers.CertificateFilenameHelper
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@AllowIfLandlord
@PrsdbController
@RequestMapping(PROPERTY_REACTIVATION_ROUTE)
class ReactivatePropertyController(
    private val propertyReactivationJourneyFactory: PropertyReactivationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val certificateUploadHelper: CertificateUploadHelper,
) {
    // TODO PDJB-2149: Remove this annotation when the ACTIVE_INACTIVE flag is removed
    @AvailableWhenFeatureEnabled(ACTIVE_INACTIVE)
    @GetMapping("/{*stepPath}")
    fun getJourneyStep(
        @PathVariable stepPath: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
    ): ModelAndView {
        throwIfPropertyCannotBeReactivated(propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { getStepModelAndView() }
    }

    // TODO PDJB-2149: Remove this annotation when the ACTIVE_INACTIVE flag is removed
    @AvailableWhenFeatureEnabled(ACTIVE_INACTIVE)
    @PostMapping("/{*stepPath}")
    fun postJourneyData(
        @PathVariable stepPath: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam formData: FormData,
    ): ModelAndView {
        throwIfPropertyCannotBeReactivated(propertyOwnershipId)
        return dispatchJourneyStep(stepPath, propertyOwnershipId) { postStepModelAndView(formData) }
    }

    // TODO PDJB-2149: Remove this annotation when the ACTIVE_INACTIVE flag is removed
    @AvailableWhenFeatureEnabled(ACTIVE_INACTIVE)
    @PostMapping("/{*stepPath}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun postFileUploadJourneyData(
        @PathVariable stepPath: String,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
        @RequestParam(JourneyIdProvider.PARAMETER_NAME) journeyId: String,
        @RequestParam(CollectionKeyParameterService.PARAMETER_NAME) memberId: String?,
        @RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator,
        @CookieValue(name = FILE_UPLOAD_COOKIE_NAME) token: String,
        request: HttpServletRequest,
    ): ModelAndView {
        throwIfPropertyCannotBeReactivated(propertyOwnershipId)

        val stepName = stepPath.trimStart('/')
        val formData =
            certificateUploadHelper.uploadFileAndReturnFormModel(
                CertificateFilenameHelper.getCertFilename(journeyId, stepName, memberId),
                fileInputIterator,
                token,
                request,
            )

        return dispatchJourneyStep(stepPath, propertyOwnershipId) { postStepModelAndView(formData) }
    }

    private fun dispatchJourneyStep(
        stepPath: String,
        propertyOwnershipId: Long,
        dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
    ): ModelAndView =
        JourneyStepDispatcher.handleInitialisableRequest(
            rawStepPath = stepPath,
            createRoutingMap = { propertyReactivationJourneyFactory.createJourneySteps(propertyOwnershipId) },
            initialiseJourney = { propertyReactivationJourneyFactory.initializeJourneyState(propertyOwnershipId) },
            dispatch = dispatch,
            startNewJourneyOn = { it is PropertyOwnershipMismatchException },
        )

    // TODO PDJB-2149: Remove this annotation when the ACTIVE_INACTIVE flag is removed
    @AvailableWhenFeatureEnabled(ACTIVE_INACTIVE)
    @GetMapping("/$CONFIRMATION_PATH_SEGMENT")
    fun getConfirmation(
        model: Model,
        @PathVariable("propertyOwnershipId") propertyOwnershipId: Long,
    ): String {
        throwIfPropertyCannotBeReactivated(propertyOwnershipId)

        model.addAttribute("todoComment", "TODO (PDJB-2044): Save the reactivation and show the confirmation page")
        return "forms/todoConfirmation"
    }

    private fun throwIfPropertyCannotBeReactivated(propertyOwnershipId: Long) {
        propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)
    }

    companion object {
        const val PROPERTY_REACTIVATION_ROUTE = "/$LANDLORD_PATH_SEGMENT/$REACTIVATE_PROPERTY_JOURNEY_URL/{propertyOwnershipId}"

        fun getPropertyReactivationBasePath(propertyOwnershipId: Long): String =
            UriTemplate(PROPERTY_REACTIVATION_ROUTE)
                .expand(propertyOwnershipId)
                .toASCIIString()

        fun getPropertyReactivationPath(propertyOwnershipId: Long): String =
            "${getPropertyReactivationBasePath(propertyOwnershipId)}/${ReactivationInfoStep.ROUTE_SEGMENT}"
    }
}
