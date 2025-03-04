package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.VerifiedIdentityModel

class VerifyIdentityPage : Page(VerifiedIdentityModel::class, "", mapOf()) {
    override fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
    ): ModelAndView {
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
