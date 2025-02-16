package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskSectionViewModel

class RegisterPropertyTaskListPage(
    val getListOfSections: () -> List<TaskSectionViewModel>,
) : Page(NoInputFormModel::class, "registerPropertyTaskList", mapOf()) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        journeyData!!

        model.addAttribute("registerPropertyTaskSections", getListOfSections())
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl, journeyData)
    }
}
