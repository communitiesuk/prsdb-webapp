package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class Page<TPageForm : FormModel>(
    private val validator: Validator,
    val templateName: String = "genericFormPage",
    val pageFormType: KClass<TPageForm>,
    val messageKeys: MessageKeys,
    val updateJourneyData: (
        MutableMap<String, Any>,
        Map<String, String>,
    ) -> Unit = { journeyData, formDataMap ->
        journeyData[pageFormType.simpleName!!] = formDataMap
    },
    bindFormDataToModel: ((Map<String, String>) -> PageModel<TPageForm>)? = null,
    bindJourneyDataToModel: ((Map<String, Any>) -> PageModel<TPageForm>)? = null,
) {
    val bindFormDataToModel: (Map<String, String>) -> PageModel<TPageForm> =
        bindFormDataToModel ?: { formData ->
            val pageForm = pageFormType.createInstance()
            val binder = WebDataBinder(pageForm)
            binder.validator = validator
            binder.bind(MutablePropertyValues(formData))
            binder.validate()

            // Get page-level error keys and per-field error keys
            val fieldNames = pageForm.getFieldNames()
            val bindResult = binder.bindingResult
            val pageErrorKeys = bindResult.globalErrors.mapNotNull { it.defaultMessage }.toMutableList()
            val errorKeysByField =
                fieldNames
                    .associateWith { fieldName ->
                        bindResult.getFieldErrors(fieldName).mapNotNull { it.defaultMessage }
                    }.toMutableMap()

            // If there's only one field, promote any errors for that field to page errors
            if (fieldNames.size == 1) {
                val singleFieldErrors = errorKeysByField.values.flatten()
                pageErrorKeys.addAll(singleFieldErrors)
                errorKeysByField.clear()
            }

            PageModel(
                pageForm = pageForm,
                pageErrorKeys = pageErrorKeys,
                errorKeysByField = errorKeysByField,
            )
        }
    val bindJourneyDataToModel: (Map<String, Any>) -> PageModel<TPageForm> =
        bindJourneyDataToModel ?: { journeyData ->
            val formDataMap = journeyData[pageFormType.simpleName!!] as? Map<String, String> ?: mapOf()
            bindFormDataToModel(formDataMap)
        }

    fun isSatisfied(journeyData: Map<String, Any>): Boolean = !bindJourneyDataToModel(journeyData).hasErrors()
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

class PageBuilder<TPageForm : FormModel>(
    val pageFormType: KClass<TPageForm>,
    val validator: Validator,
) {
    private var messageKeys: MessageKeys? = null

    fun messageKeys(init: MessageKeysBuilder.() -> Unit) {
        messageKeys = MessageKeysBuilder().apply(init).build()
    }

    fun build() = Page(validator, pageFormType = pageFormType, messageKeys = messageKeys!!)
}
