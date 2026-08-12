package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import java.security.Principal

@PrsdbWebService
class UpdateCompaniesHouseJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateCompaniesHouseJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return if (state.checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state)
        }
    }

    private fun mainJourneyMap(state: UpdateCompaniesHouseJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }

            task(journey.updateCompaniesHouseTask) {
                initialStep()
                backDestination { Destination.ExternalUrl(LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) }
                nextStep { journey.cyaStep }
            }
            step(journey.cyaStep) {
                routeSegment(CompaniesHouseUpdateCheckAnswersStep.ROUTE_SEGMENT)
                parents { journey.updateCompaniesHouseTask.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }

    private fun checkYourAnswersJourneyMap(state: UpdateCompaniesHouseJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            unreachableStepDestination { journey.returnToCyaPageDestination }

            when (state.checkingAnswersFor) {
                OrgIsRegisteredCompanyStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.updateCompaniesHouseTask)
                }

                OrgCompanyNumberStep.ROUTE_SEGMENT -> {
                    checkAnswerStep(journey.updateCompaniesHouseTask.orgCompanyNumberStep, OrgCompanyNumberStep.ROUTE_SEGMENT)
                }

                OrgGovBodyMemberListStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(
                        journey.updateCompaniesHouseTask.orgGovBodyMembersTask,
                        { OrgGovBodyMembersDependencies(listState = journey.updateCompaniesHouseTask) },
                    )
                    configureStep(journey.updateCompaniesHouseTask.orgGovBodyMembersTask.orgGovBodyMemberListStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateCompaniesHouseJourney(
    override val updateCompaniesHouseTask: UpdateCompaniesHouseTask,
    override val cyaStep: CompaniesHouseUpdateCheckAnswersStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateCompaniesHouseJourneyState>,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "companies-house",
) : AbstractJourneyState(journeyStateService),
    UpdateCompaniesHouseJourneyState {
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateCompaniesHouseJourneyState : CheckYourAnswersJourneyState {
    val updateCompaniesHouseTask: UpdateCompaniesHouseTask
    override val cyaStep: CompaniesHouseUpdateCheckAnswersStep
    override val finishCyaStep: FinishCyaJourneyStep
}
