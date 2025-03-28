package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.reflect.KClass

class PropertyRegistrationNumberOfPeoplePage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    shouldDisplaySectionHeader: Boolean = false,
    private val latestNumberOfHouseholds: Int,
) : AbstractPage(formModel, templateName, content, shouldDisplaySectionHeader) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {}

    override fun bindDataToFormModel(
        validator: Validator,
        formData: PageData?,
    ): BindingResult {
        val newFormData = formData?.toMutableMap()
        if (newFormData != null) {
            newFormData["numberOfHouseholds"] = latestNumberOfHouseholds.toString()
        }
        return super.bindDataToFormModel(validator, newFormData)
    }
}
