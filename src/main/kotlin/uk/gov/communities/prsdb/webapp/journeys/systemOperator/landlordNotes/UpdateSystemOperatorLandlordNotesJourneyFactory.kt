package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

import org.springframework.beans.factory.ObjectFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.time.Instant

@PrsdbWebService
class UpdateSystemOperatorLandlordNotesJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateSystemOperatorLandlordNotesJourney>,
    private val landlordService: LandlordService,
) {
    final fun createJourneySteps(landlordId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            val landlord =
                landlordService.retrieveLandlordById(landlordId)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Landlord $landlordId not found")
            state.landlordId = landlordId
            state.lastModifiedDate = landlord.getMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.landlordId != landlordId) {
            throw PrsdbWebException("Journey state landlordId ${state.landlordId} does not match provided landlordId $landlordId")
        }

        val landlordRecordRoute = LandlordDetailsController.getLandlordDetailsForSystemOperatorPath(landlordId)

        return journey(state) {
            unreachableStepUrl { landlordRecordRoute }
            step(journey.notesStep) {
                routeSegment(SystemOperatorLandlordNotesStep.ROUTE_SEGMENT)
                backUrl { landlordRecordRoute }
                nextStep { journey.completeNotesUpdateStep }
                initialStep()
            }
            step(journey.completeNotesUpdateStep) {
                parents { journey.notesStep.hasOutcome(Complete.COMPLETE) }
                nextUrl { landlordRecordRoute }
            }
        }
    }

    fun initializeJourneyState(
        seed: Any?,
        currentLastModifiedDate: Instant,
    ): String = stateFactory.getObject().initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
}

@JourneyFrameworkComponent
class UpdateSystemOperatorLandlordNotesJourney(
    override val notesStep: SystemOperatorLandlordNotesStep,
    override val completeNotesUpdateStep: CompleteSystemOperatorLandlordNotesUpdateStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "system operator landlord notes",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateSystemOperatorLandlordNotesJourneyState {
    override var landlordId: Long by delegateProvider.requiredImmutableDelegate("landlordId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate(LAST_MODIFIED_DATE_KEY)
    override var notesInitialised: Boolean? by delegateProvider.nullableDelegate("notesInitialised")
}

interface UpdateSystemOperatorLandlordNotesJourneyState : JourneyState {
    val notesStep: SystemOperatorLandlordNotesStep
    val completeNotesUpdateStep: CompleteSystemOperatorLandlordNotesUpdateStep
    val landlordId: Long
    val lastModifiedDate: String
    var notesInitialised: Boolean?
}
