package uk.gov.communities.prsdb.webapp.multipageforms.components

interface FormComponent<TModel : Any> {
    val fragmentName: String
    val model: TModel

    fun validate(formData: Map<String, String>): Boolean

    fun prepopulate(journeyData: Map<String, Any>)

    fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    )

    fun isSatisfied(journeyData: Map<String, Any>): Boolean
}

data class EmailInput(
    override val fragmentName: String = "emailInput",
    override val model: Model = Model(value = ""),
) : FormComponent<EmailInput.Model> {
    data class Model(
        var errorKey: String? = null,
        var value: String,
    )

    override fun validate(formData: Map<String, String>): Boolean {
        val email = formData["email"]!!
        if (!email.matches(Regex(""".+@.+"""))) {
            model.errorKey = "formComponents.email.error.invalidFormat"
            return false
        }
        return true
    }

    override fun prepopulate(journeyData: Map<String, Any>) {
        model.value = journeyData["email.email"] as? String ?: ""
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
    override val model: Model = Model(value = ""),
) : FormComponent<PhoneNumberInput.Model> {
    data class Model(
        var errorKey: String? = null,
        var value: String,
    )

    override fun validate(formData: Map<String, String>): Boolean {
        val email = formData["phoneNumber"]!!
        if (!email.matches(Regex("""[\d ]+"""))) {
            model.errorKey = "formComponents.phoneNumber.error.invalidFormat"
            return false
        }
        return true
    }

    override fun prepopulate(journeyData: Map<String, Any>) {
        model.value = journeyData["phoneNumber.phoneNumber"] as? String ?: ""
    }

    override fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    ) {
        journeyData["phoneNumber.phoneNumber"] = formData["phoneNumber"]!!
    }

    override fun isSatisfied(journeyData: Map<String, Any>): Boolean = journeyData["phoneNumber.phoneNumber"] != null
}
