package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId

class LaUserRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getName(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                RegisterLaUserStepId.Name.urlPathSegment,
                "name",
            )

        fun getEmail(journeyData: JourneyData) =
            getFieldStringValue(
                journeyData,
                RegisterLaUserStepId.Email.urlPathSegment,
                "emailAddress",
            )
    }
}
