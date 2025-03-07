package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalAuthorityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

class SelectLocalAuthorityPage(
    content: Map<String, Any>,
    displaySectionHeader: Boolean = false,
    private val localAuthorityService: LocalAuthorityService,
) : AbstractPage(
        formModel = SelectLocalAuthorityFormModel::class,
        templateName = "forms/selectLocalAuthorityForm",
        content = content,
        shouldDisplaySectionHeader = displaySectionHeader,
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        val localAuthoritiesSelectOptions =
            localAuthorityService.retrieveAllLocalAuthorities().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }

        modelAndView.addObject("selectOptions", localAuthoritiesSelectOptions)
    }
}
