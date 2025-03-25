package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationCheckUserPropertiesFormModel

class LandlordDeregistrationCheckUserPropertiesPage : AbstractPage(LandlordDeregistrationCheckUserPropertiesFormModel::class, "", mapOf()) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        val bindingResult = modelAndView.model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as? BindingResult

        throw IllegalStateException(
            "Check User Properties Page should never be displayed - it should always redirect to the next step. " +
                "This should only be reached if there are binding errors: \n" +
                bindingResult!!.allErrors.joinToString(
                    "\n",
                ),
        )
    }
}
