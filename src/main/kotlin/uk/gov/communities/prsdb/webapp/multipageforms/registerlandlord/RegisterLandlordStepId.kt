package uk.gov.communities.prsdb.webapp.multipageforms.registerlandlord

import uk.gov.communities.prsdb.webapp.multipageforms.StepId

enum class RegisterLandlordStepId(
    override val urlPathSegment: String,
) : StepId {
    Email("email"),
    QuickBreak("quick-break"),
    BestFriendEmail("best-friend-email"),
    ReviewPhoneNumbers("check-phone-numbers"),
    PhoneNumber("phone-number"),
}
