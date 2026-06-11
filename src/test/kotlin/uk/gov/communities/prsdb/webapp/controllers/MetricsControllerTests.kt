package uk.gov.communities.prsdb.webapp.controllers

import org.hamcrest.Matchers.hasSize
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.MetricsController.Companion.METRICS_URL
import uk.gov.communities.prsdb.webapp.models.dataModels.MetricsDataModel
import uk.gov.communities.prsdb.webapp.services.MetricsService
import java.time.Duration
import kotlin.test.Test

@WebMvcTest(MetricsController::class)
class MetricsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var metricsService: MetricsService

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
                averageTimeToFirstProperty = null,
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
                    attributeHasNoErrors("formModel")
                }
            }
    }

    @Test
    @WithMockUser(roles = ["SYSTEM_OPERATOR"])
    fun `submitMetrics populates five metric rows for a valid date range`() {
        whenever(metricsService.getMetrics(any())).thenReturn(
            MetricsDataModel(
                numberOfLandlordRegistrations = 5L,
                numberOfVerifiedLandlords = 4L,
                numberOfProperties = 3L,
                numberOfLandlordsWithAProperty = 2L,
                averageTimeToFirstProperty = Duration.ofDays(4),
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
                    attribute("metricRows", hasSize<Any>(5))
                }
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
}
