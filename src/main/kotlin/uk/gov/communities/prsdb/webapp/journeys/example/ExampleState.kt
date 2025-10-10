package uk.gov.communities.prsdb.webapp.journeys.example

import uk.gov.communities.prsdb.webapp.journeys.AbstractStep
import uk.gov.communities.prsdb.webapp.journeys.DynamicJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel

interface FooJourneyState :
    OccupiedJourneyState,
    EpcJourneyState

interface OccupiedJourneyState : DynamicJourneyState {
    val occupied: AbstractStep<*, OccupancyFormModel, *>?
    val households: AbstractStep<*, NumberOfHouseholdsFormModel, *>?
    val tenants: AbstractStep<*, NumberOfPeopleFormModel, *>?
}

interface EpcJourneyState : DynamicJourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestion: AbstractStep<*, EpcFormModel, *>?
    val checkAutomatchedEpc: AbstractStep<*, CheckMatchedEpcFormModel, *>?
    val searchForEpc: AbstractStep<*, EpcLookupFormModel, *>?
    val epcNotFound: AbstractStep<*, NoInputFormModel, *>?
    val epcSuperseded: AbstractStep<*, NoInputFormModel, *>?
    val checkSearchedEpc: AbstractStep<*, CheckMatchedEpcFormModel, *>?
}
