package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasCertOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSupplyOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasSafetyScenario
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.toUploadedFileUrls
import uk.gov.communities.prsdb.webapp.services.UploadService

class GasSafetyRegistrationCyaSummaryRowsFactory(
    private val state: GasSafetyDetailState,
    private val uploadService: UploadService,
    private val destinationProvider: (JourneyStep.RequestableStep<*, *, *>) -> Destination = { Destination(it) },
) {
    private val scenario: GasSafetyScenario = determineScenario(state)

    fun createGasSupplyRows(): List<SummaryListRowViewModel> {
        // Flag-on position: "provide this later" is answered on the gas-supply question, so the deferred answer is
        // shown as a single row against that question rather than an implied "yes" plus a separate gas-cert row.
        if (state.gasSupplyOutcome == GasSupplyOutcome.PROVIDE_LATER) {
            return listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    fieldHeading = "checkGasSafety.gasSupply.fieldHeading",
                    fieldValue = getProvideLaterKey(),
                    destination = destinationProvider(state.gasSupplyOutcomeStep),
                ),
            )
        }

        val gasSupplyRow =
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.gasSupply.fieldHeading",
                fieldValue = state.gasSupplyOutcome == GasSupplyOutcome.HAS_SUPPLY,
                destination = destinationProvider(state.gasSupplyOutcomeStep),
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
        val uploadedFiles =
            state.gasUploadMap
                .toList()
                .sortedBy { it.first }
                .map { (_, upload) -> uploadService.getFileUploadById(upload.fileUploadId) to upload.fileName }
                .toUploadedFileUrls(
                    downloadMessageKey = "propertyDetails.complianceInformation.gasSafety.downloadCertificate",
                    uploadService = uploadService,
                )

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.validGasCert.fieldHeading",
                fieldValue = true,
                destination = destinationProvider(state.gasCertOutcomeStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.issueDate.fieldHeading",
                fieldValue = state.getGasSafetyCertificateIssueDateIfReachable(),
                destination = destinationProvider(state.gasCertIssueDateStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkGasSafety.yourCertificate.fieldHeading",
                fieldValue = uploadedFiles,
                destination = destinationProvider(state.checkGasCertUploadsStep),
            ),
        )
    }

    private fun getProvideThisLaterRow(): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkGasSafety.gasCert.fieldHeading",
            fieldValue = getProvideLaterKey(),
            destination = destinationProvider(state.gasCertOutcomeStep),
        )

    private fun getNoCertRow(): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkGasSafety.gasCert.fieldHeading",
            fieldValue = if (state.isOccupied) false else getProvideLaterKey(),
            destination = destinationProvider(state.gasCertOutcomeStep),
        )

    private fun getProvideLaterKey(): String =
        if (state.isOccupied) {
            "checkGasSafety.provideThisLater.occupied"
        } else {
            "checkGasSafety.provideThisLater.unoccupied"
        }

    private fun determineScenario(state: GasSafetyDetailState): GasSafetyScenario =
        when (state.gasSupplyOutcome) {
            GasSupplyOutcome.NO_SUPPLY -> GasSafetyScenario.NO_GAS_SUPPLY
            GasSupplyOutcome.PROVIDE_LATER -> GasSafetyScenario.PROVIDE_LATER
            GasSupplyOutcome.HAS_SUPPLY, null -> determineCertScenario(state)
        }

    private fun determineCertScenario(state: GasSafetyDetailState): GasSafetyScenario =
        when (state.gasCertOutcome) {
            GasCertOutcome.NO_CERTIFICATE -> GasSafetyScenario.NO_CERT
            GasCertOutcome.PROVIDE_LATER -> GasSafetyScenario.PROVIDE_LATER
            GasCertOutcome.HAS_CERTIFICATE ->
                if (state.getGasSafetyCertificateIsOutdated() == true) {
                    GasSafetyScenario.CERT_EXPIRED
                } else {
                    GasSafetyScenario.UPLOADED_CERTIFICATE
                }

            null -> throw IllegalStateException("CheckGasSafetyAnswersStep is not reachable before hasGasCert is answered")
        }
}
