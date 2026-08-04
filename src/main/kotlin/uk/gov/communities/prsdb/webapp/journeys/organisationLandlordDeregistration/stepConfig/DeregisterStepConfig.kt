package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.OrganisationLandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent("organisationLandlordDeregistrationDeregisterStepConfig")
class DeregisterStepConfig : AbstractInternalStepConfig<Complete, OrganisationLandlordDeregistrationJourneyState>() {
    override fun mode(state: OrganisationLandlordDeregistrationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: OrganisationLandlordDeregistrationJourneyState) {
        // TODO: PDJB-1483 - Delete org landlord logic
        // TODO: PDJB-1485 - Send org landlord deregistration confirmation emails to all team members
    }
}

@JourneyFrameworkComponent("organisationLandlordDeregistrationDeregisterStep")
class DeregisterStep(
    stepConfig: DeregisterStepConfig,
) : JourneyStep.InternalStep<Complete, OrganisationLandlordDeregistrationJourneyState>(stepConfig)
