package uk.gov.communities.prsdb.webapp.multipageforms.components

abstract class StringFormComponent : FormComponent<String> {
    override fun bindToModel(
        journeyData: Map<String, Any>,
        formData: Map<String, String>,
    ): FormComponentModel<String> {
        val value = formData[fieldName] ?: (journeyData["$fieldName.stringValue"] as? String ?: "")
        return FormComponentModel(fragmentName = fragmentName, fieldName = fieldName, labelKey = labelKey, hintKey = hintKey, value = value)
    }

    override fun updateJourneyData(
        journeyData: MutableMap<String, Any>,
        formData: Map<String, String>,
    ) {
        val email = getValueFromForm(formData)
        if (email != null) {
            journeyData["$fieldName.stringValue"] = email
        }
    }

    override fun isSatisfied(journeyData: Map<String, Any>): Boolean = journeyData["$fieldName.stringValue"] != null

    override fun getValueFromForm(formData: Map<String, String>): String? = formData[fieldName]
}

abstract class StringFormComponentBuilder : FormComponentBuilder<String>() {
    fun validateRegex(
        regex: Regex,
        errorKey: String,
    ) {
        validationRule {
            if (it != null && !it.matches(regex)) {
                listOf(errorKey)
            } else {
                listOf()
            }
        }
    }
}
