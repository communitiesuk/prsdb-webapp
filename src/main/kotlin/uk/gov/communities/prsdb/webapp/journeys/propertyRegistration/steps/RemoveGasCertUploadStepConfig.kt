package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RemoveFileFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.UploadService
import kotlin.collections.get
import kotlin.collections.remove

@JourneyFrameworkComponent
class RemoveGasCertUploadStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
    private val uploadService: UploadService,
) : AbstractRequestableStepConfig<AnyMembers, RemoveFileFormModel, GasSafetyDetailState>() {
    override val formModelClass = RemoveFileFormModel::class

    override fun getStepSpecificContent(state: GasSafetyDetailState) =
        mapOf(
            "fieldSetHeading" to "uploads.removeUploads.fieldSetHeading",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "optionalFieldSetHeadingParam" to getFileToRemove(state)?.fileName,
        )

    override fun chooseTemplate(state: GasSafetyDetailState): String = "forms/areYouSureForm"

    override fun mode(state: GasSafetyDetailState) =
        if (state.gasUploadMap.isNotEmpty()) {
            AnyMembers.SOME_MEMBERS
        } else {
            AnyMembers.NO_MEMBERS
        }

    private fun getFileToRemove(state: GasSafetyDetailState): CertificateUpload? {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        return state.gasUploadMap[keyToRemove]
    }

    override fun beforeAttemptingToReachStep(state: GasSafetyDetailState): Boolean {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.gasUploadMap

        return keyToRemove != null && keyToRemove in currentMap.keys
    }

    override fun afterStepDataIsAdded(state: GasSafetyDetailState) {
        if (getFormModelFromStateOrNull(state)?.wantsToProceed == false) {
            return
        }
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.gasUploadMap.toMutableMap()

        currentMap[keyToRemove]?.let { uploadService.deleteUploadedFile(it.fileUploadId) }
        currentMap.remove(keyToRemove)
        state.gasUploadMap = currentMap
    }
}

@JourneyFrameworkComponent
final class RemoveGasCertUploadStep(
    stepConfig: RemoveGasCertUploadStepConfig,
) : RequestableStep<AnyMembers, RemoveFileFormModel, GasSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-gas-safety-certificate-upload"
    }
}
