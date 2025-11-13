package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectAddressMode

@PrsdbWebComponent
@Scope("prototype")
class AddressTask : Task<AddressState>() {
    override fun makeSubJourney(state: AddressState) =
        subJourney(state) {
            startingStep("lookup-address", journey.lookupStep) {
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectAddressStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noAddressFoundStep
                    }
                }
            }
            step("select-address", journey.selectAddressStep) {
                parents { journey.lookupStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        SelectAddressMode.MANUAL_ADDRESS -> journey.manualAddressStep
                        SelectAddressMode.ADDRESS_ALREADY_REGISTERED -> journey.alreadyRegisteredStep
                        SelectAddressMode.ADDRESS_SELECTED -> exitStep
                    }
                }
            }
            step("no-address-found", journey.noAddressFoundStep) {
                parents { journey.lookupStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { journey.manualAddressStep }
            }
            step("manual-address", journey.manualAddressStep) {
                parents {
                    OrParents(
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.noAddressFoundStep.isComplete(),
                    )
                }
                nextStep { journey.localAuthorityStep }
            }
            step("already-registered", journey.alreadyRegisteredStep) {
                parents { journey.selectAddressStep.hasOutcome(SelectAddressMode.ADDRESS_ALREADY_REGISTERED) }
                nextStep { throw IllegalStateException("Journey ends here for already registered addresses") }
            }
            step("local-authority", journey.localAuthorityStep) {
                parents { journey.manualAddressStep.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.localAuthorityStep.hasOutcome(Complete.COMPLETE),
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.ADDRESS_SELECTED),
                    )
                }
            }
        }
}
