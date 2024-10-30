package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * The Page is a definition of the presentation and data management of a step in a multi-page form flow
 */
class Page<TPageForm : FormModel<TPageForm>>(
    private val validator: Validator,
    val templateName: String = "genericFormPage",
    val pageFormType: KClass<TPageForm>,
    val messageKeys: MessageKeys,
    val buttons: List<FormButton>,
    bindFormDataToModel: ((Map<String, String>?) -> PageModel<TPageForm>)? = null,
) {
    val bindFormDataToModel: (Map<String, String>?) -> PageModel<TPageForm> =
        bindFormDataToModel ?: { formData ->
            val pageForm = pageFormType.createInstance()
            if (formData != null) {
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
            } else {
                PageModel(
                    pageForm = pageForm,
                    pageErrorKeys = listOf(),
                    errorKeysByField = mapOf(),
                )
            }
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

class PageBuilder<TPageForm : FormModel<TPageForm>>(
    val pageFormType: KClass<TPageForm>,
    val validator: Validator,
) {
    private var messageKeys: MessageKeys? = null
    private var buttons: MutableList<FormButton> = mutableListOf()

    fun messageKeys(init: MessageKeysBuilder.() -> Unit) {
        messageKeys = MessageKeysBuilder().apply(init).build()
    }

    fun messageKeys(
        journeySubkey: String,
        fieldSubkey: String,
    ) {
        messageKeys =
            MessageKeys(
                title = "$journeySubkey.title",
                fieldsetHeading = "$journeySubkey.$fieldSubkey.fieldsetHeading",
                fieldsetHint = "$journeySubkey.$fieldSubkey.fieldsetHint",
            )
    }

    fun saveAndContinueButton() {
        buttons.add(FormButton("common.forms.saveAndContinue", "next", "action", isPrimary = true))
    }

    fun repeatButton(textKey: String) {
        buttons.add(FormButton(textKey, "repeat", "action"))
    }

    fun build(): Page<TPageForm> {
        if (buttons.isEmpty()) {
            buttons.add(FormButton("common.forms.saveAndContinue", isPrimary = true))
        }
        return Page(validator, pageFormType = pageFormType, messageKeys = messageKeys!!, buttons = buttons)
    }
}
