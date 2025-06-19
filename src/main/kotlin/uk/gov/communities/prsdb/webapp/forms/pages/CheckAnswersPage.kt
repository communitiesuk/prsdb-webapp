package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class CheckAnswersPage(
    content: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
    templateName: String = "forms/checkAnswersForm",
    shouldDisplaySectionHeader: Boolean = false,
) : AbstractPage(
        formModel = CheckAnswersFormModel::class,
        templateName = templateName,
        content = content,
        shouldDisplaySectionHeader = shouldDisplaySectionHeader,
    ) {
    final override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!
        // TODO PRSD-1219: Rename "formData" to "summaryList" for all CYA pages/templates
        modelAndView.addObject("formData", getSummaryList(filteredJourneyData))
        modelAndView.addObject("submittedFilteredJourneyData", CheckAnswersFormModel.serializeJourneyData(filteredJourneyData))
        furtherEnrichModel(modelAndView, filteredJourneyData)
    }

    protected abstract fun getSummaryList(filteredJourneyData: JourneyData): List<SummaryListRowViewModel>

    protected open fun furtherEnrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {}

    override fun enrichFormData(formData: PageData?): PageData? {
        if (formData == null) return null
        val journeyData = journeyDataService.getJourneyDataFromSession()
        return formData + (CheckAnswersFormModel::storedJourneyData.name to journeyData)
    }

    override fun isSatisfied(bindingResult: BindingResult) =
        if (super.isSatisfied(bindingResult)) {
            true
        } else {
            // TODO PRSD-1298: Show custom error page
            journeyDataService.removeJourneyDataAndContextIdFromSession()
            throw PrsdbWebException("Filtered journey data has changed since the page loaded.")
        }
}
