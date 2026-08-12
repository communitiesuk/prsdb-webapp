package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class UpdateCompaniesHouseTask(
    journeyStateService: JourneyStateService,
    private val userToLandlordService: UserToLandlordService,
    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
    override val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep,
    override val interruptionStep: OrgCompaniesHouseInterruptionStep,
    override val orgCompanyNumberStep: OrgCompanyNumberStep,
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val govBodyMembersBackRoutingStep: GovBodyMembersBackRoutingStep,
) : TaskWithoutDependencies<UpdateCompaniesHouseTaskState>(journeyStateService),
    UpdateCompaniesHouseTaskState {
    override val taskState get() = this

    override var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>? by delegateProvider.nullableDelegate(
        "governingBodyMembersMap",
    )
    override var nextGoverningBodyMemberId: Int? by delegateProvider.nullableDelegate("nextGoverningBodyMemberId")
    override var editingGovBodyMemberId: Int? by delegateProvider.nullableDelegate("editingGovBodyMemberId")

    private var governingBodyMembersInitialised: Boolean? by delegateProvider.nullableDelegate("governingBodyMembersInitialised")

    fun initialiseGoverningBodyMembersFromDatabase() {
        if (governingBodyMembersInitialised == true) return
        val existingMembers = userToLandlordService.getCurrentOrganisationLandlordForUser().governingBodyMembers
        governingBodyMembersMap =
            existingMembers
                .mapIndexed { index, member -> (index + 1) to GoverningBodyMemberDataModel.fromEntity(member) }
                .toMap()
        nextGoverningBodyMemberId = existingMembers.size + 1
        governingBodyMembersInitialised = true
    }

    override fun makeSubJourney(state: UpdateCompaniesHouseTaskState) =
        subJourney(state) {
            step(journey.orgIsRegisteredCompanyStep) {
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                nextStep { journey.orgCompaniesHouseUpdateRoutingStep }
            }
            step<OrgCompaniesHouseUpdateRouteMode, OrgCompaniesHouseUpdateRoutingStepConfig>(journey.orgCompaniesHouseUpdateRoutingStep) {
                stepSpecificInitialisation {
                    usingPreviousIsRegisteredCompany { getPreviousIsRegisteredCompanyFromDatabase(userToLandlordService) }
                }
                parents { journey.orgIsRegisteredCompanyStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY -> Destination(journey.orgCompanyNumberStep)
                        OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY -> Destination(journey.orgGovBodyMembersTask.firstStep)
                        OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY -> Destination(journey.interruptionStep)
                        OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY -> Destination(journey.interruptionStep)
                    }
                }
            }
            step(journey.interruptionStep) {
                routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
                    )
                }
                nextStep {
                    if (journey.orgCompaniesHouseUpdateRoutingStep.outcome == OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY) {
                        journey.orgCompanyNumberStep
                    } else {
                        journey.orgGovBodyMembersTask.firstStep
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        AndParents(
                            journey.interruptionStep.isComplete(),
                            journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                        ),
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY),
                    )
                }
                backDestination {
                    if (journey.orgCompaniesHouseUpdateRoutingStep.outcome == OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY) {
                        Destination(journey.interruptionStep)
                    } else {
                        Destination(journey.orgIsRegisteredCompanyStep)
                    }
                }
                nextStep { exitStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents {
                    OrParents(
                        AndParents(
                            journey.interruptionStep.isComplete(),
                            journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
                        ),
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY),
                    )
                }
                backDestination { govBodyMembersIntroBackDestination(journey) }
                nextStep { journey.govBodyMembersBackRoutingStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        listState = journey,
                    )
                }
            }
            step<AnyMembers, GovBodyMembersBackRoutingStepConfig>(journey.govBodyMembersBackRoutingStep) {
                stepSpecificInitialisation { usingMembersList { journey.governingBodyMembersMap } }
                parents { journey.orgGovBodyMembersTask.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> {
                            if (journey.orgCompaniesHouseUpdateRoutingStep.outcome ==
                                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY
                            ) {
                                journey.interruptionStep
                            } else {
                                journey.orgIsRegisteredCompanyStep
                            }
                        }

                        AnyMembers.SOME_MEMBERS -> {
                            exitStep
                        }
                    }
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.orgCompanyNumberStep.isComplete(),
                        journey.govBodyMembersBackRoutingStep.hasOutcome(AnyMembers.SOME_MEMBERS),
                    )
                }
            }
        }

    private fun govBodyMembersIntroBackDestination(state: UpdateCompaniesHouseTaskState): Destination =
        if (state.orgCompaniesHouseUpdateRoutingStep.outcome == OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY) {
            Destination(state.interruptionStep)
        } else {
            Destination(state.orgIsRegisteredCompanyStep)
        }
}
