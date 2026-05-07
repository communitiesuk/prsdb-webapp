package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalSafetyScenario
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

class ElectricalSafetyRegistrationCyaSummaryRowsFactory(
    private val state: ElectricalSafetyState,
) {
    private val scenario: ElectricalSafetyScenario = determineScenario(state)

    fun createRows(): List<SummaryListRowViewModel> =
        when (scenario) {
            ElectricalSafetyScenario.CERT_UPLOADED -> getCertUploadedRows()
            ElectricalSafetyScenario.PROVIDE_LATER -> listOf(getProvideLaterRow())
            ElectricalSafetyScenario.NO_CERT, ElectricalSafetyScenario.CERT_EXPIRED -> listOf(getNoCertRow())
        }

    fun getInsetTextKey(): String? =
        when (scenario) {
            ElectricalSafetyScenario.NO_CERT, ElectricalSafetyScenario.CERT_EXPIRED -> {
                if (state.isOccupied) "checkElectricalSafety.occupiedNoCertInsetText" else null
            }

            else -> {
                null
            }
        }

    private fun getCertUploadedRows(): List<SummaryListRowViewModel> {
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

            HasElectricalSafetyCertificate.NO_CERTIFICATE, null -> throw IllegalStateException(
                "Cert uploaded scenario requires a certificate type",
            )
        }

    private fun getProvideLaterRow(): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkElectricalSafety.electricalCert.fieldHeading",
            fieldValue =
                if (state.isOccupied) {
                    "checkElectricalSafety.provideThisLater.occupied"
                } else {
                    "checkElectricalSafety.provideThisLater.unoccupied"
                },
            destination = Destination(state.hasElectricalCertStep),
        )

    private fun getNoCertRow(): SummaryListRowViewModel =
        SummaryListRowViewModel.forCheckYourAnswersPage(
            fieldHeading = "checkElectricalSafety.electricalCert.fieldHeading",
            fieldValue =
                if (state.isOccupied) {
                    "checkElectricalSafety.noneLabel"
                } else {
                    "checkElectricalSafety.provideThisLater.unoccupied"
                },
            destination = Destination(state.hasElectricalCertStep),
        )

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
