package uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels

import uk.gov.communities.prsdb.webapp.forms.JourneyData

data class SetJourneyDataRequestModel(
    val journeyDataKey: String,
    val journeyData: JourneyData,
)
