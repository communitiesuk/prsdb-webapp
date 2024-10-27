package uk.gov.communities.prsdb.webapp.multipageforms.components

interface FormComponent<TValue : Any> {
    val fragmentName: String
    val fieldName: String
    val validationRules: List<(TValue?) -> List<String>>

    fun validate(formData: Map<String, String>): List<String>

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

data class Email(
    override val fragmentName: String = "email",
    override val fieldName: String,
    override val validationRules: List<(String?) -> List<String>>,
) : FormComponent<String> {
    override fun validate(formData: Map<String, String>): List<String> {
        val email = getValueFromForm(formData)
        return validationRules.flatMap { it(email) }
    }

    override fun bindToModel(journeyData: Map<String, Any>): FormComponentModel<String> {
        val value = journeyData["$fieldName.phoneNumber"] as? String ?: ""
        return FormComponentModel(fragmentName = fragmentName, fieldName = fieldName, value = value)
    }

    override fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    ) {
        val email = getValueFromForm(formData)
        if (email != null) {
            journeyData["$fieldName.phoneNumber"] = email
        }
    }

    override fun isSatisfied(journeyData: Map<String, Any>): Boolean = journeyData["$fieldName.textInput"] != null

    override fun getValueFromForm(formData: Map<String, String>): String? = formData[fieldName]
}

data class PhoneNumber(
    override val fragmentName: String = "phoneNumber",
    override val fieldName: String,
    override val validationRules: List<(String?) -> List<String>>,
) : FormComponent<String> {
    override fun validate(formData: Map<String, String>): List<String> {
        val email = getValueFromForm(formData)
        return validationRules.flatMap { it(email) }
    }

    override fun bindToModel(journeyData: Map<String, Any>): FormComponentModel<String> {
        val value = journeyData["$fieldName.phoneNumber"] as? String ?: ""
        return FormComponentModel(fragmentName = fragmentName, fieldName = fieldName, value = value)
    }

    override fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    ) {
        val email = getValueFromForm(formData)
        if (email != null) {
            journeyData["$fieldName.phoneNumber"] = email
        }
    }

    override fun isSatisfied(journeyData: Map<String, Any>): Boolean = journeyData["$fieldName.textInput"] != null

    override fun getValueFromForm(formData: Map<String, String>): String? = formData[fieldName]
}
