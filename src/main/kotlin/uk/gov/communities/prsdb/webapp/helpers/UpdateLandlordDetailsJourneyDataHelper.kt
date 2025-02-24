package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId

class UpdateLandlordDetailsJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getEmailUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateDetailsStepId.UpdateEmail.urlPathSegment,
                "emailAddress",
            )

        fun getNameUpdateIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateDetailsStepId.UpdateName.urlPathSegment,
                "name",
            )

        fun getPhoneNumberIfPresent(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                UpdateDetailsStepId.UpdatePhoneNumber.urlPathSegment,
                "phoneNumber",
            )
    }
}
