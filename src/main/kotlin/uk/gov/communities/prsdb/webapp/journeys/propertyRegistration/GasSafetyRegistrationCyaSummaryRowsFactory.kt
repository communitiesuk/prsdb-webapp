package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyScenario
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class GasSafetyRegistrationCyaSummaryRowsFactory(
    private val state: GasSafetyState,
) {
    private val scenario: GasSafetyScenario = determineScenario(state)

    fun createGasSupplyRows(): List<SummaryListRowViewModel> {
        val gasSupplyRow =
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.gasSupply.fieldHeading",
                fieldValue = state.hasGasSupplyStep.outcome == YesOrNo.YES,
                destination = Destination(state.hasGasSupplyStep),
            )

        val certStatusRow =
            when (scenario) {
                GasSafetyScenario.PROVIDE_LATER -> getProvideThisLaterRow()
                GasSafetyScenario.NO_CERT, GasSafetyScenario.CERT_EXPIRED -> getNoCertRow()
                else -> null
            }

        return listOfNotNull(gasSupplyRow, certStatusRow)
    }

    fun createCertRows(): List<SummaryListRowViewModel> =
        when (scenario) {
            GasSafetyScenario.UPLOADED_CERTIFICATE -> getUploadedCertRows()
            else -> emptyList()
        }

    fun getInsetTextKey(): String? =
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

    private fun getUploadedCertRows(): List<SummaryListRowViewModel> {
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

    private fun getProvideThisLaterRow(): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkGasSafety.gasCert.fieldHeading",
            fieldValue = getProvideLaterKey(),
            destination = Destination(state.hasGasCertStep),
        )

    private fun getNoCertRow(): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkGasSafety.gasCert.fieldHeading",
            fieldValue = if (state.isOccupied) false else getProvideLaterKey(),
            destination = Destination(state.hasGasCertStep),
        )

    private fun getProvideLaterKey(): String =
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
