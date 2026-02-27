package uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.NewRegisterLocalCouncilUserController.Companion.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE
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
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.LandingPageStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.LocalCouncilUserCheckableElements
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.LocalCouncilUserCyaStep
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.steps.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService

@PrsdbWebService
class NewLocalCouncilUserRegistrationJourneyFactory(
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
        checkingAnswersFor: LocalCouncilUserCheckableElements,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configure {
                withAdditionalContentProperty { "title" to "registerLocalCouncilUser.title" }
            }
            configureFirst {
                backDestination { journey.returnToCyaPageDestination }
            }
            when (checkingAnswersFor) {
                LocalCouncilUserCheckableElements.NAME -> checkAnswerStep(journey.nameStep, "name")
                LocalCouncilUserCheckableElements.EMAIL -> checkAnswerStep(journey.emailStep, "email")
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }

    private fun mainJourneyMap(state: LocalCouncilUserRegistrationJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
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
                parents { journey.landingPageStep.isComplete() }
                nextStep { journey.nameStep }
            }
            step(journey.nameStep) {
                routeSegment("name")
                parents { journey.privacyNoticeStep.isComplete() }
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
    override val finishCyaStep: FinishCyaJourneyStep<LocalCouncilUserCheckableElements>,
    private val invitationService: LocalCouncilInvitationService,
    private val objectFactory: ObjectFactory<LocalCouncilUserRegistrationJourneyState>,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    LocalCouncilUserRegistrationJourneyState {
    override var cyaJourneys: Map<LocalCouncilUserCheckableElements, String> by delegateProvider.requiredDelegate(
        "checkYourAnswersChildJourneyId",
        mapOf(),
    )
    override var checkingAnswersFor: LocalCouncilUserCheckableElements? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var invitationToken: String by delegateProvider.requiredImmutableDelegate("invitationToken")
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    private var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override var returnToCyaPageDestination: Destination
        get() = cyaRouteSegment?.let { Destination.StepRoute(it, baseJourneyId) } ?: Destination.Nowhere()
        set(destination) {
            cyaRouteSegment =
                when (destination) {
                    is Destination.StepRoute -> destination.routeSegment
                    is Destination.VisitableStep -> destination.step.routeSegment
                    else -> null
                }
        }

    override val invitation: LocalCouncilInvitation
        get() = invitationService.getValidInvitationFromToken(invitationToken)

    override fun createChildJourneyState(cyaJourneyId: String): LocalCouncilUserRegistrationJourneyState {
        copyJourneyTo(cyaJourneyId)
        return objectFactory.getObject().apply { setJourneyId(cyaJourneyId) }
    }

    override fun generateJourneyId(seed: Any?): String {
        val token = seed as? String
        return super<AbstractJourneyState>.generateJourneyId(
            token?.let { "LC user reg journey for token $it" },
        )
    }
}

interface LocalCouncilUserRegistrationJourneyState :
    JourneyState,
    CheckYourAnswersJourneyState<LocalCouncilUserCheckableElements> {
    val landingPageStep: LandingPageStep
    val privacyNoticeStep: PrivacyNoticeStep
    val nameStep: NameStep
    val emailStep: EmailStep
    override val finishCyaStep: FinishCyaJourneyStep<LocalCouncilUserCheckableElements>
    override val cyaStep: LocalCouncilUserCyaStep
    val invitationToken: String
    val invitation: LocalCouncilInvitation
}
