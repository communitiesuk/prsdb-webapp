package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
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

    override fun enrichFormData(formData: PageData?): PageData? {
        if (formData == null) {
            return null
        }
        val newFormData = formData.toMutableMap()
        newFormData[NumberOfPeopleFormModel::numberOfHouseholds.name] = latestNumberOfHouseholds.toString()
        return newFormData
    }
}
