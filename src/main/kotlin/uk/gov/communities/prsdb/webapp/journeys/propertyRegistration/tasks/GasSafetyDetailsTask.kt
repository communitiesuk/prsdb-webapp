package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.GasSupplyProvideLaterStrategy
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasCertOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSupplyOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyOrProvideLaterMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyOrProvideLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveGasCertUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState

@JourneyFrameworkComponent("propertyRegistrationGasSafetyDetailsTask")
class GasSafetyDetailsTask(
    override val hasUploadedCert: HasAnyInCollectionStep,
    override val hasGasSupplyStep: HasGasSupplyStep,
    override val beforePdjb1022HasGasCertStep: BeforePdjb1022HasGasCertStep,
    override val hasGasSupplyOrProvideLaterStep: HasGasSupplyOrProvideLaterStep,
    override val hasGasCertStep: HasGasCertStep,
    override val gasCertIssueDateStep: GasCertIssueDateStep,
    override val uploadGasCertStep: UploadGasCertStep,
    override val checkGasCertUploadsStep: CheckGasCertUploadsStep,
    override val removeGasCertUploadStep: RemoveGasCertUploadStep,
    override val gasCertExpiredStep: GasCertExpiredStep,
    override val gasCertMissingStep: GasCertMissingStep,
    override val provideGasCertLaterStep: ProvideGasCertLaterStep,
    journeyStateService: JourneyStateService,
    private val gasSupplyProvideLaterStrategy: GasSupplyProvideLaterStrategy,
) : Task<GasSafetyDetailState, GasSafetyDependencies>(journeyStateService),
    GasSafetyDetailState {
    override val taskState: GasSafetyDetailState
        get() = this

    override val isOccupied: Boolean
        get() = dependencies.isOccupied
    override val allowProvideCertificateLaterRoute: Boolean
        get() = dependencies.allowProvideCertificateLaterRoute

    override var gasUploadMap: Map<Int, CertificateUpload> by delegateProvider.requiredDelegate("gasUploadMap", mapOf())
    override var highestAssignedGasMemberId: Int? by delegateProvider.nullableDelegate("highestGasUploadMemberId")

    override val gasSupplyOutcome: GasSupplyOutcome?
        get() =
            gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                ifEnabled = {
                    when (hasGasSupplyOrProvideLaterStep.outcome) {
                        HasGasSupplyOrProvideLaterMode.HAS_SUPPLY -> GasSupplyOutcome.HAS_SUPPLY
                        HasGasSupplyOrProvideLaterMode.NO_SUPPLY -> GasSupplyOutcome.NO_SUPPLY
                        HasGasSupplyOrProvideLaterMode.PROVIDE_LATER -> GasSupplyOutcome.PROVIDE_LATER
                        null -> null
                    }
                },
                ifDisabled = {
                    when (hasGasSupplyStep.outcome) {
                        YesOrNo.YES -> GasSupplyOutcome.HAS_SUPPLY
                        YesOrNo.NO -> GasSupplyOutcome.NO_SUPPLY
                        null -> null
                    }
                },
            )

    override val gasSupplyOutcomeStep: JourneyStep.RequestableStep<*, *, *>
        get() =
            gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                ifEnabled = { hasGasSupplyOrProvideLaterStep },
                ifDisabled = { hasGasSupplyStep },
            )

    override val gasCertOutcome: GasCertOutcome?
        get() =
            gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                ifEnabled = {
                    when (hasGasCertStep.outcome) {
                        HasGasCertMode.YES -> GasCertOutcome.HAS_CERTIFICATE
                        HasGasCertMode.NO -> GasCertOutcome.NO_CERTIFICATE
                        null -> null
                    }
                },
                ifDisabled = {
                    when (beforePdjb1022HasGasCertStep.outcome) {
                        BeforePdjb1022HasGasCertMode.HAS_CERTIFICATE -> GasCertOutcome.HAS_CERTIFICATE
                        BeforePdjb1022HasGasCertMode.NO_CERTIFICATE -> GasCertOutcome.NO_CERTIFICATE
                        BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER -> GasCertOutcome.PROVIDE_LATER
                        null -> null
                    }
                },
            )

    override val gasCertOutcomeStep: JourneyStep.RequestableStep<*, *, *>
        get() =
            gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                ifEnabled = { hasGasCertStep },
                ifDisabled = { beforePdjb1022HasGasCertStep },
            )

    override fun makeSubJourney(state: GasSafetyDetailState) =
        subJourney(state) {
            gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                ifEnabled = {
                    step(journey.hasGasSupplyOrProvideLaterStep) {
                        routeSegment(HasGasSupplyOrProvideLaterStep.ROUTE_SEGMENT)
                        nextStep { mode ->
                            when (mode) {
                                HasGasSupplyOrProvideLaterMode.HAS_SUPPLY -> journey.hasGasCertStep
                                HasGasSupplyOrProvideLaterMode.NO_SUPPLY -> exitStep
                                HasGasSupplyOrProvideLaterMode.PROVIDE_LATER -> journey.provideGasCertLaterStep
                            }
                        }
                        savable()
                    }
                    step(journey.hasGasCertStep) {
                        routeSegment(HasGasCertStep.ROUTE_SEGMENT)
                        parents { journey.hasGasSupplyOrProvideLaterStep.hasOutcome(HasGasSupplyOrProvideLaterMode.HAS_SUPPLY) }
                        nextStep { mode ->
                            when (mode) {
                                HasGasCertMode.YES -> journey.gasCertIssueDateStep
                                HasGasCertMode.NO -> journey.gasCertMissingStep
                            }
                        }
                        savable()
                    }
                },
                ifDisabled = {
                    step(journey.hasGasSupplyStep) {
                        routeSegment(HasGasSupplyStep.ROUTE_SEGMENT)
                        nextStep { mode ->
                            when (mode) {
                                YesOrNo.YES -> journey.beforePdjb1022HasGasCertStep
                                YesOrNo.NO -> exitStep
                            }
                        }
                        savable()
                    }
                    step(journey.beforePdjb1022HasGasCertStep) {
                        routeSegment(BeforePdjb1022HasGasCertStep.ROUTE_SEGMENT)
                        parents { journey.hasGasSupplyStep.hasOutcome(YesOrNo.YES) }
                        nextStep { mode ->
                            when (mode) {
                                BeforePdjb1022HasGasCertMode.HAS_CERTIFICATE -> journey.gasCertIssueDateStep
                                BeforePdjb1022HasGasCertMode.NO_CERTIFICATE -> journey.gasCertMissingStep
                                BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER -> journey.provideGasCertLaterStep
                            }
                        }
                        savable()
                    }
                },
            )

            step(journey.gasCertIssueDateStep) {
                routeSegment(GasCertIssueDateStep.ROUTE_SEGMENT)
                parents {
                    gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                        ifEnabled = { journey.hasGasCertStep.hasOutcome(HasGasCertMode.YES) },
                        ifDisabled = { journey.beforePdjb1022HasGasCertStep.hasOutcome(BeforePdjb1022HasGasCertMode.HAS_CERTIFICATE) },
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE -> journey.hasUploadedCert
                        GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED -> journey.gasCertExpiredStep
                    }
                }
                savable()
            }
            step<AnyMembers, HasAnyInCollectionStepConfig>(journey.hasUploadedCert) {
                parents { journey.gasCertIssueDateStep.hasOutcome(GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE) }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.uploadGasCertStep
                        AnyMembers.SOME_MEMBERS -> journey.checkGasCertUploadsStep
                    }
                }
                stepSpecificInitialisation { collectionMap = journey.gasUploadMap }
            }
            step(journey.uploadGasCertStep) {
                routeSegment(UploadGasCertStep.ROUTE_SEGMENT)
                parents { journey.gasCertIssueDateStep.hasOutcome(GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE) }
                nextStep { journey.checkGasCertUploadsStep }
                savable()
            }
            step(journey.checkGasCertUploadsStep) {
                routeSegment(CheckGasCertUploadsStep.ROUTE_SEGMENT)
                parents { journey.uploadGasCertStep.isComplete() }
                nextStep { exitStep }
                backDestination {
                    val cyaState = dependencies as? CheckYourAnswersJourneyState
                    if (cyaState?.isCheckingAnswers == true) {
                        cyaState.returnToCyaPageDestination
                    } else {
                        Destination(journey.gasCertIssueDateStep)
                    }
                }
                savable()
            }
            step(journey.removeGasCertUploadStep) {
                parents {
                    journey.hasUploadedCert.hasOutcome(AnyMembers.SOME_MEMBERS)
                }
                backStep { journey.checkGasCertUploadsStep }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> journey.checkGasCertUploadsStep
                        AnyMembers.NO_MEMBERS -> journey.uploadGasCertStep
                    }
                }
                routeSegment(RemoveGasCertUploadStep.ROUTE_SEGMENT)
                savable()
            }
            step(journey.gasCertExpiredStep) {
                routeSegment(GasCertExpiredStep.ROUTE_SEGMENT)
                parents {
                    journey.gasCertIssueDateStep.hasOutcome(GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED)
                }
                nextStep { exitStep }
                savable()
            }
            step(journey.gasCertMissingStep) {
                routeSegment(GasCertMissingStep.ROUTE_SEGMENT)
                parents {
                    gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                        ifEnabled = { journey.hasGasCertStep.hasOutcome(HasGasCertMode.NO) },
                        ifDisabled = { journey.beforePdjb1022HasGasCertStep.hasOutcome(BeforePdjb1022HasGasCertMode.NO_CERTIFICATE) },
                    )
                }
                nextStep { exitStep }
                savable()
            }
            step(journey.provideGasCertLaterStep) {
                routeSegment(ProvideGasCertLaterStep.ROUTE_SEGMENT)
                parents {
                    gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                        ifEnabled = { journey.hasGasSupplyOrProvideLaterStep.hasOutcome(HasGasSupplyOrProvideLaterMode.PROVIDE_LATER) },
                        ifDisabled = { journey.beforePdjb1022HasGasCertStep.hasOutcome(BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER) },
                    )
                }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        gasSupplyProvideLaterStrategy.ifEnabledOrElse(
                            ifEnabled = { journey.hasGasSupplyOrProvideLaterStep.hasOutcome(HasGasSupplyOrProvideLaterMode.NO_SUPPLY) },
                            ifDisabled = { journey.hasGasSupplyStep.hasOutcome(YesOrNo.NO) },
                        ),
                        journey.provideGasCertLaterStep.isComplete(),
                        journey.gasCertMissingStep.isComplete(),
                        journey.gasCertExpiredStep.isComplete(),
                        journey.checkGasCertUploadsStep.isComplete(),
                    )
                }
            }
        }
}
