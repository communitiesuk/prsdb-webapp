package uk.gov.communities.prsdb.webapp.journeys.joinProperty.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.PropertySearchResultDataModel
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep

interface AddressSearchState : JourneyState {
    var searchResults: List<PropertySearchResultDataModel>?

    val findPropertyStep: FindPropertyStep
    val noMatchingPropertiesStep: NoMatchingPropertiesStep
    val selectPropertyStep: SelectPropertyStep
    val propertyNotRegisteredStep: PropertyNotRegisteredStep
}
