package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@PrsdbWebComponent
@Scope("prototype")
class AddressTask : Task<AddressState>() {
    override fun makeSubJourney(state: AddressState) =
        subJourney(state) {
            step(journey.lookupStep) {
                routeSegment("lookup-address")
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectAddressStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noAddressFoundStep
                    }
                }
            }
            step(journey.selectAddressStep) {
                routeSegment("select-address")
                parents { journey.lookupStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        SelectAddressMode.MANUAL_ADDRESS -> journey.manualAddressStep
                        SelectAddressMode.ADDRESS_ALREADY_REGISTERED -> journey.alreadyRegisteredStep
                        SelectAddressMode.ADDRESS_SELECTED -> exitStep
                    }
                }
            }
            step(journey.noAddressFoundStep) {
                routeSegment("no-address-found")
                parents { journey.lookupStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { journey.manualAddressStep }
            }
            step(journey.manualAddressStep) {
                routeSegment("manual-address")
                parents {
                    OrParents(
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.noAddressFoundStep.isComplete(),
                    )
                }
                nextStep { journey.localCouncilStep }
            }
            step(journey.alreadyRegisteredStep) {
                routeSegment("already-registered")
                parents { journey.selectAddressStep.hasOutcome(SelectAddressMode.ADDRESS_ALREADY_REGISTERED) }
                noNextDestination()
            }
            step(journey.localCouncilStep) {
                routeSegment("local-council")
                parents { journey.manualAddressStep.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
            }
            exitStep {
                savable()
                parents {
                    OrParents(
                        journey.localCouncilStep.hasOutcome(Complete.COMPLETE),
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.ADDRESS_SELECTED),
                    )
                }
            }
        }
}
