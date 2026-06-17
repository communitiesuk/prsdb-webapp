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
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
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
            countRow("metrics.rows.verifiedLandlords", metrics.numberOfVerifiedLandlords),
            countRow("metrics.rows.properties", metrics.numberOfProperties),
            countRow("metrics.rows.landlordsWithProperty", metrics.numberOfLandlordsWithAProperty),
            durationRow("metrics.rows.medianTimeToFirstProperty", metrics.medianTimeToFirstProperty),
            durationRow("metrics.rows.p90TimeToFirstProperty", metrics.p90TimeToFirstProperty),
            durationRow("metrics.rows.p95TimeToFirstProperty", metrics.p95TimeToFirstProperty),
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
                duration?.let { formatDuration(it) }
                    ?: messageSource.getMessageForKey("metrics.saveAndReturn.noData"),
        )

    // Shows each non-zero unit down to minutes (e.g. "1 day, 6 hours, 30 minutes") rather than
    // rounding down to a single coarse unit.
    private fun formatDuration(duration: Duration): String {
        val parts =
            buildList {
                if (duration.toDaysPart() > 0) add(unitMessage("day", duration.toDaysPart()))
                if (duration.toHoursPart() > 0) add(unitMessage("hour", duration.toHoursPart().toLong()))
                if (duration.toMinutesPart() > 0) add(unitMessage("minute", duration.toMinutesPart().toLong()))
            }
        return parts.ifEmpty { listOf(unitMessage("minute", 0)) }.joinToString(", ")
    }

    private fun unitMessage(
        unit: String,
        amount: Long,
    ): String {
        val key = if (amount == 1L) "metrics.saveAndReturn.$unit" else "metrics.saveAndReturn.${unit}s"
        return messageSource.getMessageForKey(key, arrayOf(amount))
    }

    companion object {
        const val METRICS_URL = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$METRICS_PATH_SEGMENT"
    }
}
