package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.electricalSafety

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

@JourneyFrameworkComponent
class CompleteElectricalSafetyUpdateStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
) : AbstractInternalStepConfig<Complete, UpdateElectricalSafetyJourneyState>() {
    override fun mode(state: UpdateElectricalSafetyJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateElectricalSafetyJourneyState) {
        propertyComplianceService.updateElectricalSafety(
            propertyOwnershipId = state.propertyId,
            initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            electricalSafetyExpiryDate = state.getElectricalCertificateExpiryDateIfReachable()?.toJavaLocalDate(),
            electricalSafetyCertUploadIds = state.electricalUploadIds,
        )
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
