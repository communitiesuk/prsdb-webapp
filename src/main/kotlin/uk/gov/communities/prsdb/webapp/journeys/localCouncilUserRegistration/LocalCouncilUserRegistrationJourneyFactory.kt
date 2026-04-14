package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController.Companion.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.stepConfig.LandingPageStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.stepConfig.LocalCouncilUserCyaStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService

@PrsdbWebService
class LocalCouncilUserRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<LocalCouncilUserRegistrationJourney>,
) {
    fun createJourneySteps(token: String): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.invitationToken = token
            state.isStateInitialized = true
        }

        if (state.invitationToken != token) {
            throw PrsdbWebException("Journey state token ${state.invitationToken} does not match provided token $token")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: LocalCouncilUserRegistrationJourney,
        checkingAnswersFor: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configure {
                withAdditionalContentProperty { "title" to "registerLocalCouncilUser.title" }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            when (checkingAnswersFor) {
                "name" -> checkAnswerStep(journey.nameStep, "name")
                "email" -> checkAnswerStep(journey.emailStep, "email")
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }

    private fun mainJourneyMap(state: LocalCouncilUserRegistrationJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepStep { journey.privacyNoticeStep }
            configure {
                withAdditionalContentProperty { "title" to "registerLocalCouncilUser.title" }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            step(journey.privacyNoticeStep) {
                routeSegment(PRIVACY_NOTICE_PATH_SEGMENT)
                initialStep()
                nextStep { journey.landingPageStep }
            }
            step(journey.landingPageStep) {
                routeSegment(LANDING_PAGE_PATH_SEGMENT)
                parents { journey.privacyNoticeStep.isComplete() }
                nextStep { journey.nameStep }
            }
            step(journey.nameStep) {
                routeSegment("name")
                parents { journey.landingPageStep.isComplete() }
                nextStep { journey.emailStep }
            }
            step(journey.emailStep) {
                routeSegment("email")
                parents { journey.nameStep.isComplete() }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment("check-answers")
                parents { journey.emailStep.isComplete() }
                nextUrl { "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
        }

    fun initializeJourneyState(token: String): String = stateFactory.getObject().initializeState(token)
}

@JourneyFrameworkComponent
class LocalCouncilUserRegistrationJourney(
    override val landingPageStep: LandingPageStep,
    override val privacyNoticeStep: PrivacyNoticeStep,
    override val nameStep: NameStep,
    override val emailStep: EmailStep,
    override val cyaStep: LocalCouncilUserCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    private val invitationService: LocalCouncilInvitationService,
    override val stateFactory: ObjectFactory<LocalCouncilUserRegistrationJourneyState>,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    LocalCouncilUserRegistrationJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var cyaJourneys: Map<String, String> = mapOf()
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var invitationToken: String by delegateProvider.requiredImmutableDelegate("invitationToken")
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    override var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val invitation: LocalCouncilInvitation
        get() = invitationService.getValidInvitationFromToken(invitationToken)

    override fun generateJourneyId(seed: Any?): String {
        val token = seed as? String
        return super<AbstractJourneyState>.generateJourneyId(
            token?.let { "LC user reg journey for token $it" },
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
    override val finishCyaStep: FinishCyaJourneyStep
    override val cyaStep: LocalCouncilUserCyaStep
    val invitationToken: String
    val invitation: LocalCouncilInvitation
}
