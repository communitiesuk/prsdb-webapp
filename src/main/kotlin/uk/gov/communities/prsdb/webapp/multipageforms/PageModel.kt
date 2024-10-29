package uk.gov.communities.prsdb.webapp.multipageforms

data class PageModel<TPageForm : FormModel<TPageForm>>(
    val pageForm: TPageForm,
    val pageErrorKeys: List<String>,
    val errorKeysByField: Map<String, List<String>>,
) {
    fun hasErrors(): Boolean = pageErrorKeys.isNotEmpty() || errorKeysByField.values.any { it.isNotEmpty() }

    val fieldModels: List<FormFieldModel>
        get() = pageForm.getFieldModels(errorKeysByField)
}
