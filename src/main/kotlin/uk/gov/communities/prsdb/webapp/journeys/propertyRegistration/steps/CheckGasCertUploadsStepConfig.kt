package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.UploadService

@JourneyFrameworkComponent
class CheckGasCertUploadsStepConfig(
    private val memberIdService: CollectionKeyParameterService,
    private val uploadService: UploadService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyDetailState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyDetailState) =
        mapOf(
            "addAnotherTitle" to "uploads.checkUploads.heading",
            "optionalAddAnotherTitleParam" to getUploadCount(state),
            "summaryText" to "uploads.checkUploads.paragraph",
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "uploads.checkUploads.buttons.addAnother",
            "uploadRows" to getUploadRows(state),
            "addAnotherUrl" to
                Destination(state.uploadGasCertStep)
                    .withUrlParameter(memberIdService.createParameterPair(state.getNextGasUploadMemberId()))
                    .toUrlStringOrNull(),
        )

    private fun getUploadRows(state: GasSafetyDetailState): List<UploadRow> =
        state.gasUploadMap
            .toList()
            .sortedBy { it.first }
            .map { (internalIndex, upload) ->
                val uploadRecord = uploadService.getFileUploadById(upload.fileUploadId)
                UploadRow(
                    fileName = upload.fileName,
                    downloadUrl = uploadService.getDownloadUrlOrNull(uploadRecord, upload.fileName),
                    removeUrl =
                        Destination(
                            state.removeGasCertUploadStep,
                        ).withUrlParameter(memberIdService.createParameterPair(internalIndex)).toUrlStringOrNull(),
                    status = MessageKeyConverter.convert(uploadRecord.status),
                )
            }

    override fun chooseTemplate(state: GasSafetyDetailState): String = "forms/addAnotherFormWithFileUploadTable"

    override fun mode(state: GasSafetyDetailState) = if (state.gasUploadMap.isNotEmpty()) Complete.COMPLETE else null

    private fun getUploadCount(state: GasSafetyDetailState): Int = getUploadRows(state).size
}

@JourneyFrameworkComponent
final class CheckGasCertUploadsStep(
    stepConfig: CheckGasCertUploadsStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-gas-safety-certificate-uploads"
    }
}

data class UploadRow(
    val fileName: String,
    val downloadUrl: String?,
    val removeUrl: String?,
    val status: String,
)
