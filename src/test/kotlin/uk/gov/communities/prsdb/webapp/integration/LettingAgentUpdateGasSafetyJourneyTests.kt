package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLettingAgentView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages.CheckGasCertUploadsFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages.CheckGasSafetyAnswersFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages.GasCertIssueDateFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages.HasGasCertFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages.HasGasSupplyFormPageLettingAgentUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages.UploadGasCertFormPageLettingAgentUpdate
import java.nio.file.Path
import java.util.UUID

class LettingAgentUpdateGasSafetyJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")
    private val urlArguments = mapOf("token" to token.toString())
    private val currentDate = DateTimeHelper().getCurrentDateInUK()

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `A letting agent can update a property's gas safety details with a valid certificate`(page: Page) {
        var propertyDetailsPage = navigator.goToPropertyDetailsLettingAgentView(token)
        propertyDetailsPage.gasSafetyCard
            .getAction("Change")
            .link
            .clickAndWait()

        // Has gas supply page - the letting agent journey mirrors the landlord journey
        val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPageLettingAgentUpdate::class, urlArguments)
        hasGasSupplyPage.submitHasGasSupply()

        // Has gas cert page - the "Provide this later" route should not be available on the update journey
        val hasGasCertPage = assertPageIs(page, HasGasCertFormPageLettingAgentUpdate::class, urlArguments)
        assertThat(hasGasCertPage.provideThisLaterButton).isHidden()
        hasGasCertPage.submitHasCertificate()

        // Gas cert issue date page
        val issueDatePage = assertPageIs(page, GasCertIssueDateFormPageLettingAgentUpdate::class, urlArguments)
        issueDatePage.submitDate(currentDate)

        // Upload gas cert page
        val uploadPage = assertPageIs(page, UploadGasCertFormPageLettingAgentUpdate::class, urlArguments)
        uploadPage.uploadGasCertificate(Path.of("src/test/resources/test-files/blank.png"))

        // Check gas cert uploads page
        val checkUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPageLettingAgentUpdate::class, urlArguments)
        checkUploadsPage.form.submit()

        // Check gas safety answers page
        val checkAnswersPage = assertPageIs(page, CheckGasSafetyAnswersFormPageLettingAgentUpdate::class, urlArguments)
        assertThat(checkAnswersPage.gasSupplySummaryList.gasSupplyRow.value).containsText("Yes")
        checkAnswersPage.form.submit()

        // Verify we're back on the letting agent property record with the updated certificate
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLettingAgentView::class, urlArguments)
        assertThat(propertyDetailsPage.gasSafetyCard.summaryList.yourCertificateRow.value).containsText("Pending virus scan")
    }
}
