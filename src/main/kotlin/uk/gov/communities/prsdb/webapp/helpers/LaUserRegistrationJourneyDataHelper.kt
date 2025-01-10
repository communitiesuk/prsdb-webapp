package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LaUserRegistrationJourneyDataHelper : JourneyDataHelper() {
    companion object {
        fun getName(journeyData: JourneyData) =
            JourneyDataService.getFieldStringValue(
                journeyData,
                RegisterLaUserStepId.Name.urlPathSegment,
                "name",
            )

        fun getEmail(journeyData: JourneyData) =
            JourneyDataService.getFieldStringValue(
                journeyData,
                RegisterLaUserStepId.Email.urlPathSegment,
                "emailAddress",
            )
    }
}
