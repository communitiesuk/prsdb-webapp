package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.ComplianceDetailsDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.UploadService

@JourneyFrameworkComponent
class CompleteElectricalSafetyUpdateStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
    private val complianceDetailsDataModelFactory: ComplianceDetailsDataModelFactory,
    private val uploadService: UploadService,
) : AbstractInternalStepConfig<Complete, UpdateElectricalSafetyJourneyState>() {
    override fun mode(state: UpdateElectricalSafetyJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateElectricalSafetyJourneyState) {
        val electricalSafety = complianceDetailsDataModelFactory.fromElectricalSafetyDetailState(state.electricalSafetyDetailsTask)

        try {
            propertyComplianceService.updateElectricalSafety(
                propertyOwnershipId = state.propertyId,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
                electricalCertType = electricalSafety.certType,
                eicrExpiryDate = electricalSafety.eicrExpiryDate,
                electricalSafetyCertUploadIds = electricalSafety.uploadIds,
                eicIssueDate = electricalSafety.eicIssueDate,
                // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
                eicExpiryDate = electricalSafety.eicExpiryDate,
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }

        state.previousUploadIds.forEach { uploadId ->
            uploadService.deleteUploadedFile(uploadId)
        }
    }

    override fun resolveNextDestination(
        state: UpdateElectricalSafetyJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteElectricalSafetyUpdateStep(
    stepConfig: CompleteElectricalSafetyUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateElectricalSafetyJourneyState>(stepConfig)
