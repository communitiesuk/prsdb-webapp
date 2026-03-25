package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController.Companion.FILE_UPLOAD_COOKIE_NAME
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.TokenCookieService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService

@JourneyFrameworkComponent
class GasSafetyCertificateUploadStepConfig(
    private val virusScanCallbackService: VirusScanCallbackService,
    private val tokenCookieService: TokenCookieService,
    private val response: HttpServletResponse,
    private val request: HttpServletRequest,
) : AbstractRequestableStepConfig<Complete, GasSafetyUploadCertificateFormModel, GasSafetyState>() {
    override val formModelClass = GasSafetyUploadCertificateFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> {
        val cookie = tokenCookieService.createCookieForValue(FILE_UPLOAD_COOKIE_NAME, request.requestURI)
        response.addCookie(cookie)

        return mapOf(
            "fieldSetHeading" to "forms.uploadCertificate.gasSafety.fieldSetHeading",
            "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
            "alreadyUploaded" to (getFormModelFromStateOrNull(state)?.fileUploadId != null),
            "nextStepUrl" to
                resolveNextDestination(
                    state,
                    Destination.VisitableStep(state.gasSafetyUploadConfirmationStep, state.journeyId),
                ).toUrlStringOrNull(),
        )
    }

    override fun chooseTemplate(state: GasSafetyState): String = "forms/uploadCertificateForm"

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.fileUploadId?.let { Complete.COMPLETE }

    override fun afterSaveState(
        state: GasSafetyState,
        saveStateId: SavedJourneyState,
    ) {
        state.getGasSafetyCertificateFileUploadIdIfReachable()?.let { fileUploadId ->
            virusScanCallbackService.saveEmailToOwner(
                state.propertyId,
                fileUploadId,
                CertificateType.GasSafetyCert,
            )
            virusScanCallbackService.saveEmailToMonitoringTeam(
                state.propertyId,
                fileUploadId,
                CertificateType.GasSafetyCert,
            )
        }
    }
}

@JourneyFrameworkComponent
final class GasSafetyCertificateUploadStep(
    stepConfig: GasSafetyCertificateUploadStepConfig,
) : RequestableStep<Complete, GasSafetyUploadCertificateFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-$FILE_UPLOAD_URL_SUBSTRING"
    }
}
