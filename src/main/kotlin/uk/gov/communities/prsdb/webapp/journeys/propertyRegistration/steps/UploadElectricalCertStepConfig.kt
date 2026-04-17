package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FILE_UPLOAD_URL_SUBSTRING
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ElectricalUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService
import uk.gov.communities.prsdb.webapp.services.FileUploadCookieService
import uk.gov.communities.prsdb.webapp.services.VirusScanCallbackService
import kotlin.collections.set
import kotlin.math.max

@JourneyFrameworkComponent
class UploadElectricalCertStepConfig(
    private val virusScanCallbackService: VirusScanCallbackService,
    private val fileUploadCookieService: FileUploadCookieService,
    private val memberIdService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, ElectricalUploadCertificateFormModel, ElectricalSafetyState>() {
    override val formModelClass = ElectricalUploadCertificateFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState): Map<String, Any?> {
        fileUploadCookieService.addFileUploadCookieToResponse()

        val fieldSetHeading =
            when (state.getElectricalCertificateType()) {
                HasElectricalSafetyCertificate.HAS_EIC -> "forms.uploadCertificate.eic.fieldSetHeading"

                HasElectricalSafetyCertificate.HAS_EICR -> "forms.uploadCertificate.eicr.fieldSetHeading"

                else -> throw PrsdbWebException(
                    "Upload electrical cert step reached without a valid electrical certificate type selection",
                )
            }

        return mapOf(
            "fieldSetHeading" to fieldSetHeading,
            "fieldSetHint" to null,
        )
    }

    override fun chooseTemplate(state: ElectricalSafetyState): String = "forms/registrationCertificateForm"

    override fun mode(state: ElectricalSafetyState) = if (state.electricalUploadMap.isNotEmpty()) Complete.COMPLETE else null

    override fun afterStepDataIsAdded(state: ElectricalSafetyState) {
        getFormModelFromState(state).fileUploadId?.let { fileUploadId ->
            virusScanCallbackService.saveEmailForJourney(
                state.journeyId,
                fileUploadId,
                CertificateType.Eicr,
            )
            virusScanCallbackService.saveEmailToMonitoringTeam(
                state.journeyId,
                fileUploadId,
                CertificateType.Eicr,
            )

            val formModel = getFormModelFromState(state)

            val keyToUpdate = memberIdService.getParameterOrNull() ?: state.getNextElectricalUploadMemberId()

            val currentMap = state.electricalUploadMap.toMutableMap()
            currentMap[keyToUpdate] = CertificateUpload(fileUploadId, formModel.name)
            state.electricalUploadMap = currentMap

            // We need entries to have unique indexes as if a user goes back to the delete page of an old upload, we want to ensure they can't delete a file they didn't mean to
            state.highestAssignedElectricalMemberId = max(keyToUpdate, state.highestAssignedElectricalMemberId ?: 0)

            state.uploadElectricalCertStep.clearFormData()
        }
    }
}

@JourneyFrameworkComponent
final class UploadElectricalCertStep(
    stepConfig: UploadElectricalCertStepConfig,
) : RequestableStep<Complete, ElectricalUploadCertificateFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "electrical-safety-certificate-$FILE_UPLOAD_URL_SUBSTRING"
    }
}
