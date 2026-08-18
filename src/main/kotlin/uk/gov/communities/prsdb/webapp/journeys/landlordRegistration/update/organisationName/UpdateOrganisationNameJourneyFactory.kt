package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationName

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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationNameJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationNameJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            step(journey.orgNameStep) {
                initialStep()
                routeSegment(OrgNameStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.completeOrganisationNameUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "landlordDetails.update.title",
                        "fieldSetHeading" to "forms.orgName.fieldSetHeading",
                        "fieldSetHint" to "forms.orgName.fieldSetHint",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "submitButton" to "transactionSubmitButton",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completeOrganisationNameUpdateStep) {
                parents { journey.orgNameStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateOrganisationNameJourney(
    override val orgNameStep: OrgNameStep,
    override val completeOrganisationNameUpdateStep: CompleteOrganisationNameUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-name",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationNameJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateOrganisationNameJourneyState : JourneyState {
    val orgNameStep: OrgNameStep
    val completeOrganisationNameUpdateStep: CompleteOrganisationNameUpdateStep
}
