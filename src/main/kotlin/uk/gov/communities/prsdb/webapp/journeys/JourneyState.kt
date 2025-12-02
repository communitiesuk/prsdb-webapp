package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.forms.PageData

interface JourneyState {
    fun getStepData(key: String): PageData?

    fun addStepData(
        key: String,
        value: PageData,
    )

    val journeyId: String

    fun deleteJourney()
}
