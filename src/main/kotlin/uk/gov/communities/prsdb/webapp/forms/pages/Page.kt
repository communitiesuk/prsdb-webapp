package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.reflect.KClass

class Page(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    shouldDisplaySectionHeader: Boolean = false,
) : AbstractPage(formModel, templateName, content, shouldDisplaySectionHeader) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        journeyData: JourneyData?,
    ) {
    }
}
