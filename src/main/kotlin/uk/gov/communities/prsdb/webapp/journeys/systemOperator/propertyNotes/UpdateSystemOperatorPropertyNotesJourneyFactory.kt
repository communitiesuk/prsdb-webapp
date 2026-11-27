package uk.gov.communities.prsdb.webapp.journeys.systemOperator.propertyNotes

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesJourney
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.Instant

@PrsdbWebService
class UpdateSystemOperatorPropertyNotesJourneyFactory(
    stateFactory: ObjectFactory<UpdateSystemOperatorPropertyNotesJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractSystemOperatorNotesJourneyFactory<UpdateSystemOperatorPropertyNotesJourney>(stateFactory) {
    override fun getRecordLastModifiedDate(recordId: Long): Instant =
        propertyOwnershipService.retrievePropertyOwnershipByIdRegardlessOfActiveStatus(recordId).getMostRecentlyUpdated()

    override fun getRecordRoute(recordId: Long): String = PropertyDetailsController.getPropertyDetailsForSystemOperatorPath(recordId)
}

@JourneyFrameworkComponent
class UpdateSystemOperatorPropertyNotesJourney(
    override val notesStep: SystemOperatorPropertyNotesStep,
    override val completeNotesUpdateStep: CompleteSystemOperatorPropertyNotesUpdateStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "system operator property notes",
) : AbstractSystemOperatorNotesJourney(journeyStateService, journeyName)
