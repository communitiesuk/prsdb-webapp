package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalAuthorityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

class SelectLocalAuthorityPage(
    content: Map<String, Any>,
    displaySectionHeader: Boolean = false,
    private val localAuthorityService: LocalAuthorityService,
) : Page(
        formModel = SelectLocalAuthorityFormModel::class,
        templateName = "forms/selectLocalAuthorityForm",
        content = content,
        shouldDisplaySectionHeader = displaySectionHeader,
    ) {
    override fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
    ): ModelAndView {
        val localAuthoritiesSelectOptions =
            localAuthorityService.retrieveAllLocalAuthorities().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }

        val modelAndView = super.getModelAndView(validator, pageData, prevStepUrl)
        modelAndView.addObject("selectOptions", localAuthoritiesSelectOptions)

        return modelAndView
    }
}
