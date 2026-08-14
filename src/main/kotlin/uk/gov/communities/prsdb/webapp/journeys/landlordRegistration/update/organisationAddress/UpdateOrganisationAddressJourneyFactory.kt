package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationAddress

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.OrgAddressTask
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationAddressJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationAddressJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configure {
                backDestination { Destination.ExternalUrl(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) }
                withAdditionalContentProperty { "title" to "landlordDetails.update.title" }
            }
            task(journey.addressTask) {
                initialStep()
                nextStep { journey.completeOrganisationAddressUpdateStep }
                configureStep(journey.addressTask.selectAddressStep) {
                    withAdditionalContentProperties {
                        mapOf(
                            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                            "showWarning" to true,
                        )
                    }
                }
                configureStep(journey.addressTask.manualAddressStep) {
                    withAdditionalContentProperties {
                        mapOf(
                            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                            "showWarning" to true,
                        )
                    }
                }
            }
            step(journey.completeOrganisationAddressUpdateStep) {
                parents { journey.addressTask.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateOrganisationAddressJourney(
    override val addressTask: OrgAddressTask,
    override val completeOrganisationAddressUpdateStep: CompleteOrganisationAddressUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-address",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationAddressJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateOrganisationAddressJourneyState : JourneyState {
    val addressTask: AddressTask
    val completeOrganisationAddressUpdateStep: CompleteOrganisationAddressUpdateStep
}
