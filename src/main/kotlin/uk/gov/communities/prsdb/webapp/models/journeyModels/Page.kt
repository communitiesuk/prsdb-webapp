package uk.gov.communities.prsdb.webapp.models.journeyModels

import kotlinx.serialization.json.JsonElement
import org.springframework.web.servlet.ModelAndView

class Page(
    val messageKeys: Map<String, String>,
    val template: String,
    val validateSubmission: (Map<String, JsonElement>) -> Boolean,
) {
    fun getModelAttributes(populatedPageFields: Map<String, String>?): ModelAndView {
        val modelAndView = ModelAndView(template)
        populatedPageFields?.let { fields -> fields.forEach { modelAndView.addObject(it.key, it.value) } }
        messageKeys.forEach { if (populatedPageFields?.keys?.contains((it.key)) != true) modelAndView.addObject(it.key, it.value) }
        return modelAndView
    }
}
