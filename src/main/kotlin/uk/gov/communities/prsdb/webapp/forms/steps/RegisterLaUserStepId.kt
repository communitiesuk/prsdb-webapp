package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT

enum class RegisterLaUserStepId(
    override val urlPathSegment: String,
) : StepId {
    LandingPage(LANDING_PAGE_PATH_SEGMENT),
    PrivacyNotice(PRIVACY_NOTICE_PATH_SEGMENT),
    Name("name"),
    Email("email"),
    CheckAnswers("check-answers"),
}
