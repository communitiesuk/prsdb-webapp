package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LeadTrusteeTask
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationTypeJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationTypeJourney>,
    private val userToLandlordService: UserToLandlordService,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.previousOrgTypeMode =
                if (userToLandlordService.getCurrentOrganisationLandlordForUser().isTrust) {
                    OrgTypeMode.INCLUDES_TRUST
                } else {
                    OrgTypeMode.EXCLUDES_TRUST
                }
            state.isStateInitialized = true
        }

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configure {
                withAdditionalContentProperty { "title" to "landlordDetails.update.title" }
            }
            step(journey.orgTypeStep) {
                routeSegment(OrgTypeStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.orgTypeUpdateRoutingStep }
                withAdditionalContentProperties {
                    mapOf(
                        "submitButtonText" to "forms.buttons.continue",
                    )
                }
            }
            step(journey.orgTypeUpdateRoutingStep) {
                parents { journey.orgTypeStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        OrgTypeUpdateRouteMode.TRUST_UNCHANGED -> Destination(journey.orgTypeCyaStep)
                        OrgTypeUpdateRouteMode.ADDING_TRUST -> Destination(journey.orgTypeTrustInterruptionStep)
                        OrgTypeUpdateRouteMode.REMOVING_TRUST -> Destination(journey.orgTypeTrustInterruptionStep)
                    }
                }
            }
            step(journey.orgTypeTrustInterruptionStep) {
                routeSegment(OrgTypeTrustInterruptionStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.ADDING_TRUST),
                        journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.REMOVING_TRUST),
                    )
                }
                nextStep {
                    if (journey.orgTypeUpdateRoutingStep.outcome == OrgTypeUpdateRouteMode.ADDING_TRUST) {
                        journey.leadTrusteeTask.firstStep
                    } else {
                        journey.orgTypeCyaStep
                    }
                }
            }
            task(journey.leadTrusteeTask) {
                parents {
                    AndParents(
                        journey.orgTypeTrustInterruptionStep.isComplete(),
                        journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.ADDING_TRUST),
                    )
                }
                nextStep { journey.orgTypeCyaStep }
            }
            step(journey.orgTypeCyaStep) {
                routeSegment(OrgTypeCyaStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.TRUST_UNCHANGED),
                        journey.leadTrusteeTask.isComplete(),
                        AndParents(
                            journey.orgTypeTrustInterruptionStep.isComplete(),
                            journey.orgTypeUpdateRoutingStep.hasOutcome(OrgTypeUpdateRouteMode.REMOVING_TRUST),
                        ),
                    )
                }
                nextStep { journey.completeOrganisationTypeUpdateStep }
                withAdditionalContentProperties {
                    mapOf("submitButtonText" to "forms.buttons.confirmAndSubmitUpdate")
                }
            }
            step(journey.completeOrganisationTypeUpdateStep) {
                parents { journey.orgTypeCyaStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

interface UpdateOrganisationTypeJourneyState :
    JourneyState,
    OrgTypeUpdateState {
    override val orgTypeStep: OrgTypeStep
    val orgTypeUpdateRoutingStep: OrgTypeUpdateRoutingStep
    val orgTypeTrustInterruptionStep: OrgTypeTrustInterruptionStep
    val leadTrusteeTask: LeadTrusteeTask
    val orgTypeCyaStep: OrgTypeCyaStep
    val completeOrganisationTypeUpdateStep: CompleteOrganisationTypeUpdateStep
    var isStateInitialized: Boolean
}

@JourneyFrameworkComponent
class UpdateOrganisationTypeJourney(
    override val orgTypeStep: OrgTypeStep,
    override val orgTypeUpdateRoutingStep: OrgTypeUpdateRoutingStep,
    override val orgTypeTrustInterruptionStep: OrgTypeTrustInterruptionStep,
    override val leadTrusteeTask: LeadTrusteeTask,
    override val orgTypeCyaStep: OrgTypeCyaStep,
    override val completeOrganisationTypeUpdateStep: CompleteOrganisationTypeUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-type",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationTypeJourneyState {
    override var previousOrgTypeMode: OrgTypeMode by delegateProvider.requiredDelegate("previous-org-type")
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}
