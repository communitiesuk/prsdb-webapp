package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import java.util.Optional

fun JourneyData.emailUpdateIfPresent() =
    if (this.containsKey(UpdateDetailsStepId.UpdateEmail.urlPathSegment)) {
        Optional.of(
            JourneyDataHelper.getFieldStringValue(
                this,
                UpdateDetailsStepId.UpdateEmail.urlPathSegment,
                "emailAddress",
            )!!,
        )
    } else {
        Optional.empty()
    }
