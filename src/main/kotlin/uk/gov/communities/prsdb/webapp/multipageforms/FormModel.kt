package uk.gov.communities.prsdb.webapp.multipageforms

import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

interface FormModel<TSelf : FormModel<TSelf>> {
    // Get the property names in the order they're defined in the primary constructor
    fun getFieldNames(): List<String> = this::class.primaryConstructor?.parameters?.map { it.name!! }!!

    fun getFieldModels(errorKeysByField: Map<String, List<String>>): List<FormFieldModel> =
        getFieldNames().map { fieldName ->
            val errorKeys = errorKeysByField[fieldName] ?: listOf()
            getFieldModel(fieldName, errorKeys)
        }

    fun getFieldModel(
        fieldName: String,
        errorKeys: List<String>,
    ): FormFieldModel {
        val valueProperty =
            this::class
                .memberProperties
                .firstOrNull { it.name == fieldName } as? KProperty1<FormModel<TSelf>, *>
                ?: throw IllegalArgumentException("No property '$fieldName' found on ${this::class}")
        val formFieldAnnotation =
            valueProperty.findAnnotation<FormField>()
                ?: throw IllegalArgumentException("Property '$fieldName' does not have a @FormField annotation")
        val fragmentName = formFieldAnnotation.fragmentName
        val labelKey = formFieldAnnotation.labelKey
        val hintKey = formFieldAnnotation.hintKey.ifBlank { null }
        val value = valueProperty.get(this)
        if (value !is String?) {
            throw NotImplementedError("Only String? form values are currently supported")
        }
        return FormFieldModel(fieldName, fragmentName, labelKey, hintKey, errorKeys, value)
    }
}
