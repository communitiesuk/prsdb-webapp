package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.CertificateUploadService

@JourneyFrameworkComponent
class EicrUploadStepConfig(
    private val certificateUploadService: CertificateUploadService,
) : AbstractRequestableStepConfig<Complete, EicrUploadCertificateFormModel, EicrState>() {
    override val formModelClass = EicrUploadCertificateFormModel::class

    override fun getStepSpecificContent(state: EicrState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.uploadCertificate.eicr.fieldSetHeading",
            "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
            "alreadyUploaded" to (getFormModelFromStateOrNull(state)?.fileUploadId != null),
            // TODO PDJB-467 - check if this works - destination should depend on whether checking answers or not
            "nextStepUrl" to
                resolveNextDestination(
                    state,
                    Destination.VisitableStep(state.eicrUploadConfirmationStep, state.journeyId),
                ).toUrlStringOrNull(),
        )

    override fun chooseTemplate(state: EicrState): String = "forms/uploadCertificateForm"

    override fun mode(state: EicrState) = getFormModelFromStateOrNull(state)?.fileUploadId?.let { Complete.COMPLETE }

    override fun afterSaveState(
        state: EicrState,
        saveStateId: SavedJourneyState,
    ) {
        state.getEicrCertificateFileUploadId()?.let { fileUploadId ->
            certificateUploadService.saveCertificateUpload(
                state.propertyId,
                fileUploadId,
                FileCategory.Eicr,
            )
        }
    }
}

@JourneyFrameworkComponent
final class EicrUploadStep(
    stepConfig: EicrUploadStepConfig,
) : RequestableStep<Complete, EicrUploadCertificateFormModel, EicrState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr-$FILE_UPLOAD_URL_SUBSTRING"
    }
}
