package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.CertificateUploadService

@JourneyFrameworkComponent
class GasSafetyCertificateUploadStepConfig(
    private val certificateUploadService: CertificateUploadService,
) : AbstractRequestableStepConfig<Complete, GasSafetyUploadCertificateFormModel, GasSafetyState>() {
    override val formModelClass = GasSafetyUploadCertificateFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.uploadCertificate.gasSafety.fieldSetHeading",
            "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
            "alreadyUploaded" to (getFormModelFromStateOrNull(state)?.fileUploadId != null),
            // TODO PDJB-467 - check if this works - destination should depend on whether checking answers or not
            "nextStepUrl" to
                resolveNextDestination(
                    state,
                    Destination.VisitableStep(state.gasSafetyUploadConfirmationStep, state.journeyId),
                ).toUrlStringOrNull(),
        )

    override fun chooseTemplate(state: GasSafetyState): String = "forms/uploadCertificateForm"

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.fileUploadId?.let { Complete.COMPLETE }

    override fun afterSaveState(
        state: GasSafetyState,
        saveStateId: SavedJourneyState,
    ) {
        state.getGasSafetyCertificateFileUploadId()?.let { fileUploadId ->
            certificateUploadService.saveCertificateUpload(
                state.propertyId,
                fileUploadId,
                FileCategory.GasSafetyCert,
            )
        } ?: throw PrsdbWebException("File upload ID not found")
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
