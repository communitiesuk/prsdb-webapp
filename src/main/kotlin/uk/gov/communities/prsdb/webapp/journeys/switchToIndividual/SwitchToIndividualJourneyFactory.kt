package uk.gov.communities.prsdb.webapp.journeys.switchToIndividual

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
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
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.HasPendingInvitationsStep
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig.CompleteSwitchToIndividualStep
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig.ConfirmOnlyLandlordStep

@PrsdbWebService
class SwitchToIndividualJourneyFactory(
    private val stateFactory: ObjectFactory<SwitchToIndividualJourney>,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        val propertyDetailsLandlordTab =
            "${PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)}#$LANDLORD_DETAILS_FRAGMENT"

        return journey(state) {
            unreachableStepUrl { propertyDetailsLandlordTab }
            configure {
                withAdditionalContentProperty { "title" to "switchToIndividual.title" }
            }
            step(journey.hasPendingInvitationsStep) {
                routeSegment(HasPendingInvitationsStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { propertyDetailsLandlordTab }
                nextStep { mode ->
                    when (mode) {
                        HasPendingInvitationsMode.YES -> journey.checkPendingInvitationsStep
                        HasPendingInvitationsMode.NO -> journey.confirmOnlyLandlordStep
                    }
                }
            }
            step(journey.checkPendingInvitationsStep) {
                routeSegment(CheckPendingInvitationsStep.ROUTE_SEGMENT)
                withAdditionalContentProperty { "messagePrefix" to "switchToIndividual" }
                parents { journey.hasPendingInvitationsStep.hasOutcome(HasPendingInvitationsMode.YES) }
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
    val checkPendingInvitationsStep: CheckPendingInvitationsStep,
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
