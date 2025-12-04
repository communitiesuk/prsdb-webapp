package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.forms.PageData

interface JourneyState {
    fun getStepData(key: String): PageData?

    fun addStepData(
        key: String,
        value: PageData,
    )

    fun getSubmittedStepData(): Map<String, Any?>

    val journeyId: String
    val journeyMetadata: JourneyMetadata

    fun deleteJourney()
}
