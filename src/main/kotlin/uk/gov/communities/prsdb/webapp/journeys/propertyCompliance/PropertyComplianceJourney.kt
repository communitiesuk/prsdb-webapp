package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyCertificateUploadStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.PropertyComplianceTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.tasks.GasSafetyTask

@JourneyFrameworkComponent
class PropertyComplianceJourney(
    override val taskListStep: PropertyComplianceTaskListStep,
    // Gas safety task
    override val gasSafetyTask: GasSafetyTask,
    override val gasSafetyCertificateUploadStep: GasSafetyCertificateUploadStep,
    override val gasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    PropertyComplianceJourneyState
