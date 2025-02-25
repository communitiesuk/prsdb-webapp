package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.VerifiedIdentityModel

class VerifyIdentityPage : Page(VerifiedIdentityModel::class, "", mapOf()) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
    ): String {
        val bindingResult = bindDataToFormModel(validator, pageData)
        throw IllegalStateException(
            "Verify Identity Page should never be displayed - it should always redirect to the next step. " +
                "This should only be reached if there are binding errors: \n" +
                bindingResult.allErrors.joinToString(
                    "\n",
                ),
        )
    }
}
