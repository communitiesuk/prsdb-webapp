package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

@JourneyFrameworkComponent
class CompleteEpcUpdateStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractInternalStepConfig<Complete, UpdateEpcJourneyState>() {
    override fun mode(state: UpdateEpcJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateEpcJourneyState) {
        val acceptedEpc = state.acceptedEpcIfReachable

        try {
            propertyComplianceService.updateEpc(
                propertyOwnershipId = state.propertyId,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
                epcCertificateUrl = acceptedEpc?.let { epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber) },
                epcExpiryDate = acceptedEpc?.expiryDate?.toJavaLocalDate(),
                epcEnergyRating = acceptedEpc?.energyRating,
                tenancyStartedBeforeEpcExpiry =
                    state.epcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull?.tenancyStartedBeforeExpiry,
                epcExemptionReason = state.epcExemptionStep.formModelIfReachableOrNull?.exemptionReason,
                epcMeesExemptionReason = state.meesExemptionStep.formModelIfReachableOrNull?.exemptionReason,
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
