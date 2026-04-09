package uk.gov.communities.prsdb.webapp.journeys.joinProperty

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.PrnSearchState
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.CheckSelectedPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.ConfirmPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyByPrnStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.JoinPropertyAlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PendingRequestStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PrnNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.RequestRejectedStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SendRequestStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.tasks.AddressSearchTask
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.tasks.PrnSearchTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.security.Principal

@PrsdbWebService
class JoinPropertyJourneyFactory(
    private val stateFactory: ObjectFactory<JoinPropertyJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.addressSearchTask.firstStep }
            configure {
                withAdditionalContentProperty { "title" to "joinProperty.title" }
            }

            section {
                withHeadingMessageKey("joinProperty.title", shouldUseNumbering = false)

                // Address search task
                task(journey.addressSearchTask) {
                    initialStep()
                    backUrl { JOIN_PROPERTY_ROUTE }
                    nextStep { journey.confirmPropertyStep }
                }

                // PRN search task - accessible when no addresses found via direct link
                task(journey.prnSearchTask) {
                    parents {
                        OrParents(
                            journey.addressSearchTask.isComplete(),
                            journey.lookupAddressStep.hasOutcome(LookupAddressMode.NO_ADDRESSES_FOUND),
                        )
                    }
                    nextStep { journey.confirmPropertyStep }
                }
            }

            // Remaining steps after search tasks
            // TODO: PDJB-281 - Connect when user has pending request
            step(journey.pendingRequestStep) {
                routeSegment(PendingRequestStep.ROUTE_SEGMENT)
                parents { journey.alreadyRegisteredStep.isComplete() }
                nextStep { journey.requestRejectedStep }
            }
            // TODO: PDJB-282 - Entry point from dashboard notification
            step(journey.requestRejectedStep) {
                routeSegment(RequestRejectedStep.ROUTE_SEGMENT)
                parents { journey.pendingRequestStep.isComplete() }
                nextStep { journey.confirmPropertyStep }
            }
            // TODO: PDJB-278 - Confirm property details page
            step(journey.confirmPropertyStep) {
                routeSegment(ConfirmPropertyStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.addressSearchTask.isComplete(),
                        journey.prnSearchTask.isComplete(),
                        journey.requestRejectedStep.isComplete(),
                    )
                }
                nextStep { journey.sendRequestStep }
            }
            // TODO: PDJB-284 - Send request declaration page with responsibilities
            step(journey.sendRequestStep) {
                routeSegment(SendRequestStep.ROUTE_SEGMENT)
                parents { journey.confirmPropertyStep.isComplete() }
                nextUrl { JOIN_PROPERTY_CONFIRMATION_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal) = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent
class JoinPropertyJourney(
    // Address search task
    override val addressSearchTask: AddressSearchTask,
    override val lookupAddressStep: LookupAddressStep,
    override val selectPropertyStep: SelectPropertyStep,
    override val checkSelectedPropertyStep: CheckSelectedPropertyStep,
    override val noMatchingPropertiesStep: NoMatchingPropertiesStep,
    override val propertyNotRegisteredStep: PropertyNotRegisteredStep,
    override val alreadyRegisteredStep: JoinPropertyAlreadyRegisteredStep,
    // PRN search task
    override val prnSearchTask: PrnSearchTask,
    override val findPropertyByPrnStep: FindPropertyByPrnStep,
    override val prnNotFoundStep: PrnNotFoundStep,
    // Remaining steps
    override val pendingRequestStep: PendingRequestStep,
    override val requestRejectedStep: RequestRejectedStep,
    override val confirmPropertyStep: ConfirmPropertyStep,
    override val sendRequestStep: SendRequestStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    JoinPropertyJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("cachedAddresses")

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(user) })
    }

    companion object {
        private fun generateSeedForUser(user: Principal): String =
            "Join property journey for ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface JoinPropertyJourneyState :
    JoinPropertyAddressSearchState,
    PrnSearchState {
    val addressSearchTask: AddressSearchTask
    val prnSearchTask: PrnSearchTask
    val pendingRequestStep: PendingRequestStep
    val requestRejectedStep: RequestRejectedStep
    val confirmPropertyStep: ConfirmPropertyStep
    val sendRequestStep: SendRequestStep
}
