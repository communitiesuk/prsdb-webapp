package uk.gov.communities.prsdb.webapp.journeys.systemOperator.propertyNotes

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractCompleteSystemOperatorNotesUpdateStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractCompleteSystemOperatorNotesUpdateStepConfig
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.Instant

@JourneyFrameworkComponent
class CompleteSystemOperatorPropertyNotesUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractCompleteSystemOperatorNotesUpdateStepConfig() {
    override fun updateNotes(
        recordId: Long,
        notes: String,
        initialLastModifiedDate: Instant,
    ) = propertyOwnershipService.updateSystemOperatorNotes(
        id = recordId,
        notes = notes,
        initialLastModifiedDate = initialLastModifiedDate,
    )
}

@JourneyFrameworkComponent
class CompleteSystemOperatorPropertyNotesUpdateStep(
    stepConfig: CompleteSystemOperatorPropertyNotesUpdateStepConfig,
) : AbstractCompleteSystemOperatorNotesUpdateStep(stepConfig)
