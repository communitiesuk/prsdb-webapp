package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckAutomatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.SearchForEpcStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

interface EpcState : JourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestionStep: EpcQuestionStep
    val checkAutomatchedEpcStep: CheckAutomatchedEpcStep
    val searchForEpcStep: SearchForEpcStep
    val epcSupersededStep: EpcSupersededStep
    val checkSearchedEpcStep: CheckMatchedEpcStep
    val epcNotFoundStep: EpcNotFoundStep
}
