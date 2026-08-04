package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety

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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateGasSafetyJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateGasSafetyJourney>,
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
            state.previousUploadIds = propertyCompliance.gasSafetyFileUploads.map { it.id }
            state.isOccupied = propertyOwnership.isOccupied
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
        state: UpdateGasSafetyJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyComplianceRoute = PropertyDetailsController.getPropertyCompliancePath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyComplianceRoute }
            task(journey.gasSafetyDetailsTask) {
                withDependencies { journey }
                initialStep()
                backUrl { propertyComplianceRoute }
                nextStep { journey.updateCheckGasSafetyAnswersStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "sectionHeaderInfo" to null,
                    )
                }
            }
            step(journey.updateCheckGasSafetyAnswersStep) {
                routeSegment(UpdateCheckGasSafetyAnswersStep.ROUTE_SEGMENT)
                parents { journey.gasSafetyDetailsTask.isComplete() }
                nextStep { journey.completeGasSafetyUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                    )
                }
            }
            step(journey.completeGasSafetyUpdateStep) {
                parents { journey.updateCheckGasSafetyAnswersStep.isComplete() }
                nextUrl { propertyComplianceRoute }
            }
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: UpdateGasSafetyJourney,
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
                journey.gasSafetyDetailsTask,
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
class UpdateGasSafetyJourney(
    journeyStateService: JourneyStateService,
    journeyName: String = "gasSafety",
    override val gasSafetyDetailsTask: GasSafetyDetailsTask,
    val updateCheckGasSafetyAnswersStep: UpdateCheckGasSafetyAnswersStep,
    override val completeGasSafetyUpdateStep: CompleteGasSafetyUpdateStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateGasSafetyJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateGasSafetyJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var previousUploadIds: List<Long> by delegateProvider.requiredImmutableDelegate("previousUploads")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val cyaStep get() = updateCheckGasSafetyAnswersStep

    override var isOccupied: Boolean by delegateProvider.requiredImmutableDelegate("isOccupied")
    override val allowProvideCertificateLaterRoute: Boolean = false
}

interface UpdateGasSafetyJourneyState :
    JourneyState,
    GasSafetyDependencies,
    CheckYourAnswersJourneyState {
    val gasSafetyDetailsTask: GasSafetyDetailsTask
    val propertyId: Long
    val lastModifiedDate: String
    val previousUploadIds: List<Long>
    val completeGasSafetyUpdateStep: CompleteGasSafetyUpdateStep
}
