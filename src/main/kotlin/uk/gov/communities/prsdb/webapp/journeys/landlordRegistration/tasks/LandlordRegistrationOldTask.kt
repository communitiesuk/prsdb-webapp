package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep

@JourneyFrameworkComponent
class LandlordRegistrationOldTask : Task<LandlordRegistrationState>() {
    override fun makeSubJourney(state: LandlordRegistrationState) =
        subJourney(state) {
            step(journey.privacyNoticeStep) {
                routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                nextStep { journey.identityTask.firstStep }
            }
            task(journey.identityTask) {
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.landlordRegistrationForNotOrgLandlordTask.firstStep }
            }
            task(journey.landlordRegistrationForNotOrgLandlordTask) {
                parents { journey.identityTask.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.landlordRegistrationForNotOrgLandlordTask.isComplete() }
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
                        checkAnswerStep(journey.nameStep, NameStep.ROUTE_SEGMENT)
                    }

                    DateOfBirthStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.dateOfBirthStep, DateOfBirthStep.ROUTE_SEGMENT)
                    }

                    EmailStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.emailStep, EmailStep.ROUTE_SEGMENT)
                    }

                    PhoneNumberStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(journey.phoneNumberStep, PhoneNumberStep.ROUTE_SEGMENT)
                    }

                    CountryOfResidenceStep.ROUTE_SEGMENT -> {
                        checkAnswerStep(
                            journey.countryOfResidenceStep,
                            CountryOfResidenceStep.ROUTE_SEGMENT,
                        )
                    }

                    LookupAddressStep.ROUTE_SEGMENT -> {
                        checkAnswerTask(journey.addressTask)
                    }
                }
                step(journey.finishCyaStep) {
                    initialStep()
                    nextDestination { Destination.Nowhere() }
                }
            }
    }
}
