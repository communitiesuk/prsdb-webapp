package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
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
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: PageData?,
        prevStepUrl: String?,
    ): String {
        val localAuthoritiesSelectOptions =
            localAuthorityService.retrieveAllLocalAuthorities().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }

        model.addAttribute("selectOptions", localAuthoritiesSelectOptions)

        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}
