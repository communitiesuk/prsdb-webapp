package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class CheckGasSafetyAnswersStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState): Map<String, Any?> {
        val scenario = determineScenario(state)
        return mapOf(
            "gasSupplyRows" to getGasSupplyRows(state, scenario),
            "certRows" to getCertRows(state, scenario),
            "insetTextKey" to getInsetTextKey(state, scenario),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: GasSafetyState) = "forms/checkGasSafetyAnswersForm"

    override fun mode(state: GasSafetyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun getGasSupplyRows(
        state: GasSafetyState,
        scenario: GasSafetyScenario,
    ): List<SummaryListRowViewModel> {
        val gasSupplyRow =
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.gasSupply.fieldHeading",
                fieldValue = state.hasGasSupplyStep.outcome == YesOrNo.YES,
                destination = Destination(state.hasGasSupplyStep),
            )

        val certStatusRow =
            when (scenario) {
                GasSafetyScenario.PROVIDE_LATER -> getProvideThisLaterRow(state)
                GasSafetyScenario.NO_CERT, GasSafetyScenario.CERT_EXPIRED -> getNoCertRow(state)
                else -> null
            }

        return listOfNotNull(gasSupplyRow, certStatusRow)
    }

    private fun getCertRows(
        state: GasSafetyState,
        scenario: GasSafetyScenario,
    ): List<SummaryListRowViewModel> =
        when (scenario) {
            GasSafetyScenario.UPLOADED_CERTIFICATE -> getUploadedCertRows(state)
            else -> emptyList()
        }

    private fun getUploadedCertRows(state: GasSafetyState): List<SummaryListRowViewModel> {
        val uploadFileNames =
            state.gasUploadMap
                .toList()
                .sortedBy { it.first }
                .map { (_, upload) -> upload.fileName }

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.validGasCert.fieldHeading",
                fieldValue = true,
                destination = Destination(state.hasGasCertStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.issueDate.fieldHeading",
                fieldValue = state.getGasSafetyCertificateIssueDateIfReachable(),
                destination = Destination(state.gasCertIssueDateStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.yourCertificate.fieldHeading",
                fieldValue = uploadFileNames,
                destination = Destination(state.checkGasCertUploadsStep),
            ),
        )
    }

    private fun getProvideThisLaterRow(state: GasSafetyState): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkGasSafety.gasCert.fieldHeading",
            fieldValue = getProvideLaterKey(state),
            destination = Destination(state.hasGasCertStep),
        )

    private fun getNoCertRow(state: GasSafetyState): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkGasSafety.gasCert.fieldHeading",
            fieldValue = if (state.isOccupied) false else getProvideLaterKey(state),
            destination = Destination(state.hasGasCertStep),
        )

    private fun getInsetTextKey(
        state: GasSafetyState,
        scenario: GasSafetyScenario,
    ): String? =
        when (scenario) {
            GasSafetyScenario.NO_GAS_SUPPLY -> {
                "checkGasSafety.noGasSupplyInsetText"
            }

            GasSafetyScenario.NO_CERT -> {
                if (state.isOccupied) "checkGasSafety.occupiedNoCertInsetText" else null
            }

            GasSafetyScenario.CERT_EXPIRED -> {
                if (state.isOccupied) "checkGasSafety.occupiedNoCertInsetText" else null
            }

            else -> {
                null
            }
        }

    private fun getProvideLaterKey(state: GasSafetyState): String =
        if (state.isOccupied) {
            "checkGasSafety.provideThisLater.occupied"
        } else {
            "checkGasSafety.provideThisLater.unoccupied"
        }

    private fun determineScenario(state: GasSafetyState): GasSafetyScenario {
        if (state.hasGasSupplyStep.outcome == YesOrNo.NO) return GasSafetyScenario.NO_GAS_SUPPLY
        return when (state.hasGasCertStep.outcome) {
            HasGasCertMode.PROVIDE_THIS_LATER -> {
                GasSafetyScenario.PROVIDE_LATER
            }

            HasGasCertMode.NO_CERTIFICATE -> {
                GasSafetyScenario.NO_CERT
            }

            HasGasCertMode.HAS_CERTIFICATE -> {
                if (state.getGasSafetyCertificateIsOutdated() == true) {
                    GasSafetyScenario.CERT_EXPIRED
                } else {
                    GasSafetyScenario.UPLOADED_CERTIFICATE
                }
            }

            else -> {
                throw IllegalStateException("CheckGasSafetyAnswersStep is not reachable before hasGasCert is answered")
            }
        }
    }
}

enum class GasSafetyScenario {
    UPLOADED_CERTIFICATE,
    NO_GAS_SUPPLY,
    PROVIDE_LATER,
    NO_CERT,
    CERT_EXPIRED,
}

@JourneyFrameworkComponent
final class CheckGasSafetyAnswersStep(
    stepConfig: CheckGasSafetyAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-gas-safety-answers"
    }
}
