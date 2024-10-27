package uk.gov.communities.prsdb.webapp.multipageforms.components

interface FormComponent<TValue : Any> {
    val fragmentName: String

    fun validate(formData: Map<String, String>): List<String>

    fun bindToModel(journeyData: Map<String, Any>): FormComponentModel<TValue>

    fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    )

    fun isSatisfied(journeyData: Map<String, Any>): Boolean
}

data class FormComponentModel<T : Any>(
    val fragmentName: String,
    var errors: List<String>? = null,
    var value: T,
)

data class EmailInput(
    override val fragmentName: String = "emailInput",
) : FormComponent<String> {
    override fun validate(formData: Map<String, String>): List<String> {
        val email = formData["email"]!!
        if (!email.matches(Regex(""".+@.+"""))) {
            return listOf("formComponents.email.error.invalidFormat")
        }
        return listOf()
    }

    override fun bindToModel(journeyData: Map<String, Any>): FormComponentModel<String> {
        val value = journeyData["email.email"] as? String ?: ""
        return FormComponentModel(fragmentName = fragmentName, value = value)
    }

    override fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    ) {
        journeyData["email.email"] = formData["email"]!!
    }

    override fun isSatisfied(journeyData: Map<String, Any>): Boolean = journeyData["email.email"] != null
}

data class PhoneNumberInput(
    override val fragmentName: String = "phoneNumber",
) : FormComponent<String> {
    override fun validate(formData: Map<String, String>): List<String> {
        val email = formData["phoneNumber"]!!
        if (!email.matches(Regex("""[\d ]+"""))) {
            return listOf("formComponents.phoneNumber.error.invalidFormat")
        }
        return listOf()
    }

    override fun bindToModel(journeyData: Map<String, Any>): FormComponentModel<String> {
        val value = journeyData["phoneNumber.phoneNumber"] as? String ?: ""
        return FormComponentModel(fragmentName = fragmentName, value = value)
    }

    override fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    ) {
        journeyData["phoneNumber.phoneNumber"] = formData["phoneNumber"]!!
    }

    override fun isSatisfied(journeyData: Map<String, Any>): Boolean = journeyData["phoneNumber.phoneNumber"] != null
}
