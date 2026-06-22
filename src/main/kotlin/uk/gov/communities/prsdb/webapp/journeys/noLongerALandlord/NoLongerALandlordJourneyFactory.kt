package uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.NoLongerALandlordController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.noLongerALandlord.stepConfig.ConfirmStep

@PrsdbWebService
class NoLongerALandlordJourneyFactory(
    private val stateFactory: ObjectFactory<NoLongerALandlordJourney>,
) {
    fun createJourneySteps(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId, baseUserId)

        return journey(state) {
            unreachableStepStep { journey.confirmStep }
            configure {
                withAdditionalContentProperty { "title" to "noLongerALandlord.title" }
            }
            step(journey.confirmStep) {
                routeSegment(ConfirmStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextUrl {
                    "${
                        NoLongerALandlordController.getNoLongerALandlordBasePath(
                            propertyOwnershipId,
                        )
                    }/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

    private fun getInitializedState(
        propertyOwnershipId: Long,
        baseUserId: String,
    ): NoLongerALandlordJourney {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyOwnershipId = propertyOwnershipId
            state.baseUserId = baseUserId
            state.isStateInitialized = true
        }

        if (state.propertyOwnershipId != propertyOwnershipId) {
            throw PropertyOwnershipMismatchException(
                "Journey was initialized for property ownership ${state.propertyOwnershipId} " +
                    "but request is for property ownership $propertyOwnershipId",
            )
        }

        return state
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)
}

@JourneyFrameworkComponent
class NoLongerALandlordJourney(
    override val confirmStep: ConfirmStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    NoLongerALandlordJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")
    override var baseUserId: String by delegateProvider.requiredDelegate("baseUserId")

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { generateSeedForPropertyOwnership(it) },
        )
    }

    companion object {
        private fun generateSeedForPropertyOwnership(propertyOwnershipId: Long): String =
            "No longer a landlord journey for property $propertyOwnershipId at time ${System.currentTimeMillis()}"
    }
}

interface NoLongerALandlordJourneyState : JourneyState {
    val confirmStep: ConfirmStep
    var propertyOwnershipId: Long
    var baseUserId: String
}
