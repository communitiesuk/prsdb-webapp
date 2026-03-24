package uk.gov.communities.prsdb.webapp.journeys.joinProperty.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyByPrnStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.JoinPropertyAlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectedPropertyCheckResult
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStepConfig

@JourneyFrameworkComponent
class AddressSearchTask : Task<JoinPropertyAddressSearchState>() {
    override fun makeSubJourney(state: JoinPropertyAddressSearchState) =
        subJourney(state) {
            step<LookupAddressMode, LookupAddressStepConfig>(journey.lookupAddressStep) {
                routeSegment(LookupAddressStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectPropertyStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noMatchingPropertiesStep
                    }
                }
                stepSpecificInitialisation {
                    restrictToEngland()
                }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.lookupAddress.joinProperty.fieldSetHeading",
                        "postcodeHint" to "forms.lookupAddress.joinProperty.postcode.hint",
                        "houseNameOrNumberLabel" to "forms.lookupAddress.joinProperty.houseNameOrNumber.label",
                        "submitButtonText" to "forms.buttons.findProperty",
                        "postcodeFieldWidth" to 20,
                        "houseNameOrNumberFieldWidth" to 20,
                        "alternativeActionUrl" to FindPropertyByPrnStep.ROUTE_SEGMENT,
                        "alternativeActionText" to "forms.lookupAddress.joinProperty.alternativeAction",
                    )
                }
            }
            step(journey.noMatchingPropertiesStep) {
                routeSegment(NoMatchingPropertiesStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                noNextDestination()
            }
            step(journey.selectPropertyStep) {
                routeSegment(SelectPropertyStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { journey.checkSelectedPropertyStep }
            }
            step(journey.checkSelectedPropertyStep) {
                parents { journey.selectPropertyStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        SelectedPropertyCheckResult.PROPERTY_NOT_REGISTERED -> journey.propertyNotRegisteredStep
                        SelectedPropertyCheckResult.ALREADY_LANDLORD -> journey.alreadyRegisteredStep
                        SelectedPropertyCheckResult.ELIGIBLE_TO_JOIN -> exitStep
                    }
                }
            }
            step(journey.propertyNotRegisteredStep) {
                routeSegment(PropertyNotRegisteredStep.ROUTE_SEGMENT)
                parents { journey.checkSelectedPropertyStep.hasOutcome(SelectedPropertyCheckResult.PROPERTY_NOT_REGISTERED) }
                noNextDestination()
            }
            step(journey.alreadyRegisteredStep) {
                routeSegment(JoinPropertyAlreadyRegisteredStep.ROUTE_SEGMENT)
                parents { journey.checkSelectedPropertyStep.hasOutcome(SelectedPropertyCheckResult.ALREADY_LANDLORD) }
                noNextDestination()
            }
            exitStep {
                parents { journey.checkSelectedPropertyStep.hasOutcome(SelectedPropertyCheckResult.ELIGIBLE_TO_JOIN) }
            }
        }
}
