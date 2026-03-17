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
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.DeregisterStep
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.ReasonStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class LandlordDeregistrationJourneyFactory(
    private val stateFactory: ObjectFactory<LandlordDeregistrationJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    fun createJourneySteps(baseUserId: String): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.userHasRegisteredProperties = propertyOwnershipService.doesLandlordHaveRegisteredProperties(baseUserId)
            state.isStateInitialized = true
        }

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
                    } else if (state.userHasRegisteredProperties) {
                        Destination(journey.reasonStep)
                    } else {
                        Destination(journey.deregisterStep)
                    }
                }
            }
            step(journey.reasonStep) {
                routeSegment(ReasonStep.ROUTE_SEGMENT)
                parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                nextDestination { Destination(journey.deregisterStep) }
            }
            step(journey.deregisterStep) {
                parents {
                    OrParents(
                        journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED),
                        journey.reasonStep.hasOutcome(Complete.COMPLETE),
                    )
                }
                nextUrl { "$LANDLORD_DEREGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class LandlordDeregistrationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val reasonStep: ReasonStep,
    override val deregisterStep: DeregisterStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    LandlordDeregistrationJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var userHasRegisteredProperties: Boolean by delegateProvider.requiredDelegate("userHasRegisteredProperties")

    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Landlord deregistration journey at time ${System.currentTimeMillis()}",
        )
}

interface LandlordDeregistrationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val reasonStep: ReasonStep
    val deregisterStep: DeregisterStep
    var userHasRegisteredProperties: Boolean
}
