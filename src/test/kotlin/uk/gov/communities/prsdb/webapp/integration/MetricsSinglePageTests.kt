package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.PlausibleClient
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.MetricsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQuery
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQueryResponse
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleResultRow
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService.Companion.LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService.Companion.LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService.Companion.PROPERTY_REGISTRATION_CONFIRMATION_ROUTE

class MetricsSinglePageTests : IntegrationTestWithImmutableData("data-metrics-local.sql") {
    @MockitoBean
    private lateinit var plausibleClient: PlausibleClient

    @BeforeEach
    fun setUp() {
        whenever(plausibleClient.query(any())).thenAnswer { invocation ->
            val query = invocation.arguments[0] as PlausibleQuery
            if (query.dimensions.isEmpty()) {
                PlausibleQueryResponse(listOf(PlausibleResultRow(metrics = listOf(753.0), dimensions = emptyList())))
            } else {
                PlausibleQueryResponse(
                    listOf(
                        completionRow(LANDLORD_REGISTRATION_START_PAGE_ROUTE, 1000.0, 1200.0),
                        completionRow(LANDLORD_REGISTRATION_CONFIRMATION_ROUTE, 732.0, 800.0),
                        completionRow(PROPERTY_REGISTRATION_ROUTE, 60.0, 80.0),
                        completionRow(PROPERTY_REGISTRATION_CONFIRMATION_ROUTE, 18.0, 20.0),
                        completionRow(LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE, 3.0, 4.0),
                        completionRow(LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE, 1.0, 2.0),
                    ),
                )
            }
        }
    }

    private fun completionRow(
        page: String,
        visitors: Double,
        pageViews: Double,
    ) = PlausibleResultRow(metrics = listOf(visitors, pageViews), dimensions = listOf(page))

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
    fun `the 2030 cohort renders the seven metrics with exact median, p90 and p95 in days`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("1", "1", "2030", "31", "12", "2030")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        assertThat(reloadedPage.metricsList.rowValue(0)).containsText("121")
        assertThat(reloadedPage.metricsList.rowValue(1)).containsText("97")
        assertThat(reloadedPage.metricsList.rowValue(2)).containsText("101")
        assertThat(reloadedPage.metricsList.rowValue(3)).containsText("101")
        assertThat(reloadedPage.metricsList.rowValue(4)).containsText("50 days")
        assertThat(reloadedPage.metricsList.rowValue(5)).containsText("90 days")
        assertThat(reloadedPage.metricsList.rowValue(6)).containsText("95 days")
    }

    @Test
    fun `the 2028 cohort renders multi-unit median, p90 and p95 durations down to minutes`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("1", "1", "2028", "31", "12", "2028")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        assertThat(reloadedPage.metricsList.rowValue(0)).containsText("120")
        assertThat(reloadedPage.metricsList.rowValue(1)).containsText("72")
        assertThat(reloadedPage.metricsList.rowValue(2)).containsText("100")
        assertThat(reloadedPage.metricsList.rowValue(3)).containsText("100")
        assertThat(reloadedPage.metricsList.rowValue(4)).containsText("22 minutes")
        assertThat(reloadedPage.metricsList.rowValue(5)).containsText("1 day, 6 hours, 21 minutes")
        assertThat(reloadedPage.metricsList.rowValue(6)).containsText("2 days, 43 minutes")
    }

    @Test
    fun `submitting a valid date range renders the CloudWatch utilisation and error rate rows from the local stub`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("1", "9", "2024", "30", "6", "2025")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        assertThat(reloadedPage.metricsList.rowValue(10)).containsText("73.40%")
        assertThat(reloadedPage.metricsList.rowValue(11)).containsText("41.20%")
        assertThat(reloadedPage.metricsList.rowValue(12)).containsText("62.50%")
        assertThat(reloadedPage.metricsList.rowValue(13)).containsText("18.90%")
        assertThat(reloadedPage.metricsList.rowValue(14)).containsText("0.82%")
        assertThat(reloadedPage.metricsList.rowValue(15)).containsText("0.05%")
    }

    @Test
    fun `submitting a valid date range renders the completion-rate and transaction-count rows from the Plausible stub`(page: Page) {
        val metricsPage = navigator.goToMetricsPage()

        metricsPage.submitDateRange("1", "9", "2024", "30", "6", "2025")

        val reloadedPage = assertPageIs(page, MetricsPage::class)
        assertThat(reloadedPage.metricsList.rowValue(7)).containsText("73.20%")
        assertThat(reloadedPage.metricsList.rowValue(8)).containsText("25.00%")
        assertThat(reloadedPage.metricsList.rowValue(9)).containsText("33.33%")
        assertThat(reloadedPage.metricsList.rowValue(16)).containsText("753")
    }
}
