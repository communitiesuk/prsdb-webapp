package uk.gov.communities.prsdb.webapp.multipageforms

sealed class StepId(
    val urlPathSegment: String,
)

sealed class LandlordRegistrationStepId(
    urlPathSegment: String,
) : StepId(urlPathSegment) {
    data object Email : LandlordRegistrationStepId("email")

    data object PhoneNumber : LandlordRegistrationStepId("phone-number")
}
