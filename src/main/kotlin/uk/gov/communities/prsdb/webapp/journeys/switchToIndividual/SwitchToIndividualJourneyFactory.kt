package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig.CompleteSwitchToIndividualStep
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig.ConfirmOnlyLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig.SwitchToIndividualCheckPendingInvitationsStep

@PrsdbWebService
class SwitchToIndividualJourneyFactory(
    private val stateFactory: ObjectFactory<SwitchToIndividualJourney>,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.hasPendingInvitationsStep }
            configure {
                withAdditionalContentProperty { "title" to "switchToIndividual.title" }
            }
            step(journey.hasPendingInvitationsStep) {
                initialStep()
                nextStep { mode ->
                    when (mode) {
                        HasPendingInvitationsMode.YES -> journey.checkPendingInvitationsStep
                        HasPendingInvitationsMode.NO -> journey.confirmOnlyLandlordStep
                    }
                }
            }
            step(journey.checkPendingInvitationsStep) {
                routeSegment(SwitchToIndividualCheckPendingInvitationsStep.ROUTE_SEGMENT)
                parents { journey.hasPendingInvitationsStep.hasOutcome(HasPendingInvitationsMode.YES) }
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextStep { journey.confirmOnlyLandlordStep }
            }
            step(journey.confirmOnlyLandlordStep) {
                routeSegment(ConfirmOnlyLandlordStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasPendingInvitationsStep.hasOutcome(HasPendingInvitationsMode.NO),
                        journey.checkPendingInvitationsStep.isComplete(),
                    )
                }
                nextStep { journey.completeSwitchToIndividualStep }
            }
            step(journey.completeSwitchToIndividualStep) {
                parents { journey.confirmOnlyLandlordStep.isComplete() }
                nextUrl {
                    "${SwitchToIndividualController.getSwitchToIndividualBasePath(propertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)

    private fun getInitializedState(propertyOwnershipId: Long): SwitchToIndividualJourney =
        stateFactory.getObject().initialiseFromPropertyOwnershipId(propertyOwnershipId)
}

@JourneyFrameworkComponent
class SwitchToIndividualJourney(
    val hasPendingInvitationsStep: HasPendingInvitationsStep,
    val checkPendingInvitationsStep: SwitchToIndividualCheckPendingInvitationsStep,
    val confirmOnlyLandlordStep: ConfirmOnlyLandlordStep,
    val completeSwitchToIndividualStep: CompleteSwitchToIndividualStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    SwitchToIndividualJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { "Switch to individual journey for property $it at time ${System.currentTimeMillis()}" },
        )
    }
}

interface SwitchToIndividualJourneyState : PropertyOwnershipJourneyState
