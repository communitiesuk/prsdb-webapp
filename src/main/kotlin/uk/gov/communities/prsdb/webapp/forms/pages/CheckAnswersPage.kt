package uk.gov.communities.prsdb.webapp.forms.pages

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class CheckAnswersPage(
    content: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
) : AbstractPage(
        formModel = CheckAnswersFormModel::class,
        templateName = "forms/checkAnswersForm",
        content = content,
    ) {
    final override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!
        modelAndView.addObject("formData", getFormData(filteredJourneyData))
        modelAndView.addObject("submittedFilteredJourneyData", serializeJourneyData(filteredJourneyData))
        furtherEnrichModel(modelAndView, filteredJourneyData)
    }

    protected abstract fun getFormData(filteredJourneyData: JourneyData): List<SummaryListRowViewModel>

    protected open fun furtherEnrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {}

    override fun isSatisfied(bindingResult: BindingResult): Boolean {
        val formModel = bindingResult.target as CheckAnswersFormModel
        val submittedFilteredJourneyDataWithStringValues = deserializeJourneyData(formModel.submittedFilteredJourneyData)
        val journeyData = journeyDataService.getJourneyDataFromSession()

        val hasFilteredJourneyDataChanged = submittedFilteredJourneyDataWithStringValues.any { it.value != journeyData[it.key].toString() }
        return if (hasFilteredJourneyDataChanged) {
            journeyDataService.removeJourneyDataFromSession()
            // TODO PRSD-1298: Show custom error page
            throw PrsdbWebException("Filtered journey data has changed since the page loaded.")
        } else {
            true
        }
    }

    companion object {
        private fun serializeJourneyData(journeyData: JourneyData): String {
            val journeyDataWithStringValues = journeyData.mapValues { (_, value) -> value.toString() }
            return Json.encodeToString(journeyDataWithStringValues)
        }

        private fun deserializeJourneyData(serializedJourneyData: String): Map<String, String> =
            Json.decodeFromString(serializedJourneyData)
    }
}
