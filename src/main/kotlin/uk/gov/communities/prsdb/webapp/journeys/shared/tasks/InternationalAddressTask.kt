package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import uk.gov.communities.prsdb.webapp.constants.enums.AddressSearchRegion
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InternationalAddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

/**
 * An address task for adresses that may be outside the UK. A screener yes/no question sends international addresses to the manual
 * address task immediately
 */
abstract class InternationalAddressTask(
    journeyStateService: JourneyStateService,
    override val addressInUkStep: AddressInUkStep,
    lookupAddressStep: LookupAddressStep,
    selectAddressStep: SelectAddressStep,
    noAddressFoundStep: NoAddressFoundStep,
    manualAddressStep: ManualAddressStep,
    override val internationalManualAddressStep: ManualAddressStep,
) : TaskWithoutDependencies<InternationalAddressState>(journeyStateService),
    InternationalAddressState {
    // Must set the screener's fieldSetHeading, as AddressInUkStepConfig has no default
    protected abstract val addressInUkContentProperties: Map<String, Any?>

    // Message key for the screener's error when no answer is selected, as AddressInUkStepConfig has no default
    protected abstract val addressInUkMissingAnswerErrorKey: String
    protected open val lookupAddressContentProperties: Map<String, Any?> = emptyMap()
    protected open val selectAddressContentProperties: Map<String, Any?> = emptyMap()
    protected open val manualAddressContentProperties: Map<String, Any?> = emptyMap()

    // Defaults to the UK manual address content; a getter so it reads the subclass's override after initialisation
    protected open val internationalManualAddressContentProperties: Map<String, Any?>
        get() = manualAddressContentProperties

    protected open val lookupAddressTemplate: String = LookupAddressStepConfig.DEFAULT_TEMPLATE

    // The hooks are read through getters (not copied at construction) because subclass overrides aren't initialised
    // until after this class's constructor has run
    final override val addressTask: AddressTask =
        object : AddressTask(
            journeyStateService,
            lookupAddressStep,
            selectAddressStep,
            noAddressFoundStep,
            manualAddressStep,
        ) {
            override val lookupAddressContentProperties get() = this@InternationalAddressTask.lookupAddressContentProperties
            override val selectAddressContentProperties get() = this@InternationalAddressTask.selectAddressContentProperties
            override val manualAddressContentProperties get() = this@InternationalAddressTask.manualAddressContentProperties
            override val lookupAddressTemplate get() = this@InternationalAddressTask.lookupAddressTemplate

            // International addresses implicitly have no address filters
            override val searchRegion = AddressSearchRegion.UK
        }

    override val taskState get() = this

    fun clearFormData() {
        addressInUkStep.clearFormData()
        internationalManualAddressStep.clearFormData()
        addressTask.clearFormData()
    }

    override fun makeSubJourney(state: InternationalAddressState) =
        subJourney(state) {
            step<YesOrNo, AddressInUkStepConfig>(journey.addressInUkStep) {
                routeSegment(AddressInUkStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.addressTask.firstStep
                        YesOrNo.NO -> journey.internationalManualAddressStep
                    }
                }
                stepSpecificInitialisation { withMissingAnswerError(addressInUkMissingAnswerErrorKey) }
                withAdditionalContentProperties { addressInUkContentProperties }
            }
            task(journey.addressTask) {
                parents { journey.addressInUkStep.hasOutcome(YesOrNo.YES) }
                nextStep { exitStep }
            }
            step(journey.internationalManualAddressStep) {
                routeSegment(INTERNATIONAL_MANUAL_ADDRESS_ROUTE_SEGMENT)
                parents { journey.addressInUkStep.hasOutcome(YesOrNo.NO) }
                nextStep { exitStep }
                withAdditionalContentProperties { internationalManualAddressContentProperties }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.addressTask.isComplete(),
                        journey.internationalManualAddressStep.isComplete(),
                    )
                }
            }
        }

    companion object {
        const val INTERNATIONAL_MANUAL_ADDRESS_ROUTE_SEGMENT = "non-uk-address"
    }
}
