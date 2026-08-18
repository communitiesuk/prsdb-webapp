package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationPhoneNumber

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationPhoneNumberJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationPhoneNumberJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            step(journey.orgPhoneNumberStep) {
                initialStep()
                routeSegment(OrgPhoneNumberStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.completeOrganisationPhoneNumberUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "landlordDetails.update.title",
                        "fieldSetHeading" to "registerAsALandlord.orgPhoneNumber.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "submitButton" to "transactionSubmitButton",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completeOrganisationPhoneNumberUpdateStep) {
                parents { journey.orgPhoneNumberStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateOrganisationPhoneNumberJourney(
    override val orgPhoneNumberStep: OrgPhoneNumberStep,
    override val completeOrganisationPhoneNumberUpdateStep: CompleteOrganisationPhoneNumberUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-phone-number",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationPhoneNumberJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateOrganisationPhoneNumberJourneyState : JourneyState {
    val orgPhoneNumberStep: OrgPhoneNumberStep
    val completeOrganisationPhoneNumberUpdateStep: CompleteOrganisationPhoneNumberUpdateStep
}
