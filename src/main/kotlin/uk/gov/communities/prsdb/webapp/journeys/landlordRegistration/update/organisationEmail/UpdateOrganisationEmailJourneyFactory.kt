package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationEmail

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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationEmailJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationEmailJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            step(journey.orgEmailStep) {
                initialStep()
                routeSegment(OrgEmailStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.completeOrganisationEmailUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "landlordDetails.update.title",
                        "fieldSetHeading" to "forms.orgEmail.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "submitButton" to "transactionSubmitButton",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completeOrganisationEmailUpdateStep) {
                parents { journey.orgEmailStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateOrganisationEmailJourney(
    override val orgEmailStep: OrgEmailStep,
    override val completeOrganisationEmailUpdateStep: CompleteOrganisationEmailUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-email",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationEmailJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateOrganisationEmailJourneyState : JourneyState {
    val orgEmailStep: OrgEmailStep
    val completeOrganisationEmailUpdateStep: CompleteOrganisationEmailUpdateStep
}
