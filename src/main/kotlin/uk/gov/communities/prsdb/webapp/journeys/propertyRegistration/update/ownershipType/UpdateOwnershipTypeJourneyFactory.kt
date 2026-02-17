package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType

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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
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

        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { "/" }
            step(journey.ownershipTypeStep) {
                routeSegment("ownership-type")
                backUrl { propertyDetailsRoute }
                nextStep { journey.completeOwnershipTypeUpdateStep }
                initialStep()
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "fieldSetHeading" to "forms.update.ownershipType.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completeOwnershipTypeUpdateStep) {
                parents { journey.ownershipTypeStep.isComplete() }
                nextUrl { propertyDetailsRoute }
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
    override val ownershipTypeStep: OwnershipTypeStep,
    override val completeOwnershipTypeUpdateStep: CompleteOwnershipTypeUpdateStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
    journeyName: String = "ownership type",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, delegateProvider, journeyName),
    UpdateOwnershipTypeJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
}

interface UpdateOwnershipTypeJourneyState : JourneyState {
    val ownershipTypeStep: OwnershipTypeStep
    val completeOwnershipTypeUpdateStep: CompleteOwnershipTypeUpdateStep
    val propertyId: Long
}
