package uk.gov.communities.prsdb.webapp.multipageforms

class FormFieldModel(
    val fieldName: String,
    val fragmentName: String,
    val labelKey: String,
    val hintKey: String?,
    val errorKeys: List<String>,
    val value: String?,
)
