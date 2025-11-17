package uk.gov.communities.prsdb.webapp.annotations.webAnnotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AvailableWhenFeatureFlagDisabled(
    val flagName: String,
)
