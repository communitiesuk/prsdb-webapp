package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService

@JourneyFrameworkComponent
class UploadGasCertStepConfig(
    private val virusScanCallbackService: VirusScanCallbackService,
    private val fileUploadCookieService: FileUploadCookieService,
) : AbstractRequestableStepConfig<Complete, GasSafetyUploadCertificateFormModel, GasSafetyState>() {
    override val formModelClass = GasSafetyUploadCertificateFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> {
        fileUploadCookieService.addFileUploadCookieToResponse()

        return mapOf(
            "fieldSetHeading" to "forms.uploadCertificate.gasSafety.fieldSetHeading",
            "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
            "alreadyUploaded" to false,
        )
    }

    override fun chooseTemplate(state: GasSafetyState): String = "forms/uploadCertificateForm"

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.fileUploadId?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: GasSafetyState) {
        // TODO PDJB-717: Update this to notify the user and the monitoring team
        state.gasUploadId?.let { fileUploadId ->
            virusScanCallbackService.saveEmailForJourney(
                state.journeyId,
                fileUploadId,
                CertificateType.GasSafetyCert,
            )
        }
    }
}

@JourneyFrameworkComponent
final class UploadGasCertStep(
    stepConfig: UploadGasCertStepConfig,
) : RequestableStep<Complete, GasSafetyUploadCertificateFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "gas-safety-certificate-$FILE_UPLOAD_URL_SUBSTRING"
    }
}
