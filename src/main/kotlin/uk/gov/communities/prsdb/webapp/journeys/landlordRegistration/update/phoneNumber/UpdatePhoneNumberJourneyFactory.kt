package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.phoneNumber

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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import java.security.Principal

@PrsdbWebService
class UpdatePhoneNumberJourneyFactory(
    private val stateFactory: ObjectFactory<UpdatePhoneNumberJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            step(journey.phoneNumberStep) {
                routeSegment(PhoneNumberStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.completePhoneNumberUpdateStep }
                initialStep()
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "landlordDetails.update.title",
                        "fieldSetHeading" to "forms.update.phoneNumber.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completePhoneNumberUpdateStep) {
                parents { journey.phoneNumberStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdatePhoneNumberJourney(
    override val phoneNumberStep: PhoneNumberStep,
    override val completePhoneNumberUpdateStep: CompletePhoneNumberUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "phone-number",
) : AbstractJourneyState(journeyStateService),
    UpdatePhoneNumberJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdatePhoneNumberJourneyState : JourneyState {
    val phoneNumberStep: PhoneNumberStep
    val completePhoneNumberUpdateStep: CompletePhoneNumberUpdateStep
}
