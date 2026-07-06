package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.InstanceableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.doesNotHaveOutcome
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.ScopedAddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

// FLAVOUR A: gives the lead trustee the full address-lookup experience by reusing the shared address steps as a
// second, routed instance of the address task. The task's own state type is AddressState (not the org state), so
// it is added to the org journey via the DSL's `instancedTask` (see OrgLandlordRegistrationTask). `createScopedState`
// returns a ScopedAddressState which supplies fresh step objects and route-prefixes every address key, so the
// trustee's address never collides with the landlord's own address - all without adding per-instance fields to the
// journey state. Compare with the flavour B approach, which duplicates the address steps and variables as named
// fields on the state plus a hand-written per-field adapter.
@JourneyFrameworkComponent
class OrgLandlordLeadTrusteeAddressTask(
    private val journeyStateService: JourneyStateService,
    private val lookupAddressStepFactory: ObjectFactory<LookupAddressStep>,
    private val selectAddressStepFactory: ObjectFactory<SelectAddressStep>,
    private val noAddressFoundStepFactory: ObjectFactory<NoAddressFoundStep>,
    private val manualAddressStepFactory: ObjectFactory<ManualAddressStep>,
) : Task<AddressState>(),
    InstanceableTask<AddressState> {
    override fun createScopedState(
        delegate: JourneyState,
        routeSegment: String,
    ): AddressState =
        ScopedAddressState(
            routeSegment,
            journeyStateService,
            delegate,
            lookupAddressStepFactory.getObject(),
            selectAddressStepFactory.getObject(),
            noAddressFoundStepFactory.getObject(),
            manualAddressStepFactory.getObject(),
        )

    override fun makeSubJourney(state: AddressState) =
        subJourney(state) {
            step(journey.lookupAddressStep) {
                routeSegment(LookupAddressStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectAddressStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noAddressFoundStep
                    }
                }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.lookupAddress.trusteeRegistration.fieldSetHeading",
                        "fieldSetHint" to "forms.lookupAddress.trusteeRegistration.fieldSetHint",
                    )
                }
            }
            step(journey.selectAddressStep) {
                routeSegment(SelectAddressStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        SelectAddressMode.MANUAL_ADDRESS -> journey.manualAddressStep
                        else -> exitStep
                    }
                }
            }
            step(journey.noAddressFoundStep) {
                routeSegment(NoAddressFoundStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { journey.manualAddressStep }
            }
            step(journey.manualAddressStep) {
                routeSegment(ManualAddressStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.noAddressFoundStep.isComplete(),
                    )
                }
                nextStep { exitStep }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.manualAddress.trusteeRegistration.fieldSetHeading",
                        "fieldSetHint" to null,
                    )
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.selectAddressStep.doesNotHaveOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.manualAddressStep.isComplete(),
                    )
                }
            }
        }

    companion object {
        const val ROUTE_SEGMENT = "lead-trustee-address"
    }
}
