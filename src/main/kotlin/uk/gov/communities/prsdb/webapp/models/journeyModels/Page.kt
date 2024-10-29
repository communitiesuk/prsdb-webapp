package uk.gov.communities.prsdb.webapp.models.journeyModels

import org.springframework.web.servlet.ModelAndView

class Page(
    val template: String,
    val messageKeys: Map<String, String>,
    val validateSubmission: (Map<String, Any>) -> Boolean,
) {
    fun getModelAttributes(populatedPageFields: Map<String, String>?): ModelAndView {
        val modelAndView = ModelAndView(template)
        populatedPageFields?.let { fields -> fields.forEach { modelAndView.addObject(it.key, it.value) } }
        messageKeys.forEach { if (populatedPageFields?.keys?.contains((it.key)) != true) modelAndView.addObject(it.key, it.value) }
        return modelAndView
    }
}

class PageBuilder(
    val template: String,
    val messageKeys: Map<String, String>,
    val validateSubmission: (Map<String, Any>) -> Boolean,
) {
    fun build(): Page = Page(template, messageKeys, validateSubmission)
}
