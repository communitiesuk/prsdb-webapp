package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.MessageSourceConfig
import uk.gov.communities.prsdb.webapp.controllers.MetricsController.Companion.METRICS_URL
import uk.gov.communities.prsdb.webapp.models.dataModels.CloudWatchMetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.CostMetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.JourneyCompletionRatesDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.MetricsDataModel
import uk.gov.communities.prsdb.webapp.services.CloudWatchMetricsService
import uk.gov.communities.prsdb.webapp.services.CostExplorerMetricsService
import uk.gov.communities.prsdb.webapp.services.MetricsService
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService
import java.math.BigDecimal
import java.time.Duration
import kotlin.test.Test

@WebMvcTest(MetricsController::class)
@Import(MessageSourceConfig::class)
class MetricsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var metricsService: MetricsService

    @MockitoBean
    lateinit var plausibleMetricsService: PlausibleMetricsService

    @MockitoBean
    lateinit var cloudWatchMetricsService: CloudWatchMetricsService

    @MockitoBean
    lateinit var costExplorerMetricsService: CostExplorerMetricsService

    @BeforeEach
    fun setUpCostExplorerMetricsService() {
        whenever(costExplorerMetricsService.getMetrics(any(), any())).thenReturn(CostMetricsDataModel())
    }

    @Test
    fun `getMetrics returns a redirect for unauthenticated user`() {
        mvc
            .get(METRICS_URL)
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getMetrics returns 403 for unauthorized user`() {
        mvc
            .get(METRICS_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getMetrics returns 403 for LANDLORD role`() {
        mvc
            .get(METRICS_URL)
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `getMetrics returns 200 and the metrics view for a system operator`() {
        mvc
            .get(METRICS_URL)
            .andExpect {
                status { isOk() }
                view { name("metrics") }
                model {
                    attributeExists("formModel")
                    attributeExists("metricRows")
                }
            }
    }

    @Test
    fun `submitMetrics returns a redirect for unauthenticated user`() {
        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `submitMetrics returns 403 for LANDLORD role`() {
        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics re-renders the page with errors for an invalid date range`() {
        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "20")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "10")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                view { name("metrics") }
                model {
                    attributeHasFieldErrors("formModel", "toDay")
                }
            }

        verifyNoInteractions(
            metricsService,
            plausibleMetricsService,
            cloudWatchMetricsService,
            costExplorerMetricsService,
        )
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics re-renders the page without errors for a valid date range`() {
        whenever(metricsService.getMetrics(any())).thenReturn(
            MetricsDataModel(
                numberOfLandlordRegistrations = 0L,
                numberOfVerifiedLandlords = 0L,
                numberOfProperties = 0L,
                numberOfLandlordsWithAProperty = 0L,
                medianTimeToFirstProperty = null,
                p90TimeToFirstProperty = null,
                p95TimeToFirstProperty = null,
            ),
        )
        whenever(plausibleMetricsService.getCompletionRates(any())).thenReturn(
            JourneyCompletionRatesDataModel(null, null, null),
        )
        whenever(cloudWatchMetricsService.getMetrics(any())).thenReturn(
            CloudWatchMetricsDataModel(null, null, null, null, null, null),
        )

        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "10")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "20")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                view { name("metrics") }
                model {
                    attributeHasNoErrors("formModel")
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics populates nineteen metric rows for a valid date range`() {
        whenever(metricsService.getMetrics(any())).thenReturn(
            MetricsDataModel(
                numberOfLandlordRegistrations = 5L,
                numberOfVerifiedLandlords = 4L,
                numberOfProperties = 3L,
                numberOfLandlordsWithAProperty = 2L,
                medianTimeToFirstProperty = Duration.ofDays(4),
                p90TimeToFirstProperty = Duration.ofDays(10),
                p95TimeToFirstProperty = Duration.ofDays(20),
            ),
        )
        whenever(plausibleMetricsService.getCompletionRates(any())).thenReturn(
            JourneyCompletionRatesDataModel(73.24, 25.0, null),
        )
        whenever(plausibleMetricsService.getTransactionCounts(any())).thenReturn(725L)
        whenever(cloudWatchMetricsService.getMetrics(any())).thenReturn(
            CloudWatchMetricsDataModel(73.4, 41.2, 62.5, 18.9, 0.82, 0.05),
        )
        whenever(costExplorerMetricsService.getMetrics(any(), any())).thenReturn(
            CostMetricsDataModel(
                totalCost = BigDecimal("123.45"),
                costPerTransaction = BigDecimal("0.17"),
                currencyCode = "USD",
                isEstimated = false,
            ),
        )

        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "10")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "20")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                view { name("metrics") }
                model {
                    attribute("metricRows", hasSize<Any>(19))
                }
                content {
                    string(
                        stringContainsInOrder(
                            "Peak memory utilisation",
                            "73.40%",
                            "Average memory utilisation",
                            "41.20%",
                            "Peak CPU utilisation",
                            "62.50%",
                            "ElastiCache CPU utilisation",
                            "18.90%",
                            "Client error rate (HTTP 4xx)",
                            "0.82%",
                            "Server error rate (HTTP 5xx)",
                            "0.05%",
                            "Total number of transactions",
                            "725",
                            "Total AWS cost",
                            "123.45 USD",
                            "Cost per transaction",
                            "0.17 USD",
                        ),
                    )
                }
            }

        verify(plausibleMetricsService).getTransactionCounts(any())
        verify(costExplorerMetricsService).getMetrics(any(), eq(725L))
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics renders estimated cost metrics`() {
        stubNonCostMetricsServices()
        whenever(costExplorerMetricsService.getMetrics(any(), any())).thenReturn(
            CostMetricsDataModel(
                totalCost = BigDecimal("123.45"),
                costPerTransaction = BigDecimal("0.17"),
                currencyCode = "USD",
                isEstimated = true,
            ),
        )

        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "10")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "20")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                content {
                    string(
                        stringContainsInOrder(
                            "Total AWS cost",
                            "123.45 USD (estimated)",
                            "Cost per transaction",
                            "0.17 USD (estimated)",
                        ),
                    )
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics renders no data for a missing cost per transaction`() {
        stubNonCostMetricsServices()
        whenever(costExplorerMetricsService.getMetrics(any(), any())).thenReturn(
            CostMetricsDataModel(
                totalCost = BigDecimal("123.45"),
                currencyCode = "USD",
            ),
        )

        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "10")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "20")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                content {
                    string(
                        stringContainsInOrder(
                            "Total AWS cost",
                            "123.45 USD",
                            "Cost per transaction",
                            "No data",
                        ),
                    )
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics renders no data for absent cost metrics`() {
        stubNonCostMetricsServices()

        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "10")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "20")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                content {
                    string(
                        stringContainsInOrder(
                            "Total AWS cost",
                            "No data",
                            "Cost per transaction",
                            "No data",
                        ),
                    )
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `getMetrics does not render the completion rate explanation when there are no metric rows`() {
        mvc
            .get(METRICS_URL)
            .andExpect {
                status { isOk() }
                content { string(not(containsString("Completion rates for landlord and local council user registration"))) }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics renders the completion rate explanation when metric rows are populated`() {
        whenever(metricsService.getMetrics(any())).thenReturn(
            MetricsDataModel(
                numberOfLandlordRegistrations = 5L,
                numberOfVerifiedLandlords = 4L,
                numberOfProperties = 3L,
                numberOfLandlordsWithAProperty = 2L,
                medianTimeToFirstProperty = Duration.ofDays(4),
                p90TimeToFirstProperty = Duration.ofDays(10),
                p95TimeToFirstProperty = Duration.ofDays(20),
            ),
        )
        whenever(plausibleMetricsService.getCompletionRates(any())).thenReturn(
            JourneyCompletionRatesDataModel(73.24, 25.0, null),
        )
        whenever(cloudWatchMetricsService.getMetrics(any())).thenReturn(
            CloudWatchMetricsDataModel(73.4, 41.2, 62.5, 18.9, 0.82, 0.05),
        )

        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "10")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "20")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                content { string(containsString("Completion rates for landlord and local council user registration")) }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics leaves metric rows empty for an invalid date range`() {
        mvc
            .post(METRICS_URL) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                param("fromDay", "20")
                param("fromMonth", "1")
                param("fromYear", "2025")
                param("toDay", "10")
                param("toMonth", "1")
                param("toYear", "2025")
                with(csrf())
            }.andExpect {
                status { isOk() }
                model {
                    attribute("metricRows", hasSize<Any>(0))
                }
            }
    }

    private fun stubNonCostMetricsServices() {
        whenever(metricsService.getMetrics(any())).thenReturn(
            MetricsDataModel(
                numberOfLandlordRegistrations = 0L,
                numberOfVerifiedLandlords = 0L,
                numberOfProperties = 0L,
                numberOfLandlordsWithAProperty = 0L,
                medianTimeToFirstProperty = null,
                p90TimeToFirstProperty = null,
                p95TimeToFirstProperty = null,
            ),
        )
        whenever(plausibleMetricsService.getCompletionRates(any())).thenReturn(
            JourneyCompletionRatesDataModel(null, null, null),
        )
        whenever(plausibleMetricsService.getTransactionCounts(any())).thenReturn(0L)
        whenever(cloudWatchMetricsService.getMetrics(any())).thenReturn(
            CloudWatchMetricsDataModel(null, null, null, null, null, null),
        )
    }
}
