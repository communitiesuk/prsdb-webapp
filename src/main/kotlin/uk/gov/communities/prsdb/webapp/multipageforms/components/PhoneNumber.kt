package uk.gov.communities.prsdb.webapp.multipageforms.components

data class PhoneNumber(
    override val fragmentName: String = "phoneNumber",
    override val fieldName: String,
    override val labelKey: String,
    override val hintKey: String?,
    override val validationRules: List<(String?) -> List<String>>,
) : StringFormComponent()

class PhoneNumberBuilder(
    override val fieldName: String,
) : StringFormComponentBuilder() {
    override fun build() = PhoneNumber(fieldName = fieldName, labelKey = labelKey!!, hintKey = hintKey, validationRules = validationRules)
}
