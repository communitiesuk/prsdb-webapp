package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import kotlin.reflect.KClass

class AlreadyRegisteredPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val selectedAddressPathSegment: String,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        model.addAttribute(
            "singleLineAddress",
            JourneyDataHelper.getFieldStringValue(journeyData!!, selectedAddressPathSegment, "address"),
        )

        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}
