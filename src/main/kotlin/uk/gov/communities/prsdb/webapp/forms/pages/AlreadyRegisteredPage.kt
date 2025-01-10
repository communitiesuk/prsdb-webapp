package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

class AlreadyRegisteredPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val urlPathSegment: String,
    private val journeyDataService: JourneyDataService,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
    ): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        model.addAttribute(
            "singleLineAddress",
            JourneyDataService.getFieldStringValue(journeyData, urlPathSegment, "address"),
        )

        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}
