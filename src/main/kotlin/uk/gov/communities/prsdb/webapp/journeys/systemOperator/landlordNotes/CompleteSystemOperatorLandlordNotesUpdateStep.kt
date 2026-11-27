package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractCompleteSystemOperatorNotesUpdateStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractCompleteSystemOperatorNotesUpdateStepConfig
import uk.gov.communities.prsdb.webapp.services.LandlordService
import java.time.Instant

@JourneyFrameworkComponent
class CompleteSystemOperatorLandlordNotesUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractCompleteSystemOperatorNotesUpdateStepConfig() {
    override fun updateNotes(
        recordId: Long,
        notes: String,
        initialLastModifiedDate: Instant,
    ) = landlordService.updateSystemOperatorNotes(
        landlordId = recordId,
        notes = notes,
        initialLastModifiedDate = initialLastModifiedDate,
    )
}

@JourneyFrameworkComponent
class CompleteSystemOperatorLandlordNotesUpdateStep(
    stepConfig: CompleteSystemOperatorLandlordNotesUpdateStepConfig,
) : AbstractCompleteSystemOperatorNotesUpdateStep(stepConfig)
