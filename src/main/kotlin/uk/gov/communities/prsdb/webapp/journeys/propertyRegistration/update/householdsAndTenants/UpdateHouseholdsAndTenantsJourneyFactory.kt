package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateHouseholdsAndTenantsJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateHouseholdsAndTenantsJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnershipService.getPropertyOwnership(propertyId).getMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            task(journey.householdsAndTenantsTask) {
                initialStep()
                nextStep { journey.cyaStep }
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateHouseholdsAndTenantsCyaStep.ROUTE_SEGMENT)
                parents { journey.householdsAndTenantsTask.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
            configureStep(journey.households) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.numberOfHouseholds.fieldSetHeading"
                }
            }
            configureStep(journey.tenants) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.numberOfPeople.fieldSetHeading"
                }
            }
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class UpdateHouseholdsAndTenantsJourney(
    // HouseholdsAndTenants task
    override val householdsAndTenantsTask: HouseholdsAndTenantsTask,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    // Check your answers step
    override val cyaStep: UpdateHouseholdsAndTenantsCyaStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "households and tenants",
    override val finishCyaStep: FinishCyaJourneyStep,
    private val objectFactory: ObjectFactory<UpdateHouseholdsAndTenantsJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateHouseholdsAndTenantsJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")

    override var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override fun getBaseJourneyState(): CheckYourAnswersJourneyState {
        val baseJourneyId = journeyId
        objectFactory.getObject().also { baseState ->
            baseState.setJourneyId(baseJourneyId)
            return baseState
        }
    }

    override fun createChildJourneyState(childJourneyId: String): CheckYourAnswersJourneyState {
        copyJourneyTo(childJourneyId)
        return objectFactory.getObject().apply { setJourneyId(childJourneyId) }
    }
}

interface UpdateHouseholdsAndTenantsJourneyState :
    HouseholdsAndTenantsState,
    CheckYourAnswersJourneyState {
    val householdsAndTenantsTask: HouseholdsAndTenantsTask
    override val cyaStep: UpdateHouseholdsAndTenantsCyaStep
    val propertyId: Long
    val lastModifiedDate: String
}
