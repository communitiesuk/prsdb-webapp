package uk.gov.communities.prsdb.webapp.journeys.joinProperty.states

import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyByPrnStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressSearchState

interface JoinPropertyAddressSearchState : AddressSearchState {
    val selectPropertyStep: SelectPropertyStep
    val noMatchingPropertiesStep: NoMatchingPropertiesStep
    val propertyNotRegisteredStep: PropertyNotRegisteredStep

    // PRN search step is accessible from the no-matching-properties page
    val findPropertyByPrnStep: FindPropertyByPrnStep
}
