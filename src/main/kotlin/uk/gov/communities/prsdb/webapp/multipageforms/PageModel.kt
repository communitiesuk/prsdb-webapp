package uk.gov.communities.prsdb.webapp.multipageforms

/**
 * A PageModel represents the state relating to a particular Page at a particular moment in time. It is used in rendering the view.
 */
data class PageModel<TPageForm : FormModel<TPageForm>>(
    val pageForm: TPageForm,
    val pageErrorKeys: List<String>,
    val errorKeysByField: Map<String, List<String>>,
) {
    fun hasErrors(): Boolean = pageErrorKeys.isNotEmpty() || errorKeysByField.values.any { it.isNotEmpty() }

    val fieldModels: List<FormFieldModel>
        get() = pageForm.getFieldModels(errorKeysByField)
}
