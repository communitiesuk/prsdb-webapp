package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DeleteJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationTask
import java.security.Principal

@JourneyFrameworkComponent("landlordRegistrationJourneyFactory")
class LandlordRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<LandlordRegistrationJourneyState>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        val checkingAnswersFor = state.landlordRegistrationTask.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            LandlordRegistrationTask.checkYourAnswersJourneyMap(state.landlordRegistrationTask, checkingAnswersFor)
        }
    }

    private fun mainJourneyMap(state: LandlordRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            configureFirst { backDestination { Destination.ExternalUrl(LANDLORD_REGISTRATION_START_PAGE_ROUTE) } }
            unreachableStepStep { journey.landlordRegistrationTask.privacyNoticeStep }
            configure {
                withAdditionalContentProperty { "title" to "registerAsALandlord.title" }
            }
            section {
                withHeadingMessageKey("registerAsALandlord.caption", shouldUseNumbering = false)
                duplicableTask(journey.landlordRegistrationTask) {
                    initialStep()
                    nextStep { journey.deleteJourneyStep }
                }
            }
            step(journey.deleteJourneyStep) {
                parents { journey.landlordRegistrationTask.isComplete() }
                nextUrl { LANDLORD_REGISTRATION_CONFIRMATION_ROUTE }
            }
        }

    fun initializeJourneyState(user: Principal) = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent("landlordRegistrationJourney")
class LandlordRegistrationJourney(
    override val landlordRegistrationTask: LandlordRegistrationTask,
    override val deleteJourneyStep: DeleteJourneyStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    LandlordRegistrationJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(user) })
    }

    companion object {
        private fun generateSeedForUser(user: Principal): String =
            "Landlord registration journey for ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface LandlordRegistrationJourneyState : JourneyState {
    val landlordRegistrationTask: LandlordRegistrationTask
    val deleteJourneyStep: DeleteJourneyStep
}
