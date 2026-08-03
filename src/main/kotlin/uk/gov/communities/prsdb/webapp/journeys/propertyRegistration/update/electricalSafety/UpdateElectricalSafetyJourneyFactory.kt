package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety

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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateElectricalSafetyJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateElectricalSafetyJourney>,
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
            state.previousUploadIds = propertyCompliance.electricalSafetyFileUploads.map { it.id }
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
        state: UpdateElectricalSafetyJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyComplianceRoute = PropertyDetailsController.getPropertyCompliancePath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyComplianceRoute }
            task(journey.electricalSafetyDetailsTask) {
                withDependencies { journey }
                initialStep()
                backUrl { propertyComplianceRoute }
                nextStep { journey.updateCheckElectricalSafetyAnswersStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "sectionHeaderInfo" to null,
                    )
                }
            }
            step(journey.updateCheckElectricalSafetyAnswersStep) {
                routeSegment(UpdateCheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.electricalSafetyDetailsTask.isComplete() }
                nextStep { journey.completeElectricalSafetyUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                    )
                }
            }
            step(journey.completeElectricalSafetyUpdateStep) {
                parents { journey.updateCheckElectricalSafetyAnswersStep.isComplete() }
                nextUrl { propertyComplianceRoute }
            }
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: UpdateElectricalSafetyJourney,
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
                journey.electricalSafetyDetailsTask,
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
class UpdateElectricalSafetyJourney(
    journeyStateService: JourneyStateService,
    journeyName: String = "electricalSafety",
    val updateCheckElectricalSafetyAnswersStep: UpdateCheckElectricalSafetyAnswersStep,
    override val completeElectricalSafetyUpdateStep: CompleteElectricalSafetyUpdateStep,
    override val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateElectricalSafetyJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateElectricalSafetyJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var previousUploadIds: List<Long> by delegateProvider.requiredImmutableDelegate("previousUploads")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val cyaStep get() = updateCheckElectricalSafetyAnswersStep

    override var isOccupied: Boolean by delegateProvider.requiredImmutableDelegate("isOccupied")
    override val allowProvideCertificateLaterRoute: Boolean = false
}

interface UpdateElectricalSafetyJourneyState :
    JourneyState,
    ElectricalSafetyDependencies,
    CheckYourAnswersJourneyState {
    val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask
    val propertyId: Long
    val lastModifiedDate: String
    val previousUploadIds: List<Long>
    val completeElectricalSafetyUpdateStep: CompleteElectricalSafetyUpdateStep
}
