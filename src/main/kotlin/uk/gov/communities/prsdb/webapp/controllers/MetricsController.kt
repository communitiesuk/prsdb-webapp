package uk.gov.communities.prsdb.webapp.controllers

import jakarta.validation.Valid
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
import uk.gov.communities.prsdb.webapp.models.dataModels.MetricsDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MetricsDateRangeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.MetricsService
import java.text.NumberFormat
import java.time.Duration
import java.util.Locale

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(MetricsController.METRICS_URL)
class MetricsController(
    private val metricsService: MetricsService,
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
                ?.let { period -> getMetricRows(metricsService.getMetrics(period)) }
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

    private fun getMetricRows(metrics: MetricsDataModel): List<SummaryListRowViewModel> =
        listOf(
            countRow("metrics.rows.landlordRegistrations", metrics.numberOfLandlordRegistrations),
            countRow("metrics.rows.properties", metrics.numberOfProperties),
            countRow("metrics.rows.landlordsWithProperty", metrics.numberOfLandlordsWithAProperty),
            averageTimeRow(metrics.averageTimeToFirstProperty),
        )

    private fun countRow(
        headingKey: String,
        count: Long,
    ): SummaryListRowViewModel =
        SummaryListRowViewModel(
            fieldHeading = headingKey,
            fieldValue = NumberFormat.getIntegerInstance(Locale.UK).format(count),
        )

    private fun averageTimeRow(duration: Duration?): SummaryListRowViewModel {
        val (valueKey, valueParam) =
            when {
                duration == null -> "metrics.saveAndReturn.noData" to null
                duration.toDays() >= 1 -> pluralisedKey("day", duration.toDays())
                duration.toHours() >= 1 -> pluralisedKey("hour", duration.toHours())
                else -> pluralisedKey("minute", duration.toMinutes())
            }
        return SummaryListRowViewModel(
            fieldHeading = "metrics.rows.averageTimeToFirstProperty",
            fieldValue = valueKey,
            optionalFieldValueParam = valueParam,
        )
    }

    private fun pluralisedKey(
        unit: String,
        amount: Long,
    ): Pair<String, Long> {
        val key = if (amount == 1L) "metrics.saveAndReturn.$unit" else "metrics.saveAndReturn.${unit}s"
        return key to amount
    }

    companion object {
        const val METRICS_URL = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$METRICS_PATH_SEGMENT"
    }
}
