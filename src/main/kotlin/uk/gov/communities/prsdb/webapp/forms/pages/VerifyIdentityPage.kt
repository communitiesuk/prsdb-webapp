package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.VerifiedIdentityModel

class VerifyIdentityPage : AbstractPage(VerifiedIdentityModel::class, "", mapOf()) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        journeyData: JourneyData?,
    ) {
        val bindingResult = modelAndView.model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as? BindingResult

        throw IllegalStateException(
            "Verify Identity Page should never be displayed - it should always redirect to the next step. " +
                "This should only be reached if there are binding errors: \n" +
                bindingResult!!.allErrors.joinToString(
                    "\n",
                ),
        )
    }
}
