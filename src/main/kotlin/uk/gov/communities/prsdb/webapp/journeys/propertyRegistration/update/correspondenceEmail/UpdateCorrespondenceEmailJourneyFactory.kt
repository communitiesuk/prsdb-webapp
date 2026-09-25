package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.correspondenceEmail

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CorrespondenceEmailState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@PrsdbWebService
class UpdateCorrespondenceEmailJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateCorrespondenceEmailJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val userToLandlordService: UserToLandlordService,
) {
    fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnershipService.getLastModifiedDate(propertyId).toString()
            // TODO PDJB-1738: Use the organisational sub-user's email, consistently with registration.
            state.loggedInLandlordEmailAtStartOfJourney = userToLandlordService.getCurrentLandlordForUser().email
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val returnUrl = PropertyDetailsController.getPropertyDetailsPath(propertyId)
        return journey(state) {
            unreachableStepUrl { returnUrl }
            configure {
                withAdditionalContentProperty { "title" to "propertyDetails.update.title" }
            }
            configureStep(journey.correspondenceEmailStep) {
                withAdditionalContentProperties {
                    mapOf(
                        "submitButtonText" to "forms.buttons.continue",
                        "sectionHeaderInfo" to
                            SectionHeaderViewModel(
                                sectionNameKey = "registerProperty.taskList.aboutYourProperty.correspondence",
                                sectionNumber = 0,
                                totalSections = 0,
                                useNumbering = false,
                            ),
                    )
                }
            }
            if (state.checkingAnswersFor == null) {
                step(journey.correspondenceEmailStep) {
                    initialStep()
                    routeSegment(CorrespondenceEmailStep.ROUTE_SEGMENT)
                    backUrl { returnUrl }
                    nextStep { journey.cyaStep }
                }
                step(journey.cyaStep) {
                    routeSegment(UpdateCorrespondenceEmailCyaStep.ROUTE_SEGMENT)
                    parents { journey.correspondenceEmailStep.isComplete() }
                    nextUrl { returnUrl }
                }
            } else {
                check(state.checkingAnswersFor == CorrespondenceEmailStep.ROUTE_SEGMENT) {
                    "Unknown checkable element ${state.checkingAnswersFor}"
                }
                checkAnswerStep(journey.correspondenceEmailStep, CorrespondenceEmailStep.ROUTE_SEGMENT) {
                    backDestination { journey.returnToCyaPageDestination }
                }
                step(journey.finishCyaStep) {
                    parents { journey.correspondenceEmailStep.isComplete() }
                    nextDestination { Destination.Nowhere() }
                }
            }
        }
    }

    fun initializeJourneyState(
        seed: Any?,
        currentLastModifiedDate: java.time.Instant,
    ): String = stateFactory.getObject().initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
}

@JourneyFrameworkComponent
class UpdateCorrespondenceEmailJourney(
    override val correspondenceEmailStep: CorrespondenceEmailStep,
    override val cyaStep: UpdateCorrespondenceEmailCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    override val stateFactory: ObjectFactory<UpdateCorrespondenceEmailJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, "correspondence email"),
    UpdateCorrespondenceEmailJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate(LAST_MODIFIED_DATE_KEY)
    override var loggedInLandlordEmailAtStartOfJourney: String by
        delegateProvider.requiredImmutableDelegate("loggedInLandlordEmailAtStartOfJourney")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
}

interface UpdateCorrespondenceEmailJourneyState :
    CheckYourAnswersJourneyState,
    CorrespondenceEmailState {
    override val cyaStep: UpdateCorrespondenceEmailCyaStep
    val propertyId: Long
    val lastModifiedDate: String
}
