package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT

enum class UpdateLandlordDetailsStepId(
    override val urlPathSegment: String,
) : StepId {
    UpdateEmail("email"),
    UpdateName("name"),
    UpdatePhoneNumber("phone-number"),
    LookupEnglandAndWalesAddress("lookup-address"),
    SelectEnglandAndWalesAddress("select-address"),
    ManualEnglandAndWalesAddress("manual-address"),
    UpdateDetails(DETAILS_PATH_SEGMENT),
}
