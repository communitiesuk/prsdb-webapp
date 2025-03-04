package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.reflect.KClass

class AlreadyRegisteredPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val selectedAddressPathSegment: String,
) : Page(formModel, templateName, content) {
    override fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): ModelAndView {
        val modelAndView = super.getModelAndView(validator, pageData, prevStepUrl)
        modelAndView.addObject(
            "singleLineAddress",
            JourneyDataHelper.getFieldStringValue(journeyData!!, selectedAddressPathSegment, "address"),
        )
        return modelAndView
    }
}
