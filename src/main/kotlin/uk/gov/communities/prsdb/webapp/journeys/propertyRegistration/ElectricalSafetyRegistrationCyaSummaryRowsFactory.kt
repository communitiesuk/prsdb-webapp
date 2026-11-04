package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalSafetyScenario
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels.toUploadedFileUrls
import uk.gov.communities.prsdb.webapp.services.UploadService

class ElectricalSafetyRegistrationCyaSummaryRowsFactory(
    private val state: ElectricalSafetyDetailState,
    private val uploadService: UploadService,
    private val destinationProvider: (JourneyStep.RequestableStep<*, *, *>) -> Destination = { Destination(it) },
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
        val certType = state.getElectricalCertificateType()
        val downloadMessageKey = getDownloadMessageKey(certType)
        val isEic = state.hasElectricalCertStep.outcome == HasElectricalCertMode.HAS_EIC
        val uploadMap = if (isEic) state.eicUploadMap else state.eicrUploadMap
        // TODO PDJB-1679: Always use the EIC issue date step for EICs when the EIC_ISSUE_DATE flag is removed
        val dateStep =
            when (state.hasElectricalCertStep.outcome) {
                HasElectricalCertMode.HAS_EIC -> if (state.isEicIssueDateEnabled) state.eicIssueDateStep else state.eicExpiryDateStep
                HasElectricalCertMode.HAS_EICR -> state.eicrExpiryDateStep
                else -> throw IllegalStateException("Cert uploaded scenario requires a certificate type")
            }
        val checkUploadsStep = if (isEic) state.checkEicUploadsStep else state.checkEicrUploadsStep
        val uploadedFiles =
            uploadMap
                .toList()
                .sortedBy { it.first }
                .map { (_, upload) -> uploadService.getFileUploadById(upload.fileUploadId) to upload.fileName }
                .toUploadedFileUrls(downloadMessageKey, uploadService)

        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkElectricalSafety.electricalCert.fieldHeading",
                fieldValue = getCertTypeLabel(certType),
                destination = destinationProvider(state.hasElectricalCertStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading =
                    if (isEic) {
                        // TODO PDJB-1679: Remove this check and always use the issue date heading when the EIC_ISSUE_DATE flag is removed
                        if (state.isEicIssueDateEnabled) {
                            "checkElectricalSafety.issueDate.fieldHeading"
                        } else {
                            "checkElectricalSafety.expiryDate.fieldHeading"
                        }
                    } else {
                        "checkElectricalSafety.expiryDate.fieldHeading"
                    },
                // TODO PDJB-1669: Show the EIC issue date for EICs. With EIC_ISSUE_DATE enabled this value is blank for EICs,
                //  because it reads the EIC expiry date step, which can't be reached when the flag is on.
                //  The empty string keeps the value cell so the change link stays in the actions column
                fieldValue = state.getElectricalCertificateExpiryDateIfReachable() ?: "",
                destination = destinationProvider(dateStep),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                fieldHeading = "checkElectricalSafety.yourCertificate.fieldHeading",
                fieldValue = uploadedFiles,
                destination = destinationProvider(checkUploadsStep),
            ),
        )
    }

    private fun getDownloadMessageKey(certType: HasElectricalSafetyCertificate?): String =
        when (certType) {
            HasElectricalSafetyCertificate.HAS_EIC ->
                "propertyDetails.complianceInformation.electricalSafety.eic.downloadCertificate"

            HasElectricalSafetyCertificate.HAS_EICR ->
                "propertyDetails.complianceInformation.electricalSafety.eicr.downloadCertificate"

            HasElectricalSafetyCertificate.NO_CERTIFICATE, null -> throw IllegalStateException(
                "Cert uploaded scenario requires a certificate type",
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
            destination = destinationProvider(state.hasElectricalCertStep),
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
            destination = destinationProvider(state.hasElectricalCertStep),
        )

    private fun determineScenario(state: ElectricalSafetyDetailState): ElectricalSafetyScenario =
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
