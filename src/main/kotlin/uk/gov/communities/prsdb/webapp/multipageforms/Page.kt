package uk.gov.communities.prsdb.webapp.multipageforms

import uk.gov.communities.prsdb.webapp.multipageforms.components.FormComponent

class Page(
    val templateName: String = "genericFormPage",
    val titleKey: String,
    val formComponents: List<FormComponent<*>>,
    val validateSubmission: (formDataMap: Map<String, String>) -> Boolean = { formDataMap ->
        formComponents.all { formComponent ->
            formComponent.validate(formDataMap)
        }
    },
    val prepopulateForm: (Map<String, Any>) -> Unit = { journeyData ->
        formComponents.forEach { formComponent ->
            formComponent.prepopulate(journeyData)
        }
    },
    val updateJourneyData: (journeyData: MutableMap<String, Any>, formDataMap: Map<String, String>) -> Unit = { journeyData, formDataMap ->
        formComponents.forEach { formComponent ->
            formComponent.updateJourneyData(journeyData, formDataMap)
        }
    },
) {
    fun isSatisfied(journeyData: Map<String, Any>): Boolean =
        formComponents.all { formComponent ->
            formComponent.isSatisfied(journeyData)
        }
}
