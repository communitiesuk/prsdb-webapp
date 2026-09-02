package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import java.util.UUID

class PropertyDetailsLettingAgentViewTests : IntegrationTestWithImmutableData("data-local.sql") {
    // See data-local.sql: PO 39 (token ...2222a) has all details (licensing, tenancy, compliance) marked "provide later".
    private val allDetailsDelegatedToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222a")

    // PO 43 (token ...2222c) has licensing and tenancy marked "provide later" but valid, in-date compliance certificates.
    private val licensingAndTenancyOutstandingToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222c")

    // PO 40 (token ...2222b) has all details provided and valid, in-date compliance certificates.
    private val allDetailsProvidedToken = UUID.fromString("3334abcd-5678-abcd-1234-567abcd2222b")

    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `when all details are delegated the provide-details banner and single provide-later rows are shown`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(allDetailsDelegatedToken)

        assertThat(detailsPage.provideDetailsBanner).isVisible()
        assertThat(detailsPage.provideDetailsBanner).containsText("Provide all details")

        assertThat(detailsPage.sectionHeading("Property licensing")).isVisible()
        assertThat(detailsPage.summaryList.licensingRow).isVisible()
        assertThat(detailsPage.summaryList.licensingTypeRow).isHidden()

        assertThat(detailsPage.sectionHeading("Tenancy details")).isVisible()
        assertThat(detailsPage.summaryList.tenancyRow).isVisible()

        assertThat(detailsPage.sectionHeading("Compliance certificates")).isVisible()
    }

    @Test
    fun `when only licensing and tenancy are outstanding the banner is shown and compliance is valid`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(licensingAndTenancyOutstandingToken)

        assertThat(detailsPage.provideDetailsBanner).isVisible()
        assertThat(detailsPage.summaryList.licensingRow).isVisible()
        assertThat(detailsPage.summaryList.tenancyRow).isVisible()
        assertThat(detailsPage.complianceCertificates).isVisible()
    }

    @Test
    fun `when all details are provided the banner is hidden and the full detail rows are shown`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(allDetailsProvidedToken)

        assertThat(detailsPage.provideDetailsBanner).not().isVisible()

        assertThat(detailsPage.summaryList.licensingTypeRow).isVisible()
        assertThat(detailsPage.summaryList.licensingNumberRow).isVisible()
        assertThat(detailsPage.summaryList.numberOfHouseholdsRow).isVisible()
        assertThat(detailsPage.summaryList.numberOfTenantsRow).isVisible()
        assertThat(detailsPage.summaryList.furnishedStatusRow).isVisible()
        assertThat(detailsPage.summaryList.rentAmountRow).isVisible()

        assertThat(detailsPage.sectionHeading("Compliance certificates")).isVisible()
    }

    @Test
    fun `no change links are shown for any summary list row`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLettingAgentView(allDetailsProvidedToken)

        assertThat(page.locator(".govuk-summary-list__actions a")).hasCount(0)
    }

    @Test
    fun `a not found page is returned when the delegate to letting agent flag is disabled`(page: Page) {
        featureFlagManager.disable(DELEGATE_TO_LETTING_AGENT)

        navigator.navigate(
            LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(allDetailsProvidedToken),
        )

        val errorPage = createValidPage(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }
}
