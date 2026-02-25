package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.dateOfBirth

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
import java.security.Principal

@PrsdbWebService
class UpdateDateOfBirthJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateDateOfBirthJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            step(journey.dateOfBirthStep) {
                routeSegment(UpdateDateOfBirthStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.completeDateOfBirthUpdateStep }
                initialStep()
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "landlordDetails.update.title",
                        "fieldSetHeading" to "forms.update.dateOfBirth.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                    )
                }
            }
            step(journey.completeDateOfBirthUpdateStep) {
                parents { journey.dateOfBirthStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateDateOfBirthJourney(
    override val dateOfBirthStep: UpdateDateOfBirthStep,
    override val completeDateOfBirthUpdateStep: CompleteDateOfBirthUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "dateOfBirth",
) : AbstractJourneyState(journeyStateService),
    UpdateDateOfBirthJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateDateOfBirthJourneyState : JourneyState {
    val dateOfBirthStep: UpdateDateOfBirthStep
    val completeDateOfBirthUpdateStep: CompleteDateOfBirthUpdateStep
}
