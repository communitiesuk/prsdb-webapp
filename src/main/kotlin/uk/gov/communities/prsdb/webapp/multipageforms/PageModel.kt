package uk.gov.communities.prsdb.webapp.multipageforms

import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

data class PageModel<TPageForm : FormModel>(
    val pageForm: TPageForm,
    val pageErrorKeys: List<String>,
    val errorKeysByField: Map<String, List<String>>,
) {
    fun hasErrors(): Boolean = pageErrorKeys.isNotEmpty() || errorKeysByField.values.any { it.isNotEmpty() }

    val fieldModels: List<FormFieldModel>
        get() =
            pageForm.getFieldNames().map { fieldName ->
                val errorKeys = errorKeysByField[fieldName] ?: listOf()
                val valueProperty =
                    pageForm::class
                        .memberProperties
                        .firstOrNull { it.name == fieldName } as? KProperty1<TPageForm, *>
                        ?: throw IllegalArgumentException("No property '$fieldName' found on ${pageForm::class}")
                val formFieldAnnotation =
                    valueProperty.findAnnotation<FormField>()
                        ?: throw IllegalArgumentException("Property '$fieldName' does not have a @FormField annotation")
                val fragmentName = formFieldAnnotation.fragmentName
                val labelKey = formFieldAnnotation.labelKey
                val hintKey = formFieldAnnotation.hintKey.ifBlank { null }
                val value = valueProperty.get(pageForm)
                if (value !is String?) {
                    throw NotImplementedError("Only String? form values are currently supported")
                }
                FormFieldModel(fieldName, fragmentName, labelKey, hintKey, errorKeys, value)
            }
}

class FormFieldModel(
    val fieldName: String,
    val fragmentName: String,
    val labelKey: String,
    val hintKey: String?,
    val errorKeys: List<String>,
    val value: String?,
)
