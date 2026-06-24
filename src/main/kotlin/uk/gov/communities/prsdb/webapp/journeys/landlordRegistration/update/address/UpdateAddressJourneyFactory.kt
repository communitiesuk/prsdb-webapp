package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.address

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.security.Principal

@PrsdbWebService
class UpdateAddressJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateAddressJourneyState>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configureFirst { backDestination { Destination.ExternalUrl(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) } }
            configure {
                withAdditionalContentProperty { "title" to "landlordDetails.update.title" }
            }
            task(journey.addressTask) {
                initialStep()
                nextStep { journey.completeAddressUpdateStep }
            }
            step(journey.completeAddressUpdateStep) {
                parents { journey.addressTask.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateAddressJourney(
    override val addressTask: UpdateLandlordAddressTask,
    override val lookupAddressStep: LookupAddressStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val selectAddressStep: SelectAddressStep,
    override val manualAddressStep: ManualAddressStep,
    override val completeAddressUpdateStep: CompleteAddressUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "address",
) : AbstractJourneyState(journeyStateService),
    UpdateAddressJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("cachedAddresses")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("isAddressAlreadyRegistered")
    override var cachedSelectedAddress: String? by delegateProvider.nullableDelegate("cachedSelectedAddress")

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateAddressJourneyState :
    JourneyState,
    AddressState {
    val addressTask: UpdateLandlordAddressTask
    val completeAddressUpdateStep: CompleteAddressUpdateStep
}
