package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController.Companion.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.LandingPageStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.LocalCouncilUserCyaStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.NameStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable

@PrsdbWebService
class NewLocalCouncilUserRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<LocalCouncilUserRegistrationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.landingPageStep }
            configure {
                withAdditionalContentProperty { "title" to "registerLocalCouncilUser.title" }
            }
            step(journey.landingPageStep) {
                routeSegment(LANDING_PAGE_PATH_SEGMENT)
                initialStep()
                nextStep { journey.privacyNoticeStep }
            }
            step(journey.privacyNoticeStep) {
                routeSegment(PRIVACY_NOTICE_PATH_SEGMENT)
                parents { journey.landingPageStep.always() }
                nextStep { journey.nameStep }
                checkable()
            }
            step(journey.nameStep) {
                routeSegment("name")
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.emailStep }
                checkable()
            }
            step(journey.emailStep) {
                routeSegment("email")
                parents { journey.nameStep.isComplete() }
                nextStep { journey.cyaStep }
                checkable()
            }
            step(journey.cyaStep) {
                routeSegment("check-answers")
                parents { journey.emailStep.isComplete() }
                nextUrl { "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
            checkYourAnswersJourney()
        }
    }

    fun initializeJourneyState(invitation: LocalCouncilInvitation): String =
        stateFactory.getObject().initializeState(invitation.token.toString())
}

@JourneyFrameworkComponent
class LocalCouncilUserRegistrationJourney(
    override val landingPageStep: LandingPageStep,
    override val privacyNoticeStep: PrivacyNoticeStep,
    override val nameStep: NameStep,
    override val emailStep: EmailStep,
    override val cyaStep: LocalCouncilUserCyaStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    LocalCouncilUserRegistrationJourneyState {
    override var cyaChildJourneyIdIfInitialized: String? by delegateProvider.nullableDelegate("checkYourAnswersChildJourneyId")

    override fun generateJourneyId(seed: Any?): String {
        val token = seed as? String
        return super<AbstractJourneyState>.generateJourneyId(
            token?.let { "LA user reg journey for token $it" },
        )
    }
}

interface LocalCouncilUserRegistrationJourneyState :
    JourneyState,
    CheckYourAnswersJourneyState {
    val landingPageStep: LandingPageStep
    val privacyNoticeStep: PrivacyNoticeStep
    val nameStep: NameStep
    val emailStep: EmailStep
    override val cyaStep: LocalCouncilUserCyaStep
}
