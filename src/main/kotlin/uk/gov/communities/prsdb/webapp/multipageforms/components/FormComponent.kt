package uk.gov.communities.prsdb.webapp.multipageforms.components

interface FormComponent<TValue : Any> {
    val fragmentName: String
    val fieldName: String
    val validationRules: List<(TValue?) -> List<String>>

    fun validate(formData: Map<String, String>): List<String> {
        val value = getValueFromForm(formData)
        return validationRules.flatMap { it(value) }
    }

    fun bindToModel(journeyData: Map<String, Any>): FormComponentModel<TValue>

    fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    )

    fun isSatisfied(journeyData: Map<String, Any>): Boolean

    fun getValueFromForm(formData: Map<String, String>): TValue?
}

data class FormComponentModel<T : Any>(
    val fragmentName: String,
    val fieldName: String,
    var errors: List<String>? = null,
    var value: T,
)

abstract class FormComponentBuilder<TValue : Any> {
    abstract val fieldName: String
    protected val validationRules = mutableListOf<(TValue?) -> List<String>>()

    fun validationRule(rule: (TValue?) -> List<String>) {
        validationRules.add(rule)
    }

    abstract fun build(): FormComponent<TValue>
}
