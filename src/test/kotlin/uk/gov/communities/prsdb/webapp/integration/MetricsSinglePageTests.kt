package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.MetricsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class MetricsSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the metrics page loads with both reporting-period date inputs and a refresh button`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        BaseComponent.assertThat(metricsPage.form.fromDay).isVisible()
        BaseComponent.assertThat(metricsPage.form.fromMonth).isVisible()
        BaseComponent.assertThat(metricsPage.form.fromYear).isVisible()
        BaseComponent.assertThat(metricsPage.form.toDay).isVisible()
        BaseComponent.assertThat(metricsPage.form.toMonth).isVisible()
        BaseComponent.assertThat(metricsPage.form.toYear).isVisible()
        BaseComponent.assertThat(metricsPage.refreshButton).isVisible()
    }

    @Test
    fun `submitting a reversed date range shows the range error against the to-date only`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("20", "1", "2025", "10", "1", "2025")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        assertThat(reloadedPage.form.getErrorMessage("toDate"))
            .containsText("The ‘to’ date must be the same as or after the ‘from’ date")
        assertThat(reloadedPage.form.getErrorMessage("fromDate")).hasCount(0)
    }

    @Test
    fun `submitting an impossible to-date shows the date error against the to-date`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("10", "1", "2025", "31", "2", "2025")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        assertThat(reloadedPage.form.getErrorMessage("toDate")).containsText("You must enter a real date")
    }

    @Test
    fun `submitting a valid date range re-renders the page without errors`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("10", "1", "2025", "20", "1", "2025")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        BaseComponent.assertThat(reloadedPage.errorSummary).not().isVisible()
    }

    @Test
    fun `submitting a valid date range renders the five metrics computed from the seeded data`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("1", "9", "2024", "30", "6", "2025")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        assertThat(reloadedPage.metricsList.rowValue(0)).containsText("33")
        assertThat(reloadedPage.metricsList.rowValue(1)).containsText("33")
        assertThat(reloadedPage.metricsList.rowValue(2)).containsText("36")
        assertThat(reloadedPage.metricsList.rowValue(3)).containsText("3")
        assertThat(reloadedPage.metricsList.rowValue(4)).containsText("124 days")
    }
}
