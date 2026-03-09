package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.furnishedStatus

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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FurnishedStatusState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FurnishedStatusTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateFurnishedStatusJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateFurnishedStatusJourney>,
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
            task(journey.furnishedStatusTask) {
                backUrl { propertyDetailsRoute }
                nextStep { journey.completeFurnishedStatusUpdateStep }
                initialStep()
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.completeFurnishedStatusUpdateStep) {
                parents { journey.furnishedStatusTask.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
            configureStep(journey.furnishedStatus) {
                withAdditionalContentProperties {
                    mapOf(
                        "fieldSetHeading" to "forms.update.furnishedStatus.fieldSetHeading",
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
class UpdateFurnishedStatusJourney(
    override val furnishedStatusTask: FurnishedStatusTask,
    override val furnishedStatus: FurnishedStatusStep,
    override val completeFurnishedStatusUpdateStep: CompleteFurnishedStatusUpdateStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "furnished status",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateFurnishedStatusJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
}

interface UpdateFurnishedStatusJourneyState :
    JourneyState,
    FurnishedStatusState {
    val furnishedStatusTask: FurnishedStatusTask
    val completeFurnishedStatusUpdateStep: CompleteFurnishedStatusUpdateStep
    val propertyId: Long
    val lastModifiedDate: String
}
