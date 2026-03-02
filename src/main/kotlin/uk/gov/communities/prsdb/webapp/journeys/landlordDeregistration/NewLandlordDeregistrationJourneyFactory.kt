package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController.Companion.LANDLORD_DEREGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.CheckHasRegisteredPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.DeregisterWithoutReasonStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.HasRegisteredPropertiesStatus
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.ReasonStep

@PrsdbWebService
class NewLandlordDeregistrationJourneyFactory(
    private val stateFactory: ObjectFactory<LandlordDeregistrationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "deregisterLandlord.title" }
            }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextDestination { mode ->
                    if (mode == AreYouSureMode.DOES_NOT_WANT_TO_PROCEED) {
                        Destination.ExternalUrl(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE)
                    } else {
                        Destination(journey.checkHasRegisteredPropertiesStep)
                    }
                }
            }
            step(journey.checkHasRegisteredPropertiesStep) {
                parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                nextDestination { status ->
                    if (status == HasRegisteredPropertiesStatus.HAS_PROPERTIES) {
                        Destination(journey.reasonStep)
                    } else {
                        Destination(journey.deregisterWithoutReasonStep)
                    }
                }
            }
            step(journey.reasonStep) {
                routeSegment(ReasonStep.ROUTE_SEGMENT)
                parents { journey.checkHasRegisteredPropertiesStep.hasOutcome(HasRegisteredPropertiesStatus.HAS_PROPERTIES) }
                nextUrl { "$LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
            step(journey.deregisterWithoutReasonStep) {
                parents { journey.checkHasRegisteredPropertiesStep.hasOutcome(HasRegisteredPropertiesStatus.NO_PROPERTIES) }
                nextUrl { "$LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class LandlordDeregistrationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val checkHasRegisteredPropertiesStep: CheckHasRegisteredPropertiesStep,
    override val reasonStep: ReasonStep,
    override val deregisterWithoutReasonStep: DeregisterWithoutReasonStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    LandlordDeregistrationJourneyState {
    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Landlord deregistration journey at time ${System.currentTimeMillis()}",
        )
}

interface LandlordDeregistrationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val checkHasRegisteredPropertiesStep: CheckHasRegisteredPropertiesStep
    val reasonStep: ReasonStep
    val deregisterWithoutReasonStep: DeregisterWithoutReasonStep
}
