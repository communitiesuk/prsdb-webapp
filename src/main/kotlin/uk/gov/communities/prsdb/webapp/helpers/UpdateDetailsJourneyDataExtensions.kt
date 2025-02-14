package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId

fun JourneyData.getEmailUpdateIfPresent() =
    JourneyDataHelper.getFieldStringValue(
        this,
        UpdateDetailsStepId.UpdateEmail.urlPathSegment,
        "emailAddress",
    )
