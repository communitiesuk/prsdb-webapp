package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.LANDLORD_PROPERTY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable
import java.security.Principal

@PrsdbWebService
class UpdateOwnershipTypeJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOwnershipJourney>,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        return journey(state) {
            unreachableStepUrl { "/" }
            step(journey.ownershipTypeStep) {
                routeSegment("ownership-type")
                initialStep()
                nextUrl { LANDLORD_PROPERTY_DETAILS_ROUTE }
                checkable()
            }
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class UpdateOwnershipJourney(
    // OwnershipTypeStep
    override val ownershipTypeStep: UpdateOwnershipTypeStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    UpdateOwnershipTypeJourneyState {
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")

    override fun generateJourneyId(seed: Any?): String {
        val ownershipUserPair: Pair<Long, Principal>? = convertSeedToOwnershipUserPairOrNull(seed)

        return super<AbstractJourneyState>.generateJourneyId(
            ownershipUserPair?.let {
                generateSeedForPropertyOwnershipAndUser(it.first, it.second)
            },
        )
    }

    private fun convertSeedToOwnershipUserPairOrNull(seed: Any?): Pair<Long, Principal>? =
        (seed as? Pair<*, *>)?.let {
            (it.first as? Long)?.let { ownershipId ->
                (it.second as? Principal)?.let { user ->
                    Pair(ownershipId, user)
                }
            }
        }

    companion object {
        fun generateSeedForPropertyOwnershipAndUser(
            ownershipId: Long,
            user: Principal,
        ): String = "Update ownership type for property $ownershipId by user ${user.name}"
    }
}

interface UpdateOwnershipTypeJourneyState {
    val ownershipTypeStep: UpdateOwnershipTypeStep
    val propertyId: Long
}
