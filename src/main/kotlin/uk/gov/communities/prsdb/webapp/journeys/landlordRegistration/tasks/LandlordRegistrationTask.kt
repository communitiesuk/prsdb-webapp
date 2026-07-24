package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.duplicableCheckAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep

@JourneyFrameworkComponent
class LandlordRegistrationTask(
    private val featureFlagManager: FeatureFlagManager,
    override val identityTask: IdentityTask,
    override val individualLandlordRegistrationTask: IndividualLandlordRegistrationTask,
    override val orgLandlordRegistrationTask: OrgLandlordRegistrationTask,
    override val landlordTypeStep: LandlordTypeStep,
    override val privacyNoticeStep: PrivacyNoticeStep,
    override val cyaStep: LandlordRegistrationCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    override val stateFactory: ObjectFactory<LandlordRegistrationTask>,
) : DuplicableTask<LandlordRegistrationState>(journeyStateService),
    LandlordRegistrationState {
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val taskState get() = this

    override fun makeSubJourney(state: LandlordRegistrationState) =
        if (featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION)) {
            makeOrgLandlordSubJourney(state)
        } else {
            makeIndividualLandlordSubJourney(state)
        }

    private fun makeIndividualLandlordSubJourney(state: LandlordRegistrationState): SubJourneyBuilder<LandlordRegistrationState> =
        subJourney(state) {
            step(journey.privacyNoticeStep) {
                routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                nextStep { journey.identityTask.firstStep }
            }
            duplicableTask(journey.identityTask) {
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.individualLandlordRegistrationTask.firstStep }
            }
            duplicableTask(journey.individualLandlordRegistrationTask) {
                parents { journey.identityTask.isComplete() }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment(AbstractCheckYourAnswersStep.ROUTE_SEGMENT)
                parents { journey.individualLandlordRegistrationTask.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.cyaStep.isComplete() }
            }
        }

    private fun makeOrgLandlordSubJourney(state: LandlordRegistrationState): SubJourneyBuilder<LandlordRegistrationState> =
        subJourney(state) {
            step(journey.privacyNoticeStep) {
                routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                nextStep { journey.identityTask.firstStep }
            }
            duplicableTask(journey.identityTask) {
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.landlordTypeStep }
            }
            step(journey.landlordTypeStep) {
                routeSegment(LandlordTypeStep.ROUTE_SEGMENT)
                parents { journey.identityTask.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        LandlordTypeMode.INDIVIDUAL -> journey.individualLandlordRegistrationTask.firstStep
                        LandlordTypeMode.ORGANISATION -> journey.orgLandlordRegistrationTask.firstStep
                    }
                }
            }
            duplicableTask(journey.orgLandlordRegistrationTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.ORGANISATION) }
                nextStep { journey.cyaStep }
            }
            duplicableTask(journey.individualLandlordRegistrationTask) {
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL) }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment(AbstractCheckYourAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.individualLandlordRegistrationTask.isComplete(),
                        journey.orgLandlordRegistrationTask.isComplete(),
                    )
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
                        checkAnswerStep(journey.individualLandlordRegistrationTask.emailStep, EmailStep.ROUTE_SEGMENT)
                    }

                    PhoneNumberStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.individualLandlordRegistrationTask.phoneNumberStep, PhoneNumberStep.ROUTE_SEGMENT)
                    }

                    CountryOfResidenceStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(
                            journey.individualLandlordRegistrationTask.countryOfResidenceStep,
                            CountryOfResidenceStep.ROUTE_SEGMENT,
                        )
                    }

                    LookupAddressStep.ROUTE_SEGMENT -> {
                        duplicableCheckAnswerTask(journey.individualLandlordRegistrationTask.addressTask, null)
                    }

                    LandlordTypeStep.ROUTE_SEGMENT -> {
                        step(journey.landlordTypeStep) {
                            initialStep()
                            routeSegment(LandlordTypeStep.ROUTE_SEGMENT)
                            nextStep { mode ->
                                when (mode) {
                                    LandlordTypeMode.INDIVIDUAL -> journey.individualLandlordRegistrationTask.firstStep
                                    LandlordTypeMode.ORGANISATION -> journey.finishCyaStep
                                }
                            }
                        }
                        duplicableTask(journey.individualLandlordRegistrationTask) {
                            parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL) }
                            nextStep { journey.finishCyaStep }
                        }
                    }

                    OrgNameStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgNameStep, OrgNameStep.ROUTE_SEGMENT)
                    }

                    OrgAddressStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgAddressStep, OrgAddressStep.ROUTE_SEGMENT)
                    }

                    OrgEmailStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgEmailStep, OrgEmailStep.ROUTE_SEGMENT)
                    }

                    OrgPhoneNumberStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgPhoneNumberStep, OrgPhoneNumberStep.ROUTE_SEGMENT)
                    }

                    OrgTypeStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.orgLandlordRegistrationTask.orgTypeStep, OrgTypeStep.ROUTE_SEGMENT)
                    }

                    OrgCharityStep.ROUTE_SEGMENT,
                    OrgCharityRegisteredWithStep.ROUTE_SEGMENT,
                    OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT,
                    OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT,
                    OrgCharityNumberScotlandStep.ROUTE_SEGMENT,
                    -> {
                        duplicableCheckAnswerTask(journey.orgLandlordRegistrationTask.charityTask, null)
                    }

                    OrgCompaniesHouseStep.ROUTE_SEGMENT,
                    OrgCompanyNumberStep.ROUTE_SEGMENT,
                    -> {
                        duplicableCheckAnswerTask(journey.orgLandlordRegistrationTask.companiesHouseTask, null)
                    }

                    LeadTrusteeNameStep.ROUTE_SEGMENT -> {
                        duplicableCheckAnswerTask(journey.orgLandlordRegistrationTask.leadTrusteeTask, null)
                    }

                    OrgGovBodyMemberListStep.ROUTE_SEGMENT -> {
                        duplicableCheckAnswerTask(journey.orgLandlordRegistrationTask.companiesHouseTask.orgGovBodyTask, null)
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
