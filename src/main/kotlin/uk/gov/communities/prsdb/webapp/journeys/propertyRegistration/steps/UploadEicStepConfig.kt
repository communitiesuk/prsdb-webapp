package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ElectricalUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService
import kotlin.collections.set
import kotlin.math.max

@JourneyFrameworkComponent
class UploadEicStepConfig(
    private val virusScanCallbackService: VirusScanCallbackService,
    private val fileUploadCookieService: FileUploadCookieService,
    private val memberIdService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, ElectricalUploadCertificateFormModel, ElectricalSafetyDetailState>() {
    override val formModelClass = ElectricalUploadCertificateFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyDetailState): Map<String, Any?> {
        fileUploadCookieService.addFileUploadCookieToResponse()

        return mapOf(
            "fieldSetHeading" to "forms.uploadCertificate.eic.fieldSetHeading",
            "fieldSetHint" to null,
        )
    }

    override fun chooseTemplate(state: ElectricalSafetyDetailState): String = "forms/registrationCertificateForm"

    override fun mode(state: ElectricalSafetyDetailState) = if (state.eicUploadMap.isNotEmpty()) Complete.COMPLETE else null

    override fun afterStepDataIsAdded(state: ElectricalSafetyDetailState) {
        getFormModelFromState(state).fileUploadId?.let { fileUploadId ->
            virusScanCallbackService.saveVirusScanFailureEmail(
                journeyId = state.journeyId,
                fileUploadId = fileUploadId,
                certificateType = CertificateType.Eic,
                propertyOwnershipId = state.propertyOwnershipId,
            )

            val formModel = getFormModelFromState(state)

            val keyToUpdate = memberIdService.getParameterOrNull() ?: state.getNextEicUploadMemberId()

            val currentMap = state.eicUploadMap.toMutableMap()
            currentMap[keyToUpdate] = CertificateUpload(fileUploadId, formModel.name)
            state.eicUploadMap = currentMap

            // We need entries to have unique indexes as if a user goes back to the delete page of an old upload, we want to ensure they can't delete a file they didn't mean to
            state.highestAssignedEicMemberId = max(keyToUpdate, state.highestAssignedEicMemberId ?: 0)

            state.uploadEicStep.clearFormData()
        }
    }
}

@JourneyFrameworkComponent
final class UploadEicStep(
    stepConfig: UploadEicStepConfig,
) : RequestableStep<Complete, ElectricalUploadCertificateFormModel, ElectricalSafetyDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eic-$FILE_UPLOAD_URL_SUBSTRING"
    }
}
