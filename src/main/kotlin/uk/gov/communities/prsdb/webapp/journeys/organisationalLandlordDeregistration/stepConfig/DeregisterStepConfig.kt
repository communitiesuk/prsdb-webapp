package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.OrganisationalLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent("organisationalLandlordDeregistrationDeregisterStepConfig")
class DeregisterStepConfig : AbstractInternalStepConfig<Complete, OrganisationalLandlordDeregistrationJourneyState>() {
    override fun mode(state: OrganisationalLandlordDeregistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: OrganisationalLandlordDeregistrationJourneyState) {
        // TODO: PDJB-1483 - Delete org landlord logic
        // TODO: PDJB-1485 - Send org landlord deregistration confirmation emails to all team members
    }
}

@JourneyFrameworkComponent("organisationalLandlordDeregistrationDeregisterStep")
class DeregisterStep(
    stepConfig: DeregisterStepConfig,
) : JourneyStep.InternalStep<Complete, OrganisationalLandlordDeregistrationJourneyState>(stepConfig)
