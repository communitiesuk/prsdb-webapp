package uk.gov.communities.prsdb.webapp.journeys.joinProperty.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep
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
                        "fieldSetHint" to "forms.lookupAddress.joinProperty.fieldSetHint",
                    )
                }
            }
            step(journey.noMatchingPropertiesStep) {
                routeSegment(NoMatchingPropertiesStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { exitStep }
            }
            // TODO: PDJB-275 - Add conditional routing to error pages
            step(journey.selectPropertyStep) {
                routeSegment(SelectPropertyStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { journey.propertyNotRegisteredStep }
            }
            // TODO: PDJB-283 - Connect when property is not registered
            step(journey.propertyNotRegisteredStep) {
                routeSegment(PropertyNotRegisteredStep.ROUTE_SEGMENT)
                parents { journey.selectPropertyStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.propertyNotRegisteredStep.isComplete(),
                        journey.noMatchingPropertiesStep.isComplete(),
                    )
                }
            }
        }
}
