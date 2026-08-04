package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationLandlordController.Companion.ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordDeregistration.stepConfig.DeregisterStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@PrsdbWebService
class OrganisationLandlordDeregistrationJourneyFactory(
    private val stateFactory: ObjectFactory<OrganisationLandlordDeregistrationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "deregisterOrganisationLandlord.title" }
            }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextDestination { Destination(journey.deregisterStep) }
            }
            step(journey.deregisterStep) {
                parents { journey.areYouSureStep.hasOutcome(Complete.COMPLETE) }
                nextUrl { "$ORGANISATION_LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class OrganisationLandlordDeregistrationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val deregisterStep: DeregisterStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    OrganisationLandlordDeregistrationJourneyState {
    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Organisation landlord deregistration journey at time ${System.currentTimeMillis()}",
        )
}

interface OrganisationLandlordDeregistrationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val deregisterStep: DeregisterStep
}
