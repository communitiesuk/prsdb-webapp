package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertificateFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.CheckEicUploadsFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.CheckEicrUploadsFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.CheckElectricalSafetyAnswersFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.EicExpiryDateFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.EicrExpiryDateFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.HasElectricalCertFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.UploadEicFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.UploadEicrFormPageLettingAgentUpdateElectricalSafety
import java.util.UUID

class PropertyDetailsLettingAgentUpdateElectricalSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    // PO 39 (token ...2222a) was delegated at registration
    private val delegatedAtRegistrationToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222a")
    private val urlArguments = mapOf("token" to delegatedAtRegistrationToken.toString())

    @ParameterizedTest(name = "{0}")
    @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
    fun `uploading an electrical certificate as a letting agent saves it and returns to the property record`(
        certificateType: CertificateType,
        page: Page,
    ) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(delegatedAtRegistrationToken)
        detailsPage.electricalSafetyCard
            .getAction("Change")
            .link
            .clickAndWait()

        val hasElectricalCertPage =
            assertPageIs(page, HasElectricalCertFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        when (certificateType) {
            CertificateType.Eic -> hasElectricalCertPage.submitHasEic()
            CertificateType.Eicr -> hasElectricalCertPage.submitHasEicr()
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

        val expiryDate = DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 1))
        val expiryDatePage = assertExpiryDatePage(page, certificateType)
        assertThat(expiryDatePage.form.submitButton).hasText("Continue")
        expiryDatePage.submitDate(expiryDate)

        assertUploadPage(page, certificateType).uploadCertificate("validFile.png")

        assertCheckUploadsPage(page, certificateType).form.submit()

        val checkAnswersPage =
            assertPageIs(page, CheckElectricalSafetyAnswersFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        assertThat(checkAnswersPage.form.submitButton).hasText("Confirm and submit update")
        checkAnswersPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
    }

    private fun assertExpiryDatePage(
        page: Page,
        certificateType: CertificateType,
    ): DateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, EicExpiryDateFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, EicrExpiryDateFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertUploadPage(
        page: Page,
        certificateType: CertificateType,
    ): UploadCertificateFormPage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, UploadEicFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, UploadEicrFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }

    private fun assertCheckUploadsPage(
        page: Page,
        certificateType: CertificateType,
    ): CheckElectricalCertUploadsFormBasePage =
        when (certificateType) {
            CertificateType.Eic -> assertPageIs(page, CheckEicUploadsFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
            CertificateType.Eicr -> assertPageIs(page, CheckEicrUploadsFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
            else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
        }
}
