package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

import org.springframework.beans.factory.ObjectFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesJourney
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.time.Instant

@PrsdbWebService
class UpdateSystemOperatorLandlordNotesJourneyFactory(
    stateFactory: ObjectFactory<UpdateSystemOperatorLandlordNotesJourney>,
    private val landlordService: LandlordService,
) : AbstractSystemOperatorNotesJourneyFactory<UpdateSystemOperatorLandlordNotesJourney>(stateFactory) {
    override fun getRecordLastModifiedDate(recordId: Long): Instant =
        landlordService.retrieveLandlordById(recordId)?.getMostRecentlyUpdated()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Landlord $recordId not found")

    override fun getRecordRoute(recordId: Long): String = LandlordDetailsController.getLandlordDetailsForSystemOperatorPath(recordId)
}

@JourneyFrameworkComponent
class UpdateSystemOperatorLandlordNotesJourney(
    override val notesStep: SystemOperatorLandlordNotesStep,
    override val completeNotesUpdateStep: CompleteSystemOperatorLandlordNotesUpdateStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "system operator landlord notes",
) : AbstractSystemOperatorNotesJourney(journeyStateService, journeyName)
