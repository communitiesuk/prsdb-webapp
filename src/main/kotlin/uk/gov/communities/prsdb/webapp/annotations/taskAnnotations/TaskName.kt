package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TaskName(
    val value: String = "",
)
