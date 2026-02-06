package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

interface EpcState : JourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestionStep: EpcQuestionStep
}
