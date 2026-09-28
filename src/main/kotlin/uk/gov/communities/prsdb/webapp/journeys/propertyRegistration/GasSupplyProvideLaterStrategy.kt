package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.featureFlags.DisabledFeatureFlagSelector
import uk.gov.communities.prsdb.webapp.config.featureFlags.EnabledFeatureFlagSelector
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagSelector
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasCertOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSupplyOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyMode
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@PrsdbFlip(name = DELEGATE_TO_LETTING_AGENT, alterBean = "gas-supply-provide-later-flag-on")
interface GasSupplyProvideLaterStrategy : FeatureFlagSelector {
    fun gasSupplyOutcome(state: GasSafetyDetailState): GasSupplyOutcome?

    fun gasSupplyOutcomeStep(state: GasSafetyDetailState): JourneyStep.RequestableStep<*, *, *>

    fun gasCertOutcome(state: GasSafetyDetailState): GasCertOutcome?

    fun gasCertOutcomeStep(state: GasSafetyDetailState): JourneyStep.RequestableStep<*, *, *>
}

@Primary
@PrsdbWebService("gas-supply-provide-later-flag-off")
class GasSupplyProvideLaterStrategyImplFlagOff :
    DisabledFeatureFlagSelector(),
    GasSupplyProvideLaterStrategy {
    override fun gasSupplyOutcome(state: GasSafetyDetailState) =
        when (state.beforePdjb1022HasGasSupplyStep.outcome) {
            YesOrNo.YES -> GasSupplyOutcome.HAS_SUPPLY
            YesOrNo.NO -> GasSupplyOutcome.NO_SUPPLY
            null -> null
        }

    override fun gasSupplyOutcomeStep(state: GasSafetyDetailState) = state.beforePdjb1022HasGasSupplyStep

    override fun gasCertOutcome(state: GasSafetyDetailState) =
        when (state.beforePdjb1022HasGasCertStep.outcome) {
            BeforePdjb1022HasGasCertMode.HAS_CERTIFICATE -> GasCertOutcome.HAS_CERTIFICATE
            BeforePdjb1022HasGasCertMode.NO_CERTIFICATE -> GasCertOutcome.NO_CERTIFICATE
            BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER -> GasCertOutcome.PROVIDE_LATER
            null -> null
        }

    override fun gasCertOutcomeStep(state: GasSafetyDetailState) = state.beforePdjb1022HasGasCertStep
}

@PrsdbWebService("gas-supply-provide-later-flag-on")
class GasSupplyProvideLaterStrategyImplFlagOn :
    EnabledFeatureFlagSelector(),
    GasSupplyProvideLaterStrategy {
    override fun gasSupplyOutcome(state: GasSafetyDetailState) =
        when (state.hasGasSupplyStep.outcome) {
            HasGasSupplyMode.HAS_SUPPLY -> GasSupplyOutcome.HAS_SUPPLY
            HasGasSupplyMode.NO_SUPPLY -> GasSupplyOutcome.NO_SUPPLY
            HasGasSupplyMode.PROVIDE_LATER -> GasSupplyOutcome.PROVIDE_LATER
            null -> null
        }

    override fun gasSupplyOutcomeStep(state: GasSafetyDetailState) = state.hasGasSupplyStep

    override fun gasCertOutcome(state: GasSafetyDetailState) =
        when (state.hasGasCertStep.outcome) {
            HasGasCertMode.YES -> GasCertOutcome.HAS_CERTIFICATE
            HasGasCertMode.NO -> GasCertOutcome.NO_CERTIFICATE
            null -> null
        }

    override fun gasCertOutcomeStep(state: GasSafetyDetailState) = state.hasGasCertStep
}
