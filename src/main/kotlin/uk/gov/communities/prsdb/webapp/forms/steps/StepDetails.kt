package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.forms.JourneyData

data class StepDetails<T : StepId>(
    val step: Step<T>,
    val subPageNumber: Int?,
    val filteredJourneyData: JourneyData,
)
