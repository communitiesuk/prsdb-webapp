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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MetricsDateRangeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PreAuthorize("hasRole('SYSTEM_OPERATOR')")
@PrsdbController
@RequestMapping(MetricsController.METRICS_URL)
class MetricsController {
    @GetMapping
    fun getMetrics(model: Model): String {
        model.addAttribute("formModel", MetricsDateRangeFormModel())
        addMetricRows(model)
        return "metrics"
    }

    @PostMapping
    fun submitMetrics(
        @Valid @ModelAttribute("formModel") formModel: MetricsDateRangeFormModel,
        bindingResult: BindingResult,
        model: Model,
    ): String {
        addMetricRows(model)
        return "metrics"
    }

    private fun addMetricRows(model: Model) {
        model.addAttribute("metricRows", emptyList<SummaryListRowViewModel>())
    }

    companion object {
        const val METRICS_URL = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$METRICS_PATH_SEGMENT"
    }
}
