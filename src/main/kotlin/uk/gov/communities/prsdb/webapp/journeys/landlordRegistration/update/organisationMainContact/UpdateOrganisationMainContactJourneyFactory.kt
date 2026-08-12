package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationMainContact

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.ORGANISATION_CONTACTS_FRAGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationMainContactJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationMainContactJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            step(journey.orgMainContactStep) {
                initialStep()
                routeSegment(OrgMainContactStep.ROUTE_SEGMENT)
                backUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
                nextStep { journey.completeOrganisationMainContactUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "landlordDetails.update.title",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "submitButton" to "transactionSubmitButton",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completeOrganisationMainContactUpdateStep) {
                parents { journey.orgMainContactStep.isComplete() }
                nextUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateOrganisationMainContactJourney(
    override val orgMainContactStep: OrgMainContactStep,
    override val completeOrganisationMainContactUpdateStep: CompleteOrganisationMainContactUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-main-contact",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationMainContactJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateOrganisationMainContactJourneyState : JourneyState {
    val orgMainContactStep: OrgMainContactStep
    val completeOrganisationMainContactUpdateStep: CompleteOrganisationMainContactUpdateStep
}
