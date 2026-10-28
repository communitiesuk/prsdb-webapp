package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.CheckEicrUploadsFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.CheckElectricalSafetyAnswersFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.EicrExpiryDateFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.HasElectricalCertFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.UploadEicrFormPageLettingAgentUpdateElectricalSafety
import java.util.UUID

class PropertyDetailsLettingAgentUpdateElectricalSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    // PO 39 (token ...2222a) was delegated at registration
    private val delegatedAtRegistrationToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222a")
    private val urlArguments = mapOf("token" to delegatedAtRegistrationToken.toString())

    @Test
    fun `uploading an electrical certificate as a letting agent saves it and returns to the property record`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(delegatedAtRegistrationToken)
        detailsPage.electricalSafetyCard
            .getAction("Change")
            .link
            .clickAndWait()

        val hasElectricalCertPage =
            assertPageIs(page, HasElectricalCertFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        hasElectricalCertPage.submitHasEicr()

        val expiryDate = DateTimeHelper().getCurrentDateInUK().plus(DatePeriod(years = 1))
        val expiryDatePage =
            assertPageIs(page, EicrExpiryDateFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        assertThat(expiryDatePage.form.submitButton).hasText("Continue")
        expiryDatePage.submitDate(expiryDate)

        val uploadPage =
            assertPageIs(page, UploadEicrFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        uploadPage.uploadCertificate("validFile.png")

        val checkUploadsPage =
            assertPageIs(page, CheckEicrUploadsFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        checkUploadsPage.form.submit()

        val checkAnswersPage =
            assertPageIs(page, CheckElectricalSafetyAnswersFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        assertThat(checkAnswersPage.form.submitButton).hasText("Confirm and submit update")
        checkAnswersPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
    }
}
