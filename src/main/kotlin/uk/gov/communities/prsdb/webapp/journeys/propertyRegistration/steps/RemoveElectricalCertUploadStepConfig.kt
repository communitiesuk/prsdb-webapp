package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RemoveFileFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.UploadService
import kotlin.collections.get
import kotlin.collections.remove

@JourneyFrameworkComponent
class RemoveElectricalCertUploadStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
    private val uploadService: UploadService,
) : AbstractRequestableStepConfig<AnyMembers, RemoveFileFormModel, ElectricalSafetyState>() {
    override val formModelClass = RemoveFileFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState) =
        mapOf(
            "fieldSetHeading" to "uploads.removeUploads.fieldSetHeading",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "optionalFieldSetHeadingParam" to getFileToRemove(state)?.fileName,
        )

    override fun chooseTemplate(state: ElectricalSafetyState): String = "forms/areYouSureForm"

    override fun mode(state: ElectricalSafetyState) =
        if (state.electricalUploadMap.isNotEmpty()) {
            AnyMembers.SOME_MEMBERS
        } else {
            AnyMembers.NO_MEMBERS
        }

    private fun getFileToRemove(state: ElectricalSafetyState): CertificateUpload? {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        return state.electricalUploadMap[keyToRemove]
    }

    override fun beforeAttemptingToReachStep(state: ElectricalSafetyState): Boolean {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.electricalUploadMap

        return keyToRemove != null && keyToRemove in currentMap.keys
    }

    override fun afterStepDataIsAdded(state: ElectricalSafetyState) {
        if (getFormModelFromStateOrNull(state)?.wantsToProceed == false) {
            return
        }
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.electricalUploadMap.toMutableMap()

        currentMap[keyToRemove]?.let { uploadService.deleteUploadedFile(it.fileUploadId) }
        currentMap.remove(keyToRemove)
        state.electricalUploadMap = currentMap
    }
}

@JourneyFrameworkComponent
final class RemoveElectricalCertUploadStep(
    stepConfig: RemoveElectricalCertUploadStepConfig,
) : RequestableStep<AnyMembers, RemoveFileFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-electrical-safety-certificate-upload"
    }
}
