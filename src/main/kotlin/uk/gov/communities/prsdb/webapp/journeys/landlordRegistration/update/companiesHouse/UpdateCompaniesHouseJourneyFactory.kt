package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import java.security.Principal

@PrsdbWebService
class UpdateCompaniesHouseJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateCompaniesHouseJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }

            step(journey.orgIsRegisteredCompanyStep) {
                initialStep()
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.interruptionStep }
            }
            step(journey.interruptionStep) {
                routeSegment(CompaniesHouseUpdateInterruptionStep.ROUTE_SEGMENT)
                parents { journey.orgIsRegisteredCompanyStep.isComplete() }
                nextDestination {
                    if (journey.orgIsRegisteredCompanyStep.outcome == YesOrNo.YES) {
                        Destination(journey.orgCompanyNumberStep)
                    } else {
                        Destination(journey.orgGovBodyTask.firstStep)
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents { journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkAnswersStep }
            }
            task(journey.orgGovBodyTask) {
                parents { journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO) }
                nextStep { journey.checkAnswersStep }
            }
            step(journey.checkAnswersStep) {
                routeSegment(CompaniesHouseUpdateCheckAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCompanyNumberStep.isComplete(),
                        journey.orgGovBodyTask.isComplete(),
                    )
                }
                nextStep { journey.completeCompaniesHouseUpdateStep }
            }
            step(journey.completeCompaniesHouseUpdateStep) {
                parents { journey.checkAnswersStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateCompaniesHouseJourney(
    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
    override val interruptionStep: CompaniesHouseUpdateInterruptionStep,
    override val orgCompanyNumberStep: OrgCompanyNumberStep,
    override val orgGovBodyTask: OrgGovBodyTask,
    override val checkAnswersStep: CompaniesHouseUpdateCheckAnswersStep,
    override val completeCompaniesHouseUpdateStep: CompleteCompaniesHouseUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "companies-house",
) : AbstractJourneyState(journeyStateService),
    UpdateCompaniesHouseJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateCompaniesHouseJourneyState : JourneyState {
    val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
    val interruptionStep: CompaniesHouseUpdateInterruptionStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
    val orgGovBodyTask: OrgGovBodyTask
    val checkAnswersStep: CompaniesHouseUpdateCheckAnswersStep
    val completeCompaniesHouseUpdateStep: CompleteCompaniesHouseUpdateStep
}
