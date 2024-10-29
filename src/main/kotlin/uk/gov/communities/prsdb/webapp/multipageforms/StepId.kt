package uk.gov.communities.prsdb.webapp.multipageforms

interface StepId {
    val urlPathSegment: String
}

enum class RegisterLandlordStepId(
    override val urlPathSegment: String,
) : StepId {
    Email("email"),
    QuickBreak("quick-break"),
    PhoneNumber("phone-number"),
}
