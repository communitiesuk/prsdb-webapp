package uk.gov.communities.prsdb.webapp.journeys.example

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel

interface OccupiedJourneyState : JourneyState {
    val occupied: JourneyStep<*, OccupancyFormModel, *>?
    val households: JourneyStep<*, NumberOfHouseholdsFormModel, *>?
    val tenants: JourneyStep<*, NumberOfPeopleFormModel, *>?
}

interface EpcJourneyState : JourneyState {
    var automatchedEpc: EpcDataModel?
    var searchedEpc: EpcDataModel?
    val propertyId: Long

    val epcQuestion: JourneyStep<*, EpcFormModel, *>?
    val checkAutomatchedEpc: JourneyStep<*, CheckMatchedEpcFormModel, *>?
    val searchForEpc: JourneyStep<*, EpcLookupFormModel, *>?
    val epcNotFound: JourneyStep<*, NoInputFormModel, *>?
    val epcSuperseded: JourneyStep<*, NoInputFormModel, *>?
    val checkSearchedEpc: JourneyStep<*, CheckMatchedEpcFormModel, *>?
}
