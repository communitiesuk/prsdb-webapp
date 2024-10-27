package uk.gov.communities.prsdb.webapp.multipageforms

import uk.gov.communities.prsdb.webapp.multipageforms.components.FormComponent
import uk.gov.communities.prsdb.webapp.multipageforms.components.FormComponentModel

class Page(
    val templateName: String = "genericFormPage",
    val titleKey: String,
    val formComponents: List<FormComponent<*>>,
    val validateSubmission: (
        formDataMap: Map<String, String>,
    ) -> Map<String, List<String>> = { formDataMap ->
        formComponents
            .groupBy(
                { it.fieldName },
                { it.validate(formDataMap) },
            ).mapValues { it.value.flatten() }
    },
    val bindToModel: (Map<String, Any>, Map<String, List<String>>) -> List<FormComponentModel<*>> = { journeyData, errorsByFragment ->
        formComponents.map {
            val model = it.bindToModel(journeyData)
            model.errors = errorsByFragment[it.fieldName]
            model
        }
    },
    val updateJourneyData: (
        MutableMap<String, Any>,
        Map<String, String>,
    ) -> Unit = { journeyData, formDataMap ->
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
