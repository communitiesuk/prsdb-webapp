package uk.gov.communities.prsdb.webapp.constants.enums

import uk.gov.communities.prsdb.webapp.forms.tasks.SectionId

enum class PropertyRegistrationSection(
    override val sectionNumber: Int,
) : SectionId {
    PROPERTY_DETAILS(1),
    CHECK_AND_SUBMIT(2),
}
