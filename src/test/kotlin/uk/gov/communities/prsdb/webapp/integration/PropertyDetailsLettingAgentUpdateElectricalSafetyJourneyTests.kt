package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.CheckElectricalCertUploadsFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.CheckElectricalSafetyAnswersFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.ElectricalCertExpiryDateFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.HasElectricalCertFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages.UploadElectricalCertFormPageLettingAgentUpdateElectricalSafety
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import java.util.UUID

class PropertyDetailsLettingAgentUpdateElectricalSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    // PO 39 (token ...2222a) was delegated at registration
    private val delegatedAtRegistrationToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222a")
    private val urlArguments = mapOf("token" to delegatedAtRegistrationToken.toString())

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

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
            assertPageIs(page, ElectricalCertExpiryDateFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        assertThat(expiryDatePage.form.submitButton).hasText("Continue")
        expiryDatePage.submitDate(expiryDate)

        val uploadPage =
            assertPageIs(page, UploadElectricalCertFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        uploadPage.uploadCertificate("validFile.png")

        val checkUploadsPage =
            assertPageIs(page, CheckElectricalCertUploadsFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        checkUploadsPage.form.submit()

        val checkAnswersPage =
            assertPageIs(page, CheckElectricalSafetyAnswersFormPageLettingAgentUpdateElectricalSafety::class, urlArguments)
        checkAnswersPage.form.submit()

        assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
    }

    @Test
    fun `a not found page is returned for the electrical safety update route when the flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate(
            LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(delegatedAtRegistrationToken) +
                "/${HasElectricalCertStep.ROUTE_SEGMENT}",
        )

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }
}
