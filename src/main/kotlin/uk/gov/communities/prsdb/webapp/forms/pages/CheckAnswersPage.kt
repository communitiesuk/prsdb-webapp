package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class CheckAnswersPage(
    content: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
    templateName: String,
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
        // When displaying the check answer page, ensure that only data that will be submitted can be shown
        val submittableJourneyData = journeyDataService.getJourneyDataFromSession()
        modelAndView.addObject(
            "submittedFilteredJourneyData",
            CheckAnswersFormModel.serializeJourneyData(submittableJourneyData),
        )
        addPageContentToModel(modelAndView, submittableJourneyData)
    }

    protected abstract fun addPageContentToModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData,
    )

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
