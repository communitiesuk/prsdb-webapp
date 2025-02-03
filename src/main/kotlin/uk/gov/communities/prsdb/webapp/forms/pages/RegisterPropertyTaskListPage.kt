package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel

class RegisterPropertyTaskListPage : Page(NoInputFormModel::class, "", mapOf()) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
    ): String =
        throw IllegalStateException(
            "This Task List Page should never be displayed - it should always redirect to the Task List page outside the journey. ",
        )
}
