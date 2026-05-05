package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyOccupiedCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateEpcJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateEpcJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
            val propertyCompliance =
                propertyOwnership.propertyCompliance
                    ?: throw PrsdbWebException("Property ownership $propertyId does not have a compliance record")

            state.propertyId = propertyId
            state.lastModifiedDate = propertyCompliance.getMostRecentlyUpdated().toString()
            state.isOccupied = propertyOwnership.isOccupied
            state.uprn = propertyOwnership.address.uprn
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val propertyComplianceRoute = PropertyDetailsController.getPropertyCompliancePath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyComplianceRoute }
            step(journey.startEpcUpdateStep) {
                routeSegment(StartEpcUpdateStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { journey.epcTask.firstStep }
            }
            task(journey.epcTask) {
                backUrl { propertyComplianceRoute }
                parents { journey.startEpcUpdateStep.isComplete() }
                nextStep { journey.completeEpcUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "sectionHeaderInfo" to null,
                    )
                }
            }
            step(journey.completeEpcUpdateStep) {
                parents { journey.epcTask.isComplete() }
                nextUrl { propertyComplianceRoute }
            }
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class UpdateEpcJourney(
    journeyStateService: JourneyStateService,
    journeyName: String = "updateEpc",
    override val epcTask: EpcTask,
    override val epcLookupByUprnStep: EpcLookupByUprnStep,
    override val hasEpcStep: HasEpcStep,
    override val checkUprnMatchedEpcStep: ConfirmEpcRetrievedByUprnStep,
    override val epcAgeCheckStep: EpcAgeCheckStep,
    override val epcEnergyRatingCheckStep: EpcEnergyRatingCheckStep,
    override val isPropertyOccupiedCheckStep: PropertyOccupiedCheckStep,
    override val confirmEpcDetailsRetrievedByCertificateNumberStep: ConfirmEpcDetailsRetrievedByCertificateNumberStep,
    override val findYourEpcStep: FindYourEpcStep,
    override val checkSupersededEpcStep: EpcSuperseededStep,
    override val epcNotFoundStep: EpcNotFoundStep,
    override val epcInDateAtStartOfTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep,
    override val hasMeesExemptionStep: HasMeesExemptionStep,
    override val meesExemptionStep: MeesExemptionStep,
    override val lowEnergyRatingStep: LowEnergyRatingStep,
    override val epcExpiredStep: EpcExpiredStep,
    override val isEpcRequiredStep: IsEpcRequiredStep,
    override val epcExemptionStep: EpcExemptionStep,
    override val epcMissingStep: EpcMissingStep,
    override val provideEpcLaterStep: ProvideEpcLaterStep,
    override val checkEpcAnswersStep: CheckEpcAnswersStep,
    override val completeEpcUpdateStep: CompleteEpcUpdateStep,
    override val startEpcUpdateStep: StartEpcUpdateStep,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateEpcJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var isOccupied: Boolean by delegateProvider.requiredImmutableDelegate("isOccupied")
    override var uprn: Long? by delegateProvider.nullableDelegate("uprn")

    override var epcRetrievedByUprn: EpcDataModel? by delegateProvider.nullableDelegate("epcRetrievedByUprn")
    override var epcRetrievedByCertificateNumber: EpcDataModel? by delegateProvider.nullableDelegate("epcRetrievedByCertificateNumber")
    override var epcRetrievedByCertificateNumberUpdatedSinceUserReview: Boolean?
        by delegateProvider.nullableDelegate("epcRetrievedByCertificateNumberUpdatedSinceUserReview")
    override var updatedEpcRetrievedByCertificateNumber: EpcDataModel? by delegateProvider
        .nullableDelegate("updatedEpcRetrievedByCertificateNumber")
    override var acceptedEpc: EpcDataModel? by delegateProvider.nullableDelegate("acceptedEpc")

    override val allowProvideCertificateLaterRoute: Boolean = false
}

interface UpdateEpcJourneyState : JourneyState, EpcState {
    val propertyId: Long
    val lastModifiedDate: String
    val epcTask: EpcTask
    val completeEpcUpdateStep: CompleteEpcUpdateStep
    val startEpcUpdateStep: StartEpcUpdateStep
}
