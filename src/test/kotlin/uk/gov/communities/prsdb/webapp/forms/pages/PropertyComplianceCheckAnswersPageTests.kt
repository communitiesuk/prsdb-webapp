package uk.gov.communities.prsdb.webapp.forms.pages

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.builders.JourneyDataBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
@Suppress("UNCHECKED_CAST")
class PropertyComplianceCheckAnswersPageTests {
    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    private val certificateUrl: String = "https://example.com/certificate"

    private fun getSummaryData(
        filteredJourneyData: JourneyData,
        expectEpcUrl: Boolean,
    ): ModelMap {
        if (expectEpcUrl) {
            whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(any())).thenReturn(certificateUrl)
        }

        val propertyComplianceCheckAnswersPage =
            PropertyComplianceCheckAnswersPage(
                journeyDataService = mock(),
                epcCertificateUrlProvider = mockEpcCertificateUrlProvider,
            ) { "any address" }
        val modelAndView = ModelAndView()

        propertyComplianceCheckAnswersPage.enrichModel(modelAndView, filteredJourneyData)

        return modelAndView.modelMap
    }

    @Test
    fun `the correct summary rows appear when in-date certificates have been provided`() {
        // Arrange
        val gasCertIssueDate = LocalDate.now()
        val gasCertEngineerNum = "123456"
        val eicrIssueDate = LocalDate.now().minusDays(1)
        val epcDetails = MockEpcData.createEpcDataModel()
        val hasFireSafetyDeclaration = true
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(true)
                .withGasSafetyIssueDate(gasCertIssueDate)
                .withGasSafeEngineerNum(gasCertEngineerNum)
                .withGasSafetyCertUploadConfirmation()
                .withEicrStatus(true)
                .withEicrIssueDate(eicrIssueDate)
                .withEicrUploadConfirmation()
                .withAutoMatchedEpcDetails(epcDetails)
                .withCheckAutoMatchedEpcResult(true)
                .withFireSafetyDeclaration(hasFireSafetyDeclaration)
                .build()

        val expectedGasSafetyData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.gasSafety.download",
                    PropertyComplianceStepId.GasSafety.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    gasCertIssueDate.toKotlinLocalDate(),
                    PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    gasCertIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.engineerNumber",
                    gasCertEngineerNum,
                    PropertyComplianceStepId.GasSafetyEngineerNum.urlPathSegment,
                ),
            )
        val expectedEicrData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.eicr.download",
                    PropertyComplianceStepId.EICR.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    eicrIssueDate.toKotlinLocalDate(),
                    PropertyComplianceStepId.EicrIssueDate.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    eicrIssueDate.plusYears(EICR_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                    null,
                ),
            )
        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.view",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                    certificateUrl,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    epcDetails.energyRating.uppercase(),
                    null,
                ),
            )
        val expectedResponsibilityData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.responsibilities.fireSafety",
                    hasFireSafetyDeclaration,
                    PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.responsibilities.keepPropertySafe",
                    true,
                    PropertyComplianceStepId.KeepPropertySafe.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.responsibilities.responsibilityToTenants",
                    true,
                    PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = true)
        val returnedGasSafetyData = summaryData["gasSafetyData"] as List<SummaryListRowViewModel>
        val returnedEicrData = summaryData["eicrData"] as List<SummaryListRowViewModel>
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>
        val returnedResponsibilityData = summaryData["responsibilityData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedGasSafetyData, returnedGasSafetyData)
        assertIterableEquals(expectedEicrData, returnedEicrData)
        assertIterableEquals(expectedEpcData, returnedEpcData)
        assertIterableEquals(expectedResponsibilityData, returnedResponsibilityData)
    }

    @Test
    fun `the correct summary rows appear when certificates are missing`() {
        // Arrange
        val hasFireSafetyDeclaration = false
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(false)
                .withMissingGasSafetyExemption()
                .withEicrStatus(false)
                .withMissingEicrExemption()
                .withEpcMissingStep()
                .withFireSafetyDeclaration(hasFireSafetyDeclaration)
                .build()

        val expectedGasSafetyData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    PropertyComplianceStepId.GasSafety.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    "commonText.none",
                    PropertyComplianceStepId.GasSafetyExemption.urlPathSegment,
                ),
            )
        val expectedEicrData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    PropertyComplianceStepId.EICR.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    "commonText.none",
                    PropertyComplianceStepId.EicrExemption.urlPathSegment,
                ),
            )
        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.exemption",
                    "commonText.none",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                ),
            )
        val expectedResponsibilityData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.responsibilities.fireSafety",
                    hasFireSafetyDeclaration,
                    PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.responsibilities.keepPropertySafe",
                    true,
                    PropertyComplianceStepId.KeepPropertySafe.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.responsibilities.responsibilityToTenants",
                    true,
                    PropertyComplianceStepId.ResponsibilityToTenants.urlPathSegment,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = false)
        val returnedGasSafetyData = summaryData["gasSafetyData"] as List<SummaryListRowViewModel>
        val returnedEicrData = summaryData["eicrData"] as List<SummaryListRowViewModel>
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>
        val returnedResponsibilityData = summaryData["responsibilityData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedGasSafetyData, returnedGasSafetyData)
        assertIterableEquals(expectedEicrData, returnedEicrData)
        assertIterableEquals(expectedEpcData, returnedEpcData)
        assertIterableEquals(expectedResponsibilityData, returnedResponsibilityData)
    }

    @Test
    fun `the correct summary rows appear when expired certificates have been provided`() {
        // Arrange
        val gasCertIssueDate = LocalDate.now().minusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())
        val eicrIssueDate = LocalDate.now().minusYears(EICR_VALIDITY_YEARS.toLong())
        val epcDetails = MockEpcData.createEpcDataModel(expiryDate = LocalDate.now().minusDays(1).toKotlinLocalDate())
        val tenancyStartedBeforeExpiry = false
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(true)
                .withGasSafetyIssueDate(gasCertIssueDate)
                .withEicrStatus(true)
                .withEicrIssueDate(eicrIssueDate)
                .withAutoMatchedEpcDetails(epcDetails)
                .withCheckAutoMatchedEpcResult(true)
                .withEpcExpiryCheckStep(tenancyStartedBeforeExpiry)
                .withEpcExpiredStep()
                .withFireSafetyDeclaration(true)
                .build()

        val expectedGasSafetyData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.certificate.expired",
                    PropertyComplianceStepId.GasSafety.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    gasCertIssueDate.toKotlinLocalDate(),
                    PropertyComplianceStepId.GasSafetyIssueDate.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    gasCertIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                    null,
                ),
            )
        val expectedEicrData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.certificate.expired",
                    PropertyComplianceStepId.EICR.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.issueDate",
                    eicrIssueDate.toKotlinLocalDate(),
                    PropertyComplianceStepId.EicrIssueDate.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.validUntil",
                    eicrIssueDate.plusYears(EICR_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                    null,
                ),
            )
        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.viewExpired",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                    certificateUrl,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryCheck",
                    tenancyStartedBeforeExpiry,
                    PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    epcDetails.energyRating.uppercase(),
                    null,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = true)
        val returnedGasSafetyData = summaryData["gasSafetyData"] as List<SummaryListRowViewModel>
        val returnedEicrData = summaryData["eicrData"] as List<SummaryListRowViewModel>
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedGasSafetyData, returnedGasSafetyData)
        assertIterableEquals(expectedEicrData, returnedEicrData)
        assertIterableEquals(expectedEpcData, returnedEpcData)
    }

    @Test
    fun `the correct summary rows appear when exemptions have been provided`() {
        // Arrange
        val gasSafetyExemption = GasSafetyExemptionReason.NO_GAS_SUPPLY
        val eicrExemption = Pair(EicrExemptionReason.OTHER, "Other reason for exemption")
        val epcExemption = EpcExemptionReason.DUE_FOR_DEMOLITION
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(false)
                .withGasSafetyCertExemptionReason(gasSafetyExemption)
                .withGasSafetyCertExemptionConfirmation()
                .withEicrStatus(false)
                .withEicrExemptionReason(eicrExemption.first)
                .withEicrExemptionOtherReason(eicrExemption.second)
                .withEicrExemptionConfirmation()
                .withEpcExemptionReason(epcExemption)
                .withEpcExemptionConfirmationStep()
                .withFireSafetyDeclaration(true)
                .build()

        val expectedGasSafetyData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.gasSafety.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    PropertyComplianceStepId.GasSafety.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    gasSafetyExemption,
                    PropertyComplianceStepId.GasSafetyExemption.urlPathSegment,
                ),
            )
        val expectedEicrData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.eicr.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    PropertyComplianceStepId.EICR.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.certificate.exemption",
                    eicrExemption.toList(),
                    PropertyComplianceStepId.EicrExemption.urlPathSegment,
                ),
            )
        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.certificate.notRequired",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.exemption",
                    epcExemption,
                    PropertyComplianceStepId.EpcExemptionReason.urlPathSegment,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = false)
        val returnedGasSafetyData = summaryData["gasSafetyData"] as List<SummaryListRowViewModel>
        val returnedEicrData = summaryData["eicrData"] as List<SummaryListRowViewModel>
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedGasSafetyData, returnedGasSafetyData)
        assertIterableEquals(expectedEicrData, returnedEicrData)
        assertIterableEquals(expectedEpcData, returnedEpcData)
    }

    @Test
    fun `the correct summary rows appear when no EPC was found`() {
        // Arrange
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(false)
                .withMissingGasSafetyExemption()
                .withEicrStatus(false)
                .withMissingEicrExemption()
                .withEpcNotFoundStep()
                .withFireSafetyDeclaration(true)
                .build()
        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.certificate.notAdded",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.exemption",
                    "commonText.none",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = false)
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedEpcData, returnedEpcData)
    }

    @Test
    fun `the correct summary rows appear when the provided EPC expired after the tenancy started`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(expiryDate = LocalDate.now().minusDays(1).toKotlinLocalDate())
        val tenancyStartedBeforeExpiry = true
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(false)
                .withMissingGasSafetyExemption()
                .withEicrStatus(false)
                .withMissingEicrExemption()
                .withAutoMatchedEpcDetails(epcDetails)
                .withCheckAutoMatchedEpcResult(true)
                .withEpcExpiryCheckStep(tenancyStartedBeforeExpiry)
                .withFireSafetyDeclaration(true)
                .build()

        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.view",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                    certificateUrl,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryCheck",
                    tenancyStartedBeforeExpiry,
                    PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    epcDetails.energyRating.uppercase(),
                    null,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = true)
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedEpcData, returnedEpcData)
    }

    @Test
    fun `the correct summary rows appear when the provided (in-date) EPC has a low rating and a MEES exemption`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = "F")
        val meesExemption = MeesExemptionReason.HIGH_COST
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(false)
                .withMissingGasSafetyExemption()
                .withEicrStatus(false)
                .withMissingEicrExemption()
                .withAutoMatchedEpcDetails(epcDetails)
                .withCheckAutoMatchedEpcResult(true)
                .withMeesExemptionReasonStep(meesExemption)
                .withFireSafetyDeclaration(true)
                .build()

        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.view",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                    certificateUrl,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    epcDetails.energyRating.uppercase(),
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.meesExemption",
                    meesExemption,
                    PropertyComplianceStepId.MeesExemptionReason.urlPathSegment,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = true)
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedEpcData, returnedEpcData)
    }

    @Test
    fun `the correct summary rows appear when the provided (expired) EPC has a low rating and no MEES exemption`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = "F", expiryDate = LocalDate.now().minusDays(1).toKotlinLocalDate())
        val tenancyStartedBeforeExpiry = false
        val filteredJourneyData =
            JourneyDataBuilder()
                .withGasSafetyCertStatus(false)
                .withMissingGasSafetyExemption()
                .withEicrStatus(false)
                .withMissingEicrExemption()
                .withAutoMatchedEpcDetails(epcDetails)
                .withCheckAutoMatchedEpcResult(true)
                .withEpcExpiryCheckStep(tenancyStartedBeforeExpiry)
                .withEpcExpiredStep()
                .withFireSafetyDeclaration(true)
                .build()

        val expectedEpcData =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.certificate",
                    "forms.checkComplianceAnswers.epc.viewExpired",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                    certificateUrl,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryDate",
                    epcDetails.expiryDate,
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.expiryCheck",
                    tenancyStartedBeforeExpiry,
                    PropertyComplianceStepId.EpcExpiryCheck.urlPathSegment,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.energyRating",
                    epcDetails.energyRating.uppercase(),
                    null,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkComplianceAnswers.epc.meesExemption",
                    "commonText.none",
                    PropertyComplianceStepId.EPC.urlPathSegment,
                ),
            )

        // Act
        val summaryData = getSummaryData(filteredJourneyData, expectEpcUrl = true)
        val returnedEpcData = summaryData["epcData"] as List<SummaryListRowViewModel>

        // Assert
        assertIterableEquals(expectedEpcData, returnedEpcData)
    }
}
