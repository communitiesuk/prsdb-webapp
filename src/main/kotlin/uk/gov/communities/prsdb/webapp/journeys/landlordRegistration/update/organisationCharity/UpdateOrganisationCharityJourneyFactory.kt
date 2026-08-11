package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity

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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.UpdateDetailsTodoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCharityTask
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationCharityJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationCharityJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            task(journey.charityTask) {
                initialStep()
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.updateDetailsTodoStep }
                withAdditionalContentProperty {
                    "title" to "landlordDetails.update.title"
                }
            }
            step(journey.updateDetailsTodoStep) {
                routeSegment(UpdateDetailsTodoStep.ROUTE_SEGMENT)
                parents { journey.charityTask.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                withAdditionalContentProperty {
                    "todoComment" to "TODO PDJB-1463: Organisation charity check your answers page"
                }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateOrganisationCharityJourney(
    override val charityTask: OrgCharityTask,
    override val updateDetailsTodoStep: UpdateDetailsTodoStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-charity",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationCharityJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateOrganisationCharityJourneyState : JourneyState {
    val charityTask: OrgCharityTask
    val updateDetailsTodoStep: UpdateDetailsTodoStep
}
