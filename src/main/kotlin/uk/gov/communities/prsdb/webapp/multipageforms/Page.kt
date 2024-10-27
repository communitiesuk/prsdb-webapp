package uk.gov.communities.prsdb.webapp.multipageforms

import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class Page<T : Any>(
    val formType: KClass<T>,
    val templateName: String = "genericFormPage",
    val messageKeys: GenericFormPageMessages,
    val validateSubmission: (formDataMap: Map<String, Any>, validator: Validator) -> List<String> = { formDataMap, validator ->
        val typedFormData = formDataMap.toModel(formType)

        val errors = BeanPropertyBindingResult(typedFormData, formType.simpleName ?: "form")
        validator.validate(typedFormData, errors)

        // Map validation errors to a list of error message strings
        errors.allErrors.map { it.defaultMessage ?: "Validation error" }
    },
    val prepopulateForm: (Map<String, Any>) -> T = { sessionData ->
        val formData = formType.constructors.first().call()

        formType.memberProperties.forEach { property ->
            property.isAccessible = true // To access private properties
            sessionData[property.name]?.let { value ->
                (property as? KMutableProperty<*>)?.setter?.call(formData, value)
            }
        }

        formData
    },
    val updateJourneyData: (journeyData: MutableMap<String, Any>, formDataMap: Map<String, Any>) -> Unit = { journeyData, formDataMap ->
        formType.memberProperties.forEach { property ->
            property.isAccessible = true // To access private properties
            val value = formDataMap[property.name]
            if (value != null) {
                // Update the journeyData with the form data
                journeyData[property.name] = value
            }
        }
    },
)

fun <T : Any> Map<String, Any>.toModel(formType: KClass<T>): T {
    // Create an instance of T using its primary constructor
    val constructor =
        formType.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor found for ${formType.simpleName}")
    val params =
        constructor.parameters.associateWith { param ->
            // Return the corresponding value from the map, or null if not found
            this[param.name]
        }

    // Call the constructor with the parameter map
    return constructor.callBy(params)
}
