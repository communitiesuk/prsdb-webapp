package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
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
    private val missingAnswersRedirect: String,
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
        val submittableJourneyData = journeyDataService.getJourneyDataFromSession()
        modelAndView.addObject(
            "submittedFilteredJourneyData",
            CheckAnswersFormModel.serializeJourneyData(submittableJourneyData),
        )

        try {
            addPageContentToModel(modelAndView, submittableJourneyData)
        } catch (_: NullPointerException) {
            modelAndView.view = RedirectView(missingAnswersRedirect)
            modelAndView.model.clear()
        }
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
