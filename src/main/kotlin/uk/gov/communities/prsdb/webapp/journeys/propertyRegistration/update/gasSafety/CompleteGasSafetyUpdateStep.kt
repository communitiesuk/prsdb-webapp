package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety

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
class CompleteGasSafetyUpdateStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
    private val complianceDetailsDataModelFactory: ComplianceDetailsDataModelFactory,
    private val uploadService: UploadService,
) : AbstractInternalStepConfig<Complete, UpdateGasSafetyJourneyState>() {
    override fun mode(state: UpdateGasSafetyJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateGasSafetyJourneyState) {
        val gasSafety = complianceDetailsDataModelFactory.fromGasSafetyDetailState(state.gasSafetyDetailsTask)

        try {
            propertyComplianceService.updateGasSafety(
                propertyOwnershipId = state.propertyId,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
                hasGasSupply = gasSafety.hasGasSupply,
                gasSafetyCertIssueDate = gasSafety.issueDate,
                gasSafetyCertUploadIds = gasSafety.uploadIds,
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
        state: UpdateGasSafetyJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteGasSafetyUpdateStep(
    stepConfig: CompleteGasSafetyUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateGasSafetyJourneyState>(stepConfig)
