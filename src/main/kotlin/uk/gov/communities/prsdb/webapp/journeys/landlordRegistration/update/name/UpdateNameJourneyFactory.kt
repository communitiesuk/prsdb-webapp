package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.name

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.IdentityVerificationStatus
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckLandlordIdentityVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import java.security.Principal

@PrsdbWebService
class UpdateNameJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateNameJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            step(journey.checkLandlordIdentityVerifiedStep) {
                initialStep()
                nextStep { journey.nameStep }
            }
            step(journey.nameStep) {
                routeSegment(NameStep.ROUTE_SEGMENT)
                parents { journey.checkLandlordIdentityVerifiedStep.hasOutcome(IdentityVerificationStatus.NOT_VERIFIED) }
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.completeNameUpdateStep }
                withAdditionalContentProperties {
                    mapOf(
                        "title" to "landlordDetails.update.title",
                        "fieldSetHeading" to "forms.update.name.fieldSetHeading",
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "showWarning" to true,
                    )
                }
            }
            step(journey.completeNameUpdateStep) {
                parents { journey.nameStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateNameJourney(
    override val checkLandlordIdentityVerifiedStep: CheckLandlordIdentityVerifiedStep,
    override val nameStep: NameStep,
    override val completeNameUpdateStep: CompleteNameUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "name",
) : AbstractJourneyState(journeyStateService),
    UpdateNameJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateNameJourneyState : JourneyState {
    val checkLandlordIdentityVerifiedStep: CheckLandlordIdentityVerifiedStep
    val nameStep: NameStep
    val completeNameUpdateStep: CompleteNameUpdateStep
}
