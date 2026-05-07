package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.gasSafety

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

@JourneyFrameworkComponent
class CompleteGasSafetyUpdateStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
) : AbstractInternalStepConfig<Complete, UpdateGasSafetyJourneyState>() {
    override fun mode(state: UpdateGasSafetyJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateGasSafetyJourneyState) {
        propertyComplianceService.updateGasSafety(
            propertyOwnershipId = state.propertyId,
            initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            hasGasSupply =
                state.hasGasSupplyStep.formModel.hasGasSupply
                    ?: throw NotNullFormModelValueIsNullException("hasGasSupply is null"),
            gasSafetyCertIssueDate = state.getGasSafetyCertificateIssueDateIfReachable()?.toJavaLocalDate(),
            gasSafetyCertUploadIds = state.gasUploadIds,
        )
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
