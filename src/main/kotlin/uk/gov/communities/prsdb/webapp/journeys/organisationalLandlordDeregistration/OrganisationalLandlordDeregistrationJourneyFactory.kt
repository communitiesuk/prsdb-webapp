package uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController.Companion.ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig.DeregisterStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@PrsdbWebService
class OrganisationalLandlordDeregistrationJourneyFactory(
    private val stateFactory: ObjectFactory<OrganisationalLandlordDeregistrationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "deregisterLandlord.organisational.title" }
            }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextDestination { Destination(journey.deregisterStep) }
            }
            step(journey.deregisterStep) {
                parents { journey.areYouSureStep.hasOutcome(Complete.COMPLETE) }
                nextUrl { "$ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class OrganisationalLandlordDeregistrationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val deregisterStep: DeregisterStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    OrganisationalLandlordDeregistrationJourneyState {
    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Organisational landlord deregistration journey at time ${System.currentTimeMillis()}",
        )
}

interface OrganisationalLandlordDeregistrationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val deregisterStep: DeregisterStep
}
