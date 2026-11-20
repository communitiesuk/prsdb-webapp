package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc

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

@JourneyFrameworkComponent
class CompleteEpcUpdateStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
    private val complianceDetailsDataModelFactory: ComplianceDetailsDataModelFactory,
) : AbstractInternalStepConfig<Complete, UpdateEpcJourneyState>() {
    override fun mode(state: UpdateEpcJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateEpcJourneyState) {
        val epc = complianceDetailsDataModelFactory.fromEpcDetailState(state.epcDetailsTask)

        try {
            propertyComplianceService.updateEpc(
                propertyOwnershipId = state.propertyId,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
                epcCertificateUrl = epc.certificateUrl,
                epcExpiryDate = epc.expiryDate,
                epcEnergyRating = epc.energyRating,
                tenancyStartedBeforeEpcExpiry = epc.tenancyStartedBeforeExpiry,
                epcExemptionReason = epc.exemptionReason,
                epcMeesExemptionReason = epc.meesExemptionReason,
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
    }

    override fun resolveNextDestination(
        state: UpdateEpcJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteEpcUpdateStep(
    stepConfig: CompleteEpcUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateEpcJourneyState>(stepConfig)
