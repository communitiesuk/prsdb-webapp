package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.fileupload2.core.FileItemInputIterator
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AllowIfLandlord
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.filters.MultipartFormDataFilter
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REACTIVATE_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController.Companion.PROPERTY_REACTIVATION_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.helpers.CertificateFilenameHelper
import uk.gov.communities.prsdb.webapp.helpers.CertificateUploadHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStepDispatcher
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.PaymentSummaryViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyReactivationService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@AllowIfLandlord
@PrsdbController
@RequestMapping(PROPERTY_REACTIVATION_ROUTE)
class ReactivatePropertyController(
    private val propertyReactivationJourneyFactory: PropertyReactivationJourneyFactory,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val certificateUploadHelper: CertificateUploadHelper,
    private val propertyReactivationService: PropertyReactivationService,
    private val userToLandlordService: UserToLandlordService,
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
        throwIfPropertyWasNotLastReactivatedThisSession(propertyOwnershipId)

        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyOwnershipId)
        val landlord = userToLandlordService.getCurrentLandlordForUser()

        model.addAttribute("addressParts", propertyOwnership.address.toMultiLineAddress().split("\n"))
        model.addAttribute(
            "prn",
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
        )
        // TODO PDJB-2076: Set this from the session, so the payment summary only shows when a payment was made in the reactivation journey
        val paymentMade = true
        if (paymentMade) {
            model.addAttribute("paymentSummaryViewModel", getPlaceholderPaymentSummary())
        }
        model.addAttribute("hasJointLandlords", propertyOwnership.landlords.size > 1)
        model.addAttribute("landlordReactivated", propertyReactivationService.wasLandlordReactivatedByLastReactivation())
        model.addAttribute("isOrganisationLandlord", landlord.landlordType == LandlordType.ORGANISATION)
        model.addAttribute("landlordDashboardUrl", LANDLORD_DASHBOARD_URL)
        model.addAttribute("propertyDetailsUrl", PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId))

        return "reactivatePropertyConfirmation"
    }

    private fun throwIfPropertyCannotBeReactivated(propertyOwnershipId: Long) {
        propertyOwnershipService.getInactivePropertyOwnershipForCurrentLandlord(propertyOwnershipId)
    }

    private fun throwIfPropertyWasNotLastReactivatedThisSession(propertyOwnershipId: Long) {
        if (propertyOwnershipId != propertyReactivationService.getLastPropertyOwnershipIdReactivatedThisSession()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "PropertyOwnershipId $propertyOwnershipId was not the last property reactivated in the session",
            )
        }
    }

    // TODO PDJB-2076: Replace with PaymentSummaryViewModel.from using the payment made in the reactivation journey
    private fun getPlaceholderPaymentSummary() =
        PaymentSummaryViewModel(
            mutableListOf<SummaryListRowViewModel>().apply {
                addRow("registerProperty.confirmation.paymentSummary.payment", "TODO PDJB-2076")
                addRow("registerProperty.confirmation.paymentSummary.paymentDate", "TODO PDJB-2076")
                addRow("registerProperty.confirmation.paymentSummary.renewalDate", "TODO PDJB-2076")
            },
        )

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
