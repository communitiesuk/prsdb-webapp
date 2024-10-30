package uk.gov.communities.prsdb.webapp.multipageforms

data class FormButton(
    val textKey: String,
    val value: String? = null,
    val name: String? = null,
    val isPrimary: Boolean = false,
)
