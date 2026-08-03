package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.UploadService

@JourneyFrameworkComponent
class CheckElectricalCertUploadsStepConfig(
    private val memberIdService: CollectionKeyParameterService,
    private val uploadService: UploadService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyDetailState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyDetailState) =
        mapOf(
            "addAnotherTitle" to "uploads.checkUploads.heading",
            "optionalAddAnotherTitleParam" to getUploadCount(state),
            "summaryText" to "uploads.checkUploads.paragraph",
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "uploads.checkUploads.buttons.addAnother",
            "uploadRows" to getUploadRows(state),
            "addAnotherUrl" to
                Destination(state.uploadElectricalCertStep)
                    .withUrlParameter(memberIdService.createParameterPair(state.getNextElectricalUploadMemberId()))
                    .toUrlStringOrNull(),
        )

    private fun getUploadRows(state: ElectricalSafetyDetailState): List<UploadRow> =
        state.electricalUploadMap
            .toList()
            .sortedBy { it.first }
            .map { (internalIndex, upload) ->
                val uploadRecord = uploadService.getFileUploadById(upload.fileUploadId)
                UploadRow(
                    fileName = upload.fileName,
                    downloadUrl = uploadService.getDownloadUrlOrNull(uploadRecord, upload.fileName),
                    removeUrl =
                        Destination(
                            state.removeElectricalCertUploadStep,
                        ).withUrlParameter(memberIdService.createParameterPair(internalIndex)).toUrlStringOrNull(),
                    status = MessageKeyConverter.convert(uploadRecord.status),
                )
            }

    override fun chooseTemplate(state: ElectricalSafetyDetailState): String = "forms/addAnotherFormWithFileUploadTable"

    override fun mode(state: ElectricalSafetyDetailState) = if (state.electricalUploadMap.isNotEmpty()) Complete.COMPLETE else null

    private fun getUploadCount(state: ElectricalSafetyDetailState): Int = getUploadRows(state).size
}

@JourneyFrameworkComponent
final class CheckElectricalCertUploadsStep(
    stepConfig: CheckElectricalCertUploadsStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-electrical-safety-certificate-uploads"
    }
}
