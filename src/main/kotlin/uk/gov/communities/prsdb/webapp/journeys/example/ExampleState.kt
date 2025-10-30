package uk.gov.communities.prsdb.webapp.journeys.example

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.SearchEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

interface OccupiedJourneyState : JourneyState {
    val occupied: OccupiedStep
    val households: HouseholdStep
    val tenants: TenantsStep
}

interface EpcJourneyState : JourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestion: EpcQuestionStep
    val checkAutomatchedEpc: CheckEpcStep
    val searchForEpc: SearchEpcStep
    val epcNotFound: EpcNotFoundStep
    val epcSuperseded: EpcSupersededStep
    val checkSearchedEpc: CheckEpcStep
}
