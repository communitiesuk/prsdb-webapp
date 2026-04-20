package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class CheckElectricalSafetyAnswersStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, ElectricalSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: ElectricalSafetyState): Map<String, Any?> {
        val scenario = determineScenario(state)
        return mapOf(
            "rows" to getRows(state, scenario),
            "insetTextKey" to getInsetTextKey(state, scenario),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: ElectricalSafetyState) = "forms/checkElectricalSafetyAnswersForm"

    override fun mode(state: ElectricalSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun getRows(
        state: ElectricalSafetyState,
        scenario: ElectricalSafetyScenario,
    ): List<SummaryListRowViewModel> =
        when (scenario) {
            ElectricalSafetyScenario.CERT_UPLOADED -> getCertUploadedRows(state)
            ElectricalSafetyScenario.PROVIDE_LATER -> listOf(getProvideLaterRow(state))
            ElectricalSafetyScenario.NO_CERT, ElectricalSafetyScenario.CERT_EXPIRED -> listOf(getNoCertRow(state))
        }

    private fun getCertUploadedRows(state: ElectricalSafetyState): List<SummaryListRowViewModel> {
        val uploadFileNames =
            state.electricalUploadMap
                .toList()
                .sortedBy { it.first }
                .map { (_, upload) -> upload.fileName }

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkElectricalSafety.electricalCert.fieldHeading",
                fieldValue = getCertTypeLabel(state.getElectricalCertificateType()),
                destination = Destination(state.hasElectricalCertStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkElectricalSafety.expiryDate.fieldHeading",
                fieldValue = state.getElectricalCertificateExpiryDateIfReachable(),
                destination = Destination(state.electricalCertExpiryDateStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkElectricalSafety.yourCertificate.fieldHeading",
                fieldValue = uploadFileNames,
                destination = Destination(state.checkElectricalCertUploadsStep),
            ),
        )
    }

    private fun getCertTypeLabel(certType: HasElectricalSafetyCertificate?): String =
        when (certType) {
            HasElectricalSafetyCertificate.HAS_EIC -> "checkElectricalSafety.eicLabel"
            HasElectricalSafetyCertificate.HAS_EICR -> "checkElectricalSafety.eicrLabel"
            else -> throw IllegalStateException("Cert uploaded scenario requires a certificate type")
        }

    private fun getProvideLaterRow(state: ElectricalSafetyState): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkElectricalSafety.electricalCert.fieldHeading",
            fieldValue =
                if (state.isOccupied == true) {
                    "checkElectricalSafety.provideThisLater.occupied"
                } else {
                    "checkElectricalSafety.provideThisLater.unoccupied"
                },
            destination = Destination(state.hasElectricalCertStep),
        )

    private fun getNoCertRow(state: ElectricalSafetyState): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkElectricalSafety.electricalCert.fieldHeading",
            fieldValue =
                if (state.isOccupied == true) {
                    "checkElectricalSafety.noneLabel"
                } else {
                    "checkElectricalSafety.provideThisLater.unoccupied"
                },
            destination = Destination(state.hasElectricalCertStep),
        )

    private fun getInsetTextKey(
        state: ElectricalSafetyState,
        scenario: ElectricalSafetyScenario,
    ): String? =
        when (scenario) {
            ElectricalSafetyScenario.NO_CERT, ElectricalSafetyScenario.CERT_EXPIRED -> {
                if (state.isOccupied == true) "checkElectricalSafety.occupiedNoCertInsetText" else null
            }

            else -> {
                null
            }
        }

    private fun determineScenario(state: ElectricalSafetyState): ElectricalSafetyScenario =
        when (state.hasElectricalCertStep.outcome) {
            HasElectricalCertMode.PROVIDE_THIS_LATER -> {
                ElectricalSafetyScenario.PROVIDE_LATER
            }

            HasElectricalCertMode.NO_CERTIFICATE -> {
                ElectricalSafetyScenario.NO_CERT
            }

            HasElectricalCertMode.HAS_EIC, HasElectricalCertMode.HAS_EICR -> {
                if (state.getElectricalCertificateIsOutdated() == true) {
                    ElectricalSafetyScenario.CERT_EXPIRED
                } else {
                    ElectricalSafetyScenario.CERT_UPLOADED
                }
            }

            else -> {
                throw IllegalStateException("CheckElectricalSafetyAnswersStep is not reachable before hasElectricalCert is answered")
            }
        }
}

enum class ElectricalSafetyScenario {
    CERT_UPLOADED,
    PROVIDE_LATER,
    NO_CERT,
    CERT_EXPIRED,
}

@JourneyFrameworkComponent
final class CheckElectricalSafetyAnswersStep(
    stepConfig: CheckElectricalSafetyAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, ElectricalSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-electrical-safety-answers"
    }
}
