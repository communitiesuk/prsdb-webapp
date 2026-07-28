package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyRegistrationAddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@JourneyFrameworkComponent
class PropertyRegistrationAddressTask(
    journeyStateService: JourneyStateService,
    override val lookupAddressStep: LookupAddressStep,
    override val selectAddressStep: SelectAddressStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val manualAddressStep: ManualAddressStep,
    override val alreadyRegisteredStep: AlreadyRegisteredStep,
    override val localCouncilStep: LocalCouncilStep,
) : DuplicableTask<PropertyRegistrationAddressState>(journeyStateService),
    PropertyRegistrationAddressState {
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("cachedAddresses")
    override var cachedSelectedAddress: String? by delegateProvider.nullableDelegate("cachedSelectedAddress")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("isAddressAlreadyRegistered")

    override val taskState get() = this

    override fun makeSubJourney(state: PropertyRegistrationAddressState) =
        subJourney(state) {
            step<LookupAddressMode, LookupAddressStepConfig>(journey.lookupAddressStep) {
                routeSegment(LookupAddressStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        LookupAddressMode.ADDRESSES_FOUND -> journey.selectAddressStep
                        LookupAddressMode.NO_ADDRESSES_FOUND -> journey.noAddressFoundStep
                    }
                }
                stepSpecificInitialisation {
                    restrictToEngland()
                }
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.lookupAddress.propertyRegistration.fieldSetHeading",
                        "fieldSetHint" to "forms.lookupAddress.propertyRegistration.fieldSetHint",
                    )
                }
            }
            step(journey.selectAddressStep) {
                routeSegment(SelectAddressStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.ADDRESSES_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        SelectAddressMode.MANUAL_ADDRESS -> journey.manualAddressStep
                        SelectAddressMode.ADDRESS_ALREADY_REGISTERED -> journey.alreadyRegisteredStep
                        SelectAddressMode.ADDRESS_SELECTED -> exitStep
                    }
                }
            }
            step<Complete, NoAddressFoundStepConfig>(journey.noAddressFoundStep) {
                routeSegment(NoAddressFoundStep.ROUTE_SEGMENT)
                parents { journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND) }
                nextStep { journey.manualAddressStep }
                stepSpecificInitialisation {
                    restrictToEngland()
                }
            }
            step(journey.manualAddressStep) {
                routeSegment(ManualAddressStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.MANUAL_ADDRESS),
                        journey.noAddressFoundStep.isComplete(),
                    )
                }
                nextStep { journey.localCouncilStep }
                withAdditionalContentProperty { "fieldSetHeading" to "forms.manualAddress.propertyRegistration.fieldSetHeading" }
            }
            step(journey.alreadyRegisteredStep) {
                routeSegment(AlreadyRegisteredStep.ROUTE_SEGMENT)
                parents { journey.selectAddressStep.hasOutcome(SelectAddressMode.ADDRESS_ALREADY_REGISTERED) }
                noNextDestination()
            }
            step(journey.localCouncilStep) {
                routeSegment(LocalCouncilStep.ROUTE_SEGMENT)
                parents { journey.manualAddressStep.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.localCouncilStep.hasOutcome(Complete.COMPLETE),
                        journey.selectAddressStep.hasOutcome(SelectAddressMode.ADDRESS_SELECTED),
                    )
                }
            }
        }
}
