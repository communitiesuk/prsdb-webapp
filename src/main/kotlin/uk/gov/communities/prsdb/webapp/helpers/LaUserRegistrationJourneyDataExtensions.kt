package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId

object LaUserRegistrationJourneyDataExtensions {
    fun JourneyData.getName() =
        getFieldStringValue(
            RegisterLaUserStepId.Name.urlPathSegment,
            "name",
        )

    fun JourneyData.getEmail() =
        getFieldStringValue(
            RegisterLaUserStepId.Email.urlPathSegment,
            "emailAddress",
        )
}
