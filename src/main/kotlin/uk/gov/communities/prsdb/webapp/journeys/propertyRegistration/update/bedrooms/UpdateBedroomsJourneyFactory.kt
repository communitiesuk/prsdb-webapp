package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.bedrooms

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.BedroomsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.BedroomsTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateBedroomsJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateBedroomsJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnershipService.getPropertyOwnership(propertyId).getMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            task(journey.bedroomsTask) {
                backUrl { propertyDetailsRoute }
                nextStep { journey.completeBedroomsUpdateStep }
                initialStep()
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.completeBedroomsUpdateStep) {
                parents { journey.bedrooms.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
            configureStep(journey.bedrooms) {
                withAdditionalContentProperties {
                    mapOf(
                        "heading" to "forms.update.numberOfBedrooms.heading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "showWarning" to true,
                    )
                }
            }
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class UpdateBedroomsJourney(
    // BedroomsStep
    override val bedroomsTask: BedroomsTask,
    override val bedrooms: BedroomsStep,
    override val completeBedroomsUpdateStep: CompleteBedroomsUpdateStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
    journeyName: String = "bedrooms",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, delegateProvider, journeyName),
    UpdateBedroomsJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
}

interface UpdateBedroomsJourneyState :
    JourneyState,
    BedroomsState {
    val bedroomsTask: BedroomsTask
    val completeBedroomsUpdateStep: CompleteBedroomsUpdateStep
    val propertyId: Long
    val lastModifiedDate: String
}
