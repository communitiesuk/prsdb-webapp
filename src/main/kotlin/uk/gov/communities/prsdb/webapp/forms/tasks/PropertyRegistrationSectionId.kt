package uk.gov.communities.prsdb.webapp.forms.tasks

enum class PropertyRegistrationSectionId(
    override val sectionNumber: Int,
) : SectionId {
    PROPERTY_DETAILS(1),
    CHECK_AND_SUBMIT(2),
}
