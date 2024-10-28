package uk.gov.communities.prsdb.webapp.multipageforms

import uk.gov.communities.prsdb.webapp.multipageforms.components.EmailBuilder
import uk.gov.communities.prsdb.webapp.multipageforms.components.FormComponent
import uk.gov.communities.prsdb.webapp.multipageforms.components.FormComponentModel
import uk.gov.communities.prsdb.webapp.multipageforms.components.PhoneNumberBuilder

class Page(
    val templateName: String = "genericFormPage",
    val messageKeys: MessageKeys,
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

class MessageKeys(
    val title: String,
    val fieldsetHeading: String,
    val fieldsetHint: String? = null,
)

class MessageKeysBuilder {
    var title: String? = null
    var fieldsetHeading: String? = null
    var fieldsetHint: String? = null

    fun build() = MessageKeys(title = title!!, fieldsetHeading = fieldsetHeading!!, fieldsetHint = fieldsetHint)
}

class PageBuilder {
    private var messageKeys: MessageKeys? = null
    private val formComponents = mutableListOf<FormComponent<*>>()

    fun messageKeys(init: MessageKeysBuilder.() -> Unit) {
        messageKeys = MessageKeysBuilder().apply(init).build()
    }

    fun email(
        fieldName: String,
        init: EmailBuilder.() -> Unit,
    ) {
        formComponents.add(EmailBuilder(fieldName).apply(init).build())
    }

    fun phoneNumber(
        fieldName: String,
        init: PhoneNumberBuilder.() -> Unit,
    ) {
        formComponents.add(PhoneNumberBuilder(fieldName).apply(init).build())
    }

    fun build() = Page(messageKeys = messageKeys!!, formComponents = formComponents)
}
