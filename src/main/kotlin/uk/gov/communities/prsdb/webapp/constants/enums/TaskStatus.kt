package uk.gov.communities.prsdb.webapp.constants.enums

enum class TaskStatus(
    val text: String,
    val tag: Boolean,
    val tagClass: String? = null,
) {
    CANNOT_START_YET("Cannot start yet", false),
    NOT_YET_STARTED("Not yet started", true, "govuk-tag--blue"),
    IN_PROGRESS("In progress", true, "govuk-tag--light-blue"),
    COMPLETED("Completed", false),
}
