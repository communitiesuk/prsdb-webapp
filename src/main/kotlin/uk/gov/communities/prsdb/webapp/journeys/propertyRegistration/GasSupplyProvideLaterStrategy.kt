package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.ReservedTagValues
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasCertOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSupplyOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

/**
 * All the behaviour that differs between the old (letting-agent-delegation flag-off) and new (flag-on) gas-supply/gas-cert
 * step pairs is defined here, keyed by which pair is currently active. This lets [uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask]
 * stay ignorant of the old `BeforePdjb1022*` step pair entirely, so this whole file (and the old step pair) can be deleted
 * in one go once the DELEGATE_TO_LETTING_AGENT flag is removed.
 */
@PrsdbFlip(name = DELEGATE_TO_LETTING_AGENT, alterBean = "gas-supply-provide-later-flag-on")
interface GasSupplyProvideLaterStrategy {
    fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T

    fun gasSupplyOutcome(state: GasSafetyDetailState): GasSupplyOutcome?

    fun gasSupplyOutcomeStep(state: GasSafetyDetailState): JourneyStep.RequestableStep<*, *, *>

    fun gasCertOutcome(state: GasSafetyDetailState): GasCertOutcome?

    fun gasCertOutcomeStep(state: GasSafetyDetailState): JourneyStep.RequestableStep<*, *, *>

    fun configureSteps(builder: SubJourneyBuilder<GasSafetyDetailState>)

    fun gasCertIssueDateParent(state: GasSafetyDetailState): Parentage

    fun gasCertMissingParent(state: GasSafetyDetailState): Parentage

    fun provideGasCertLaterParent(state: GasSafetyDetailState): Parentage

    fun gasSupplyExitParent(state: GasSafetyDetailState): Parentage
}

@Primary
@PrsdbWebService("gas-supply-provide-later-flag-off")
class GasSupplyProvideLaterStrategyImplFlagOff : GasSupplyProvideLaterStrategy {
    override fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T = ifDisabled()

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

    override fun configureSteps(builder: SubJourneyBuilder<GasSafetyDetailState>) {
        with(builder) {
            step(journey.beforePdjb1022HasGasSupplyStep) {
                routeSegment(BeforePdjb1022HasGasSupplyStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.beforePdjb1022HasGasCertStep
                        YesOrNo.NO -> exitStep
                    }
                }
                taggedWith(ReservedTagValues.SAVABLE)
            }
            step(journey.beforePdjb1022HasGasCertStep) {
                routeSegment(BeforePdjb1022HasGasCertStep.ROUTE_SEGMENT)
                parents { journey.beforePdjb1022HasGasSupplyStep.hasOutcome(YesOrNo.YES) }
                nextStep { mode ->
                    when (mode) {
                        BeforePdjb1022HasGasCertMode.HAS_CERTIFICATE -> journey.gasCertIssueDateStep
                        BeforePdjb1022HasGasCertMode.NO_CERTIFICATE -> journey.gasCertMissingStep
                        BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER -> journey.provideGasCertLaterStep
                    }
                }
                taggedWith(ReservedTagValues.SAVABLE)
            }
        }
    }

    override fun gasCertIssueDateParent(state: GasSafetyDetailState) =
        state.beforePdjb1022HasGasCertStep.hasOutcome(BeforePdjb1022HasGasCertMode.HAS_CERTIFICATE)

    override fun gasCertMissingParent(state: GasSafetyDetailState) =
        state.beforePdjb1022HasGasCertStep.hasOutcome(BeforePdjb1022HasGasCertMode.NO_CERTIFICATE)

    override fun provideGasCertLaterParent(state: GasSafetyDetailState) =
        state.beforePdjb1022HasGasCertStep.hasOutcome(BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER)

    override fun gasSupplyExitParent(state: GasSafetyDetailState) = state.beforePdjb1022HasGasSupplyStep.hasOutcome(YesOrNo.NO)
}

@PrsdbWebService("gas-supply-provide-later-flag-on")
class GasSupplyProvideLaterStrategyImplFlagOn : GasSupplyProvideLaterStrategy {
    override fun <T> ifEnabledOrElse(
        ifEnabled: () -> T,
        ifDisabled: () -> T,
    ): T = ifEnabled()

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

    override fun configureSteps(builder: SubJourneyBuilder<GasSafetyDetailState>) {
        with(builder) {
            step(journey.hasGasSupplyStep) {
                routeSegment(HasGasSupplyStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        HasGasSupplyMode.HAS_SUPPLY -> journey.hasGasCertStep
                        HasGasSupplyMode.NO_SUPPLY -> exitStep
                        HasGasSupplyMode.PROVIDE_LATER -> journey.provideGasCertLaterStep
                    }
                }
                taggedWith(ReservedTagValues.SAVABLE)
            }
            step(journey.hasGasCertStep) {
                routeSegment(HasGasCertStep.ROUTE_SEGMENT)
                parents { journey.hasGasSupplyStep.hasOutcome(HasGasSupplyMode.HAS_SUPPLY) }
                nextStep { mode ->
                    when (mode) {
                        HasGasCertMode.YES -> journey.gasCertIssueDateStep
                        HasGasCertMode.NO -> journey.gasCertMissingStep
                    }
                }
                taggedWith(ReservedTagValues.SAVABLE)
            }
        }
    }

    override fun gasCertIssueDateParent(state: GasSafetyDetailState) = state.hasGasCertStep.hasOutcome(HasGasCertMode.YES)

    override fun gasCertMissingParent(state: GasSafetyDetailState) = state.hasGasCertStep.hasOutcome(HasGasCertMode.NO)

    override fun provideGasCertLaterParent(state: GasSafetyDetailState) = state.hasGasSupplyStep.hasOutcome(HasGasSupplyMode.PROVIDE_LATER)

    override fun gasSupplyExitParent(state: GasSafetyDetailState) = state.hasGasSupplyStep.hasOutcome(HasGasSupplyMode.NO_SUPPLY)
}
