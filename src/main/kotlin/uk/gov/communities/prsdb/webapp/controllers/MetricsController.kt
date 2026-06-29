package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
import org.springframework.context.MessageSource
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.METRICS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.helpers.MetricsDurationHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.CloudWatchMetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.JourneyCompletionRatesDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.MetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MetricsDateRangeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CloudWatchMetricsService
import uk.gov.communities.prsdb.webapp.services.MetricsService
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService
import java.text.NumberFormat
import java.time.Duration
import java.util.Locale

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(MetricsController.METRICS_URL)
class MetricsController(
    private val metricsService: MetricsService,
    private val plausibleMetricsService: PlausibleMetricsService,
    private val cloudWatchMetricsService: CloudWatchMetricsService,
    private val messageSource: MessageSource,
) {
    @GetMapping
    fun getMetrics(model: Model): String {
        model.addAttribute("formModel", MetricsDateRangeFormModel())
        model.addAttribute("metricRows", emptyList<SummaryListRowViewModel>())
        return "metrics"
    }

    @PostMapping
    fun submitMetrics(
        @Valid @ModelAttribute("formModel") formModel: MetricsDateRangeFormModel,
        bindingResult: BindingResult,
        model: Model,
    ): String {
        val metricRows =
            getReportingPeriodOrNull(bindingResult, formModel)
                ?.let { period ->
                    getMetricRows(
                        metricsService.getMetrics(period),
                        plausibleMetricsService.getCompletionRates(period),
                        cloudWatchMetricsService.getMetrics(period),
                        plausibleMetricsService.getTransactionCounts(period),
                    )
                }
                ?: emptyList()
        model.addAttribute("metricRows", metricRows)
        return "metrics"
    }

    private fun getReportingPeriodOrNull(
        bindingResult: BindingResult,
        formModel: MetricsDateRangeFormModel,
    ): ReportingPeriod? {
        if (bindingResult.hasErrors()) return null
        val from = formModel.fromLocalDateOrNull() ?: return null
        val to = formModel.toLocalDateOrNull() ?: return null
        return ReportingPeriod.fromDateRange(from, to)
    }

    private fun getMetricRows(
        metrics: MetricsDataModel,
        completionRates: JourneyCompletionRatesDataModel,
        cloudWatch: CloudWatchMetricsDataModel,
        totalTransactions: Long,
    ): List<SummaryListRowViewModel> =
        listOf(
            countRow("metrics.rows.landlordRegistrations", metrics.numberOfLandlordRegistrations),
            countRow("metrics.rows.verifiedLandlords", metrics.numberOfVerifiedLandlords),
            countRow("metrics.rows.properties", metrics.numberOfProperties),
            countRow("metrics.rows.landlordsWithProperty", metrics.numberOfLandlordsWithAProperty),
            durationRow("metrics.rows.medianTimeToFirstProperty", metrics.medianTimeToFirstProperty),
            durationRow("metrics.rows.p90TimeToFirstProperty", metrics.p90TimeToFirstProperty),
            durationRow("metrics.rows.p95TimeToFirstProperty", metrics.p95TimeToFirstProperty),
            completionRateRow("metrics.rows.landlordRegistrationCompletionRate", completionRates.landlordRegistration),
            completionRateRow("metrics.rows.propertyRegistrationCompletionRate", completionRates.propertyRegistration),
            completionRateRow(
                "metrics.rows.localCouncilUserRegistrationCompletionRate",
                completionRates.localCouncilUserRegistration,
            ),
            percentRow("metrics.rows.peakMemoryUtilisation", cloudWatch.peakMemoryUtilisation),
            percentRow("metrics.rows.averageMemoryUtilisation", cloudWatch.averageMemoryUtilisation),
            percentRow("metrics.rows.peakCpuUtilisation", cloudWatch.peakCpuUtilisation),
            percentRow("metrics.rows.elastiCacheCpuUtilisation", cloudWatch.elastiCacheCpuUtilisation),
            percentRow("metrics.rows.cloudFrontClientErrorRate", cloudWatch.cloudFrontClientErrorRate),
            percentRow("metrics.rows.cloudFrontServerErrorRate", cloudWatch.cloudFrontServerErrorRate),
            countRow("metrics.rows.totalTransactions", totalTransactions),
        )

    private fun percentRow(
        headingKey: String,
        value: Double?,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel(
            fieldHeading = headingKey,
            fieldValue = value?.let { String.format(Locale.UK, "%.2f%%", it) } ?: "metrics.saveAndReturn.noData",
        )

    private fun completionRateRow(
        headingKey: String,
        rate: Double?,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel(
            fieldHeading = headingKey,
            fieldValue = rate?.let { String.format(Locale.UK, "%.2f%%", it) } ?: "metrics.saveAndReturn.noData",
        )

    private fun countRow(
        headingKey: String,
        count: Long,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel(
            fieldHeading = headingKey,
            fieldValue = NumberFormat.getIntegerInstance(Locale.UK).format(count),
        )

    private fun durationRow(
        headingKey: String,
        duration: Duration?,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel(
            fieldHeading = headingKey,
            fieldValue =
                duration?.let { MetricsDurationHelper.formatDuration(it, messageSource) }
                    ?: "metrics.saveAndReturn.noData",
        )

    companion object {
        const val METRICS_URL = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$METRICS_PATH_SEGMENT"
    }
}
