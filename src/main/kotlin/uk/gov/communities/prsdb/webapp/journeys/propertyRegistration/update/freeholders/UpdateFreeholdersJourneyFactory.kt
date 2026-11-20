package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import org.springframework.beans.factory.ObjectFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PEOPLE_RESPONSIBLE_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersListState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.useContinueButtons
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.Instant

@PrsdbWebService
class UpdateFreeholdersJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateFreeholdersJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)

        if (!propertyOwnership.ownershipType.requiresFreeholderRecords()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property ownership $propertyId does not require freeholder records",
            )
        }

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnership.getOwnershipStructureMostRecentlyUpdated().toString()
            state.isJointFreeholder = propertyOwnership.ownershipType.isJointFreeholder()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val peopleResponsibleRoute = "${PropertyDetailsController.getPropertyDetailsPath(propertyId)}#$PEOPLE_RESPONSIBLE_FRAGMENT"

        return journey(state) {
            unreachableStepUrl { peopleResponsibleRoute }
            step(journey.initialiseFreeholdersUpdateStep) {
                initialStep()
                routeSegment(InitialiseFreeholdersUpdateStep.ROUTE_SEGMENT)
                nextStep { journey.freeholdersDetailsTask.firstStep }
            }
            task(journey.freeholdersDetailsTask) {
                parents { journey.initialiseFreeholdersUpdateStep.isComplete() }
                withDependencies {
                    FreeholdersDetailsDependencies(
                        listState = journey,
                        isJointFreeholder = { journey.isJointFreeholder },
                        allowRemovingLastMember = false,
                    )
                }
                backUrl { peopleResponsibleRoute }
                nextStep { journey.completeFreeholdersUpdateStep }
            }
            step(journey.completeFreeholdersUpdateStep) {
                parents { journey.freeholdersDetailsTask.isComplete() }
                nextUrl { peopleResponsibleRoute }
            }
            useContinueButtons(journey.freeholdersDetailsTask)
            // There's no check your answers page, so the list page submits the update.
            configureStep(journey.freeholdersDetailsTask.freeholdersListStep) {
                withAdditionalContentProperties {
                    mapOf(
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "submitButton" to "transactionSubmitButton",
                        "insetText" to "forms.update.freeholders.notificationInset",
                        "showWarning" to true,
                    )
                }
            }
        }
    }

    fun initializeJourneyState(
        seed: Any?,
        currentLastModifiedDate: Instant,
    ): String = stateFactory.getObject().initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
}

@JourneyFrameworkComponent
class UpdateFreeholdersJourney(
    // Seeds the freeholders list from the database
    override val initialiseFreeholdersUpdateStep: InitialiseFreeholdersUpdateStep,
    // Freeholder list, details and removal steps shared with property registration
    override val freeholdersDetailsTask: FreeholdersDetailsTask,
    // Completion step for the update
    override val completeFreeholdersUpdateStep: CompleteFreeholdersUpdateStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "freeholders",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateFreeholdersJourneyState {
    override var propertyId: Long by delegateProvider.requiredDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate(LAST_MODIFIED_DATE_KEY)
    override var isJointFreeholder: Boolean by delegateProvider.requiredImmutableDelegate("isJointFreeholder")
    override var freeholdersInitialised: Boolean? by delegateProvider.nullableDelegate("freeholdersInitialised")
    override var freeholdersMap: Map<Int, FreeholderDataModel>? by delegateProvider.nullableDelegate("freeholdersMap")
    override var nextFreeholderId: Int? by delegateProvider.nullableDelegate("nextFreeholderId")
    override var editingFreeholderId: Int? by delegateProvider.nullableDelegate("editingFreeholderId")
}

interface UpdateFreeholdersJourneyState :
    JourneyState,
    FreeholdersListState {
    val initialiseFreeholdersUpdateStep: InitialiseFreeholdersUpdateStep
    val freeholdersDetailsTask: FreeholdersDetailsTask
    val completeFreeholdersUpdateStep: CompleteFreeholdersUpdateStep
    val propertyId: Long
    val lastModifiedDate: String
    val isJointFreeholder: Boolean
    var freeholdersInitialised: Boolean?
}
