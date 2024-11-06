package uk.gov.communities.prsdb.webapp.validation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidatedBy(
    // Array of nested annotations that describe the constraints to be validated in order
    val constraints: Array<ConstraintDescriptor>,
)
