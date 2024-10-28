package uk.gov.communities.prsdb.webapp.multipageforms.components

data class Email(
    override val fragmentName: String = "email",
    override val fieldName: String,
    override val validationRules: List<(String?) -> List<String>>,
) : StringFormComponent()

class EmailBuilder(
    override val fieldName: String,
) : StringFormComponentBuilder() {
    override fun build() = Email(fieldName = fieldName, validationRules = validationRules)
}
