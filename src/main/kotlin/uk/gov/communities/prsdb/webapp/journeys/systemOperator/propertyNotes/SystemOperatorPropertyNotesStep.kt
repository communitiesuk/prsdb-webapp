package uk.gov.communities.prsdb.webapp.journeys.systemOperator.propertyNotes

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesStep
import uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes.AbstractSystemOperatorNotesStepConfig
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class SystemOperatorPropertyNotesStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractSystemOperatorNotesStepConfig() {
    override val fieldSetHeadingKey = "systemOperator.notes.property.fieldSetHeading"

    override fun getExistingNotes(recordId: Long): String? =
        propertyOwnershipService.retrievePropertyOwnershipById(recordId)?.systemOperatorNotes
}

@JourneyFrameworkComponent
class SystemOperatorPropertyNotesStep(
    stepConfig: SystemOperatorPropertyNotesStepConfig,
) : AbstractSystemOperatorNotesStep(stepConfig)
