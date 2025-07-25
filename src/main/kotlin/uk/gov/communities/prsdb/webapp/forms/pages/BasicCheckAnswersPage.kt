package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class BasicCheckAnswersPage(
    content: Map<String, Any>,
    journeyDataService: JourneyDataService,
    shouldDisplaySectionHeader: Boolean = false,
    missingAnswersRedirect: String,
) : CheckAnswersPage(
        content,
        journeyDataService,
        templateName = "forms/checkAnswersForm",
        shouldDisplaySectionHeader,
        missingAnswersRedirect,
    ) {
    final override fun addPageContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {
        modelAndView.addObject("summaryListData", getSummaryList(filteredJourneyData))
        addExtraContentToModel(modelAndView, filteredJourneyData)
    }

    protected abstract fun getSummaryList(filteredJourneyData: JourneyData): List<SummaryListRowViewModel>

    protected open fun addExtraContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    ) {}
}
