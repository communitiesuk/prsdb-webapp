package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesStepConfig
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class SystemOperatorLandlordNotesStepConfig(
    private val landlordService: LandlordService,
) : AbstractSystemOperatorNotesStepConfig() {
    override val fieldSetHeadingKey = "systemOperator.notes.landlord.fieldSetHeading"

    override fun getExistingNotes(recordId: Long): String? = landlordService.retrieveLandlordById(recordId)?.systemOperatorNotes
}

@JourneyFrameworkComponent
class SystemOperatorLandlordNotesStep(
    stepConfig: SystemOperatorLandlordNotesStepConfig,
) : AbstractSystemOperatorNotesStep(stepConfig)
