package uk.gov.communities.prsdb.webapp.models.journeyModels

import org.springframework.web.servlet.ModelAndView

class Page(
    val template: String,
    val messageKeys: Map<String, String>,
    val validateSubmission: (Map<String, Any>) -> Boolean,
    //  TODO-PRSD-422 add FormModel to the page
) {
    fun getModelAttributes(populatedPageFields: Map<String, String>?): ModelAndView {
        val modelAndView = ModelAndView(template)
        populatedPageFields?.let { fields -> fields.forEach { modelAndView.addObject(it.key, it.value) } }
        messageKeys.forEach { modelAndView.addObject(it.key, it.value) }
        return modelAndView
    }
}

// TODO-PRSD-422 this builder might not work - fix it if needed - then in JourneyConfig the methods can use this, StepBuilder and JourneyBuilder to create the journeys in a clearer way
class PageBuilder {
    private var template: String? = null
    private var messageKeys: Map<String, String>? = null
    private var validateSubmission: ((Map<String, Any>) -> Boolean)? = null

    fun build(): Page = Page(template!!, messageKeys!!, validateSubmission!!)
}
