package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateEpcJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateEpcJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
            val propertyCompliance =
                propertyOwnership.propertyCompliance
                    ?: throw PrsdbWebException("Property ownership $propertyId does not have a compliance record")

            state.propertyId = propertyId
            state.lastModifiedDate = propertyCompliance.getMostRecentlyUpdated().toString()
            state.isOccupied = propertyOwnership.isOccupied
            state.uprn = propertyOwnership.address.uprn
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state, propertyId)
        } else {
            checkYourAnswersJourneyMap(state, propertyId)
        }
    }

    private fun mainJourneyMap(
        state: UpdateEpcJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyComplianceRoute = PropertyDetailsController.getPropertyCompliancePath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyComplianceRoute }
            task(journey.epcDetailsTask) {
                withDependencies { journey }
                initialStep()
                backUrl { propertyComplianceRoute }
                nextStep { journey.updateCheckEpcAnswersStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "sectionHeaderInfo" to null,
                    )
                }
            }
            step(journey.updateCheckEpcAnswersStep) {
                routeSegment(UpdateCheckEpcAnswersStep.ROUTE_SEGMENT)
                parents { journey.epcDetailsTask.isComplete() }
                nextStep { journey.completeEpcUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                    )
                }
            }
            step(journey.completeEpcUpdateStep) {
                parents { journey.updateCheckEpcAnswersStep.isComplete() }
                nextUrl { propertyComplianceRoute }
            }
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: UpdateEpcJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyComplianceRoute = PropertyDetailsController.getPropertyCompliancePath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyComplianceRoute }
            configure {
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "sectionHeaderInfo" to null,
                    )
                }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            checkAnswerTask(
                journey.epcDetailsTask,
                { journey },
            )

            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class UpdateEpcJourney(
    journeyStateService: JourneyStateService,
    journeyName: String = "updateEpc",
    val updateCheckEpcAnswersStep: UpdateCheckEpcAnswersStep,
    override val epcDetailsTask: EpcDetailsTask,
    override val completeEpcUpdateStep: CompleteEpcUpdateStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateEpcJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateEpcJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var isOccupied: Boolean by delegateProvider.requiredImmutableDelegate("isOccupied")
    override var uprn: Long? by delegateProvider.nullableDelegate("uprn")

    override val allowProvideCertificateLaterRoute: Boolean = false

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val cyaStep get() = updateCheckEpcAnswersStep
}

interface UpdateEpcJourneyState :
    JourneyState,
    EpcDependencies,
    CheckYourAnswersJourneyState {
    val epcDetailsTask: EpcDetailsTask
    val propertyId: Long
    val lastModifiedDate: String
    override var isOccupied: Boolean
    val completeEpcUpdateStep: CompleteEpcUpdateStep
}
