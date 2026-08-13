package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.checkAnswersChangeJourneys.OrgCompaniesHouseChangeGovBodyTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.checkAnswersChangeJourneys.orgCompaniesHouseChangeCyaJourney
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeChangeDestination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeChangeRedirectStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeTrustInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeUpdateRouteMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeUpdateRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeUpdateRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.OrgAddressTask

@JourneyFrameworkComponent
class LandlordRegistrationTask(
    private val featureFlagManager: FeatureFlagManager,
    override val identityTask: IdentityTask,
    override val emailStep: EmailStep,
    override val phoneNumberStep: PhoneNumberStep,
    override val individualLandlordLocationTask: IndividualLandlordLocationTask,
    override val orgLandlordRegistrationTask: OrgLandlordRegistrationTask,
    override val landlordTypeStep: LandlordTypeStep,
    override val landlordTypeChangeRedirectStep: LandlordTypeChangeRedirectStep,
    override val privacyNoticeStep: PrivacyNoticeStep,
    override val cyaStep: LandlordRegistrationCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep,
    override val orgCompaniesHouseChangeGovBodyTask: OrgCompaniesHouseChangeGovBodyTask,
    override val orgTypeUpdateRoutingStep: OrgTypeUpdateRoutingStep,
    override val orgTypeTrustInterruptionStep: OrgTypeTrustInterruptionStep,
    journeyStateService: JourneyStateService,
    override val stateFactory: ObjectFactory<LandlordRegistrationTask>,
) : TaskWithoutDependencies<LandlordRegistrationState>(journeyStateService),
    LandlordRegistrationState {
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
        get() = orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep

    override val orgTypeStep: OrgTypeStep
        get() = orgLandlordRegistrationTask.orgTypeStep

    override val taskState get() = this

    override fun makeSubJourney(state: LandlordRegistrationState) =
        if (featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)) {
            makeRestructuredLandlordSubJourney(state)
        } else {
            makeLegacyLandlordSubJourney(state)
        }

    private fun makeLegacyLandlordSubJourney(state: LandlordRegistrationState): SubJourneyBuilder<LandlordRegistrationState> =
        subJourney(state) {
            step(journey.privacyNoticeStep) {
                routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                nextStep { journey.identityTask.firstStep }
            }
            task(journey.identityTask) {
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.emailStep }
            }
            step(journey.emailStep) {
                routeSegment(EmailStep.ROUTE_SEGMENT)
                parents { journey.identityTask.isComplete() }
                nextStep { journey.phoneNumberStep }
            }
            step(journey.phoneNumberStep) {
                routeSegment(PhoneNumberStep.ROUTE_SEGMENT)
                parents { journey.emailStep.isComplete() }
                nextStep { journey.individualLandlordLocationTask.firstStep }
            }
            task(journey.individualLandlordLocationTask) {
                parents { journey.phoneNumberStep.isComplete() }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment(AbstractCheckYourAnswersStep.ROUTE_SEGMENT)
                parents { journey.individualLandlordLocationTask.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.cyaStep.isComplete() }
            }
        }

    private fun makeRestructuredLandlordSubJourney(state: LandlordRegistrationState): SubJourneyBuilder<LandlordRegistrationState> =
        subJourney(state) {
            step(journey.privacyNoticeStep) {
                routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                nextStep { journey.identityTask.firstStep }
            }
            task(journey.identityTask) {
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.emailStep }
            }
            step(journey.emailStep) {
                routeSegment(EmailStep.ROUTE_SEGMENT)
                parents { journey.identityTask.isComplete() }
                nextStep { journey.phoneNumberStep }
            }
            step(journey.phoneNumberStep) {
                routeSegment(PhoneNumberStep.ROUTE_SEGMENT)
                parents { journey.emailStep.isComplete() }
                nextStep { journey.landlordTypeStep }
            }
            step(journey.landlordTypeStep) {
                routeSegment(LandlordTypeStep.ROUTE_SEGMENT)
                parents { journey.phoneNumberStep.isComplete() }
                nextStep {
                    when (journey.landlordTypeStep.outcome) {
                        LandlordTypeMode.INDIVIDUAL -> journey.individualLandlordLocationTask.firstStep
                        LandlordTypeMode.ORGANISATION -> journey.orgLandlordRegistrationTask.firstStep
                        null -> journey.individualLandlordLocationTask.firstStep
                    }
                }
            }
            task(journey.orgLandlordRegistrationTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.ORGANISATION) }
                nextStep { journey.cyaStep }
            }
            task(journey.individualLandlordLocationTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL) }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment(AbstractCheckYourAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        AndParents(
                            journey.individualLandlordLocationTask.isComplete(),
                            journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL),
                        ),
                        AndParents(
                            journey.orgLandlordRegistrationTask.isComplete(),
                            journey.landlordTypeStep.hasOutcome(LandlordTypeMode.ORGANISATION),
                        ),
                    )
                }
                backUrl {
                    when (journey.landlordTypeStep.outcome) {
                        LandlordTypeMode.ORGANISATION -> journey.orgLandlordRegistrationTask.exitStep.backUrl
                        LandlordTypeMode.INDIVIDUAL -> journey.individualLandlordLocationTask.exitStep.backUrl
                        null -> throw IllegalStateException(
                            "landlordTypeStep must have an outcome for the check your answers step to be reachable",
                        )
                    }
                }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.cyaStep.isComplete() }
            }
        }

    companion object {
        fun <T : LandlordRegistrationState> checkYourAnswersJourneyMap(
            state: T,
            checkingAnswersFor: String,
        ): Map<String, StepLifecycleOrchestrator> =
            journey(state) {
                unreachableStepDestination { journey.returnToCyaPageDestination }
                configure {
                    withAdditionalContentProperty { "title" to "registerAsALandlord.title" }
                }
                configureFirst { backDestination { journey.returnToCyaPageDestination } }
                when (checkingAnswersFor) {
                    NameStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.identityTask.nameStep, NameStep.ROUTE_SEGMENT)
                    }

                    DateOfBirthStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.identityTask.dateOfBirthStep, DateOfBirthStep.ROUTE_SEGMENT)
                    }

                    EmailStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.emailStep, EmailStep.ROUTE_SEGMENT)
                    }

                    PhoneNumberStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.phoneNumberStep, PhoneNumberStep.ROUTE_SEGMENT)
                    }

                    CountryOfResidenceStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(
                            journey.individualLandlordLocationTask.countryOfResidenceStep,
                            CountryOfResidenceStep.ROUTE_SEGMENT,
                        )
                    }

                    LookupAddressStep.ROUTE_SEGMENT -> {
                        checkAnswerTask(journey.individualLandlordLocationTask.addressTask, null)
                    }

                    LandlordTypeStep.ROUTE_SEGMENT -> {
                        step(journey.landlordTypeStep) {
                            initialStep()
                            routeSegment(LandlordTypeStep.ROUTE_SEGMENT)
                            nextStep { journey.landlordTypeChangeRedirectStep }
                        }
                        step(journey.landlordTypeChangeRedirectStep) {
                            parents {
                                journey.landlordTypeStep.isComplete()
                            }
                            nextDestination { destination ->
                                when (destination) {
                                    LandlordTypeChangeDestination.CHECK_ANSWERS -> {
                                        Destination(journey.finishCyaStep)
                                    }

                                    LandlordTypeChangeDestination.INDIVIDUAL_TASK -> {
                                        Destination(journey.individualLandlordLocationTask.firstStep)
                                    }

                                    LandlordTypeChangeDestination.ORGANISATION_TASK -> {
                                        Destination(journey.orgLandlordRegistrationTask.firstStep)
                                    }
                                }
                            }
                        }
                        task(journey.individualLandlordLocationTask) {
                            parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL) }
                            nextStep { journey.finishCyaStep }
                        }
                        task(journey.orgLandlordRegistrationTask) {
                            parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.ORGANISATION) }
                            nextStep { journey.finishCyaStep }
                        }
                    }

                    OrgNameStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgNameStep, OrgNameStep.ROUTE_SEGMENT)
                    }

                    "${OrgAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}" -> {
                        checkAnswerTask(
                            journey.orgLandlordRegistrationTask.orgAddressTask,
                            OrgAddressTask.ROUTE_SEGMENT,
                        )
                    }

                    OrgEmailStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgEmailStep, OrgEmailStep.ROUTE_SEGMENT)
                    }

                    OrgPhoneNumberStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgPhoneNumberStep, OrgPhoneNumberStep.ROUTE_SEGMENT)
                    }

                    OrgTypeStep.ROUTE_SEGMENT -> {
                        step(journey.orgLandlordRegistrationTask.orgTypeStep) {
                            initialStep()
                            routeSegment(OrgTypeStep.ROUTE_SEGMENT)
                            nextStep { journey.orgTypeUpdateRoutingStep }
                        }
                        step<OrgTypeUpdateRouteMode, OrgTypeUpdateRoutingStepConfig>(journey.orgTypeUpdateRoutingStep) {
                            stepSpecificInitialisation {
                                usingPreviousIsTrust {
                                    getPreviousIsTrustFromBaseJourney(
                                        journey,
                                        journey.orgLandlordRegistrationTask.orgTypeStep,
                                    )
                                }
                            }
                            parents { journey.orgLandlordRegistrationTask.orgTypeStep.isComplete() }
                            nextDestination { mode ->
                                when (mode) {
                                    OrgTypeUpdateRouteMode.TRUST_UNCHANGED -> Destination(journey.finishCyaStep)
                                    OrgTypeUpdateRouteMode.ADDING_TRUST -> Destination(journey.orgTypeTrustInterruptionStep)
                                    OrgTypeUpdateRouteMode.REMOVING_TRUST -> Destination(journey.orgTypeTrustInterruptionStep)
                                }
                            }
                        }
                        step(journey.orgTypeTrustInterruptionStep) {
                            routeSegment(OrgTypeTrustInterruptionStep.ROUTE_SEGMENT)
                            parents {
                                OrParents(
                                    journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.ADDING_TRUST),
                                    journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.REMOVING_TRUST),
                                )
                            }
                            nextStep {
                                if (journey.orgTypeUpdateRoutingStep.outcome == OrgTypeUpdateRouteMode.ADDING_TRUST) {
                                    journey.orgLandlordRegistrationTask.leadTrusteeTask.firstStep
                                } else {
                                    journey.finishCyaStep
                                }
                            }
                        }
                        task(journey.orgLandlordRegistrationTask.leadTrusteeTask) {
                            parents {
                                AndParents(
                                    journey.orgTypeTrustInterruptionStep.isComplete(),
                                    journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.ADDING_TRUST),
                                )
                            }
                            nextStep { journey.finishCyaStep }
                        }
                    }

                    OrgIsRegisteredCharityStep.ROUTE_SEGMENT -> {
                        checkAnswerTask(journey.orgLandlordRegistrationTask.charityTask)
                    }

                    OrgCharityRegisteredWithStep.ROUTE_SEGMENT -> {
                        checkAnswerTask(journey.orgLandlordRegistrationTask.charityTask) {
                            configureStep(journey.orgLandlordRegistrationTask.charityTask.orgCharityRegisteredWithStep) {
                                backDestination { journey.returnToCyaPageDestination }
                            }
                        }
                    }

                    OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT,
                    -> {
                        checkAnswerStep(
                            journey.orgLandlordRegistrationTask.charityTask.orgCharityNumberEnglandAndWalesStep,
                            OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT,
                        )
                    }

                    OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT,
                    -> {
                        checkAnswerStep(
                            journey.orgLandlordRegistrationTask.charityTask.orgCharityNumberNorthernIrelandStep,
                            OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT,
                        )
                    }

                    OrgCharityNumberScotlandStep.ROUTE_SEGMENT,
                    -> {
                        checkAnswerStep(
                            journey.orgLandlordRegistrationTask.charityTask.orgCharityNumberScotlandStep,
                            OrgCharityNumberScotlandStep.ROUTE_SEGMENT,
                        )
                    }

                    OrgIsRegisteredCompanyStep.ROUTE_SEGMENT,
                    -> {
                        orgCompaniesHouseChangeCyaJourney()
                    }

                    OrgCompanyNumberStep.ROUTE_SEGMENT,
                    -> {
                        checkAnswerStep(
                            journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep,
                            OrgCompanyNumberStep.ROUTE_SEGMENT,
                        )
                    }

                    LeadTrusteeNameStep.ROUTE_SEGMENT -> {
                        checkAnswerTask(journey.orgLandlordRegistrationTask.leadTrusteeTask, null)
                    }

                    OrgGovBodyMemberListStep.ROUTE_SEGMENT -> {
                        checkAnswerTask(journey.orgLandlordRegistrationTask.orgGovBodyTask) {
                            configureStep(
                                journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.orgGovBodyMemberListStep,
                            ) {
                                backDestination { journey.returnToCyaPageDestination }
                            }
                        }
                    }

                    OrgMainContactStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgMainContactStep, OrgMainContactStep.ROUTE_SEGMENT)
                    }
                }
                step(journey.finishCyaStep) {
                    initialStep()
                    nextDestination { Destination.Nowhere() }
                }
            }
    }
}
