package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration

import org.springframework.beans.factory.ObjectFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.ReasonStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class NewPropertyDeregistrationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyDeregistrationJourney>,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.initializedPropertyOwnershipId = propertyOwnershipId
            state.isStateInitialized = true
        }

        if (state.initializedPropertyOwnershipId != propertyOwnershipId) {
            throw PropertyOwnershipMismatchException(
                "Journey was initialized for property ownership ${state.initializedPropertyOwnershipId} " +
                    "but request is for property ownership $propertyOwnershipId",
            )
        }

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "deregisterProperty.title" }
            }
            section {
                withHeadingMessageKey("deregisterProperty.section.heading")
                step(journey.areYouSureStep) {
                    routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                    initialStep()
                    backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                    nextStep { journey.reasonStep }
                }
                step(journey.reasonStep) {
                    routeSegment(ReasonStep.ROUTE_SEGMENT)
                    parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                    nextUrl {
                        "${
                            DeregisterPropertyController.getPropertyDeregistrationBasePath(
                                propertyOwnershipId,
                            )
                        }/$CONFIRMATION_PATH_SEGMENT"
                    }
                }
            }
        }
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)
}

@JourneyFrameworkComponent
class PropertyDeregistrationJourney(
    // Are you sure step
    override val areYouSureStep: AreYouSureStep,
    // Reason step
    override val reasonStep: ReasonStep,
    private val propertyOwnershipService: PropertyOwnershipService,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    PropertyDeregistrationJourneyState {
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var initializedPropertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")

    override val propertyOwnershipId: Long
        get() {
            if (!isStateInitialized) {
                throw IllegalStateException("PropertyDeregistrationJourney not initialized")
            }
            return initializedPropertyOwnershipId
        }

    override fun getPropertySingleLineAddress(): String =
        propertyOwnershipService
            .retrievePropertyOwnershipById(propertyOwnershipId)
            ?.address
            ?.singleLineAddress
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Address for property ownership id $propertyOwnershipId not found",
            )

    override fun getPropertyOwnership(): PropertyOwnership =
        propertyOwnershipService.retrievePropertyOwnershipById(propertyOwnershipId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property ownership $propertyOwnershipId not found",
            )

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { generateSeedForPropertyOwnership(it) },
        )
    }

    companion object {
        private fun generateSeedForPropertyOwnership(propertyOwnershipId: Long): String =
            "Property deregistration journey for property $propertyOwnershipId at time ${System.currentTimeMillis()}"
    }
}

interface PropertyDeregistrationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val reasonStep: ReasonStep
    val propertyOwnershipId: Long
    var initializedPropertyOwnershipId: Long

    fun getPropertySingleLineAddress(): String

    fun getPropertyOwnership(): PropertyOwnership
}
