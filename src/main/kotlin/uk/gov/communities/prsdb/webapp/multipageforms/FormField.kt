package uk.gov.communities.prsdb.webapp.multipageforms

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FormField(
    val fragmentName: String,
    val labelKey: String,
    // hintKey is optional - it defaults to "" because annotation params cannot be null
    val hintKey: String = "",
)
