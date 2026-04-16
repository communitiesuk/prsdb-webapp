package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService
import kotlin.collections.set
import kotlin.math.max

@JourneyFrameworkComponent
class UploadGasCertStepConfig(
    private val virusScanCallbackService: VirusScanCallbackService,
    private val fileUploadCookieService: FileUploadCookieService,
    private val memberIdService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, GasSafetyUploadCertificateFormModel, GasSafetyState>() {
    override val formModelClass = GasSafetyUploadCertificateFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> {
        fileUploadCookieService.addFileUploadCookieToResponse()

        return mapOf(
            "fieldSetHeading" to "forms.uploadCertificate.gasSafety.fieldSetHeading",
            "fieldSetHint" to null,
        )
    }

    override fun chooseTemplate(state: GasSafetyState): String = "forms/registrationCertificateForm"

    override fun mode(state: GasSafetyState) = if (state.gasUploadMap.isNotEmpty()) Complete.COMPLETE else null

    override fun afterStepDataIsAdded(state: GasSafetyState) {
        getFormModelFromState(state).fileUploadId?.let { fileUploadId ->
            virusScanCallbackService.saveEmailForJourney(
                state.journeyId,
                fileUploadId,
                CertificateType.GasSafetyCert,
            )
            virusScanCallbackService.saveEmailToMonitoringTeam(
                state.journeyId,
                fileUploadId,
                CertificateType.GasSafetyCert,
            )

            val formModel = getFormModelFromState(state)

            val keyToUpdate = memberIdService.getParameterOrNull() ?: state.nextGasUploadMemberId

            val currentMap = state.gasUploadMap.toMutableMap()
            currentMap[keyToUpdate] = CertificateUpload(fileUploadId, formModel.name)
            state.gasUploadMap = currentMap

            // We need entries to have unique indexes as if a user goes back to the delete page of an old upload, we want to ensure they can't delete a file they didn't mean to
            state.highestAssignedGasMemberId = max(keyToUpdate, state.highestAssignedGasMemberId ?: 0)

            state.uploadGasCertStep.clearFormData()
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
