package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.forms.PageData

interface DynamicJourneyState {
    fun getStepData(key: String): PageData?

    fun addStepData(
        key: String,
        value: PageData,
    )
}
