package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.toJavaLocalDate
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService

@JourneyFrameworkComponent
class SaveComplianceDataStepConfig(
    private val propertyComplianceService: PropertyComplianceService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractInternalStepConfig<Complete, PropertyRegistrationJourneyState>() {
    override fun mode(state: PropertyRegistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: PropertyRegistrationJourneyState) {
        val registrationNumberValue = state.registrationNumberValue ?: return

        propertyComplianceService.saveRegistrationComplianceData(
            registrationNumberValue = registrationNumberValue,
            gasSafetyCertIssueDate = state.getGasSafetyCertificateIssueDateIfReachable()?.toJavaLocalDate(),
            eicrExpiryDate = state.getElectricalCertificateExpiryDateIfReachable()?.toJavaLocalDate(),
            epcCertificateUrl =
                state.acceptedEpc?.let {
                    epcCertificateUrlProvider.getEpcCertificateUrl(it.certificateNumber)
                },
            epcExpiryDate = state.acceptedEpc?.expiryDateAsJavaLocalDate,
            epcEnergyRating = state.acceptedEpc?.energyRating,
            tenancyStartedBeforeEpcExpiry =
                state.epcInDateAtStartOfTenancyCheckStep
                    .formModelIfReachableOrNull?.tenancyStartedBeforeExpiry,
            epcExemptionReason =
                state.epcExemptionStep
                    .formModelIfReachableOrNull?.exemptionReason,
            epcMeesExemptionReason =
                state.meesExemptionStep
                    .formModelIfReachableOrNull?.exemptionReason,
        )
    }

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class SaveComplianceDataStep(
    stepConfig: SaveComplianceDataStepConfig,
) : JourneyStep.InternalStep<Complete, PropertyRegistrationJourneyState>(stepConfig)
