package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.HasAnyGovBodyMembersStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.RemoveGovBodyMemberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.SaveGovBodyMemberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.SetStateForGovBodyMemberEditStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

@JourneyFrameworkComponent
class OrgGovBodyMembersTask(
    journeyStateService: JourneyStateService,
    override val orgGovBodyWhoToProvideStep: OrgGovBodyWhoToProvideStep,
    override val orgGovBodyMemberNameStep: OrgGovBodyMemberNameStep,
    override val orgGovBodyMemberDobStep: OrgGovBodyMemberDobStep,
    override val govBodyMemberAddressTask: GovBodyMemberAddressTask,
    override val orgGovBodyMemberListStep: OrgGovBodyMemberListStep,
    override val hasAnyGovBodyMembersStep: HasAnyGovBodyMembersStep,
    override val saveGovBodyMemberStep: SaveGovBodyMemberStep,
    override val setStateForGovBodyMemberEditStep: SetStateForGovBodyMemberEditStep,
    override val removeGovBodyMemberStep: RemoveGovBodyMemberStep,
) : Task<OrgGovBodyMembersState, OrgGovBodyMembersDependencies>(journeyStateService),
    OrgGovBodyMembersState {
    override val taskState get() = this

    override var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>?
        get() = dependencies.listState.governingBodyMembersMap
        set(value) {
            dependencies.listState.governingBodyMembersMap = value
        }
    override var nextGoverningBodyMemberId: Int?
        get() = dependencies.listState.nextGoverningBodyMemberId
        set(value) {
            dependencies.listState.nextGoverningBodyMemberId = value
        }
    override var editingGovBodyMemberId: Int?
        get() = dependencies.listState.editingGovBodyMemberId
        set(value) {
            dependencies.listState.editingGovBodyMemberId = value
        }

    override fun makeSubJourney(state: OrgGovBodyMembersState) =
        subJourney(state) {
            step(journey.hasAnyGovBodyMembersStep) {
                backDestination { dependencies.govBodyMembersIntroBackDestination() }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.orgGovBodyWhoToProvideStep
                        AnyMembers.SOME_MEMBERS -> journey.orgGovBodyMemberListStep
                    }
                }
            }
            step(journey.setStateForGovBodyMemberEditStep) {
                routeSegment(SetStateForGovBodyMemberEditStep.ROUTE_SEGMENT)
                parents { journey.hasAnyGovBodyMembersStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                nextStep { journey.orgGovBodyWhoToProvideStep }
            }
            step(journey.removeGovBodyMemberStep) {
                routeSegment(RemoveGovBodyMemberStep.ROUTE_SEGMENT)
                parents { journey.hasAnyGovBodyMembersStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                nextDestination { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> Destination(journey.orgGovBodyMemberListStep)
                        AnyMembers.NO_MEMBERS -> dependencies.govBodyMembersIntroBackDestination()
                    }
                }
            }
            step(journey.orgGovBodyWhoToProvideStep) {
                routeSegment(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT)
                parents { journey.hasAnyGovBodyMembersStep.isComplete() }
                backDestination {
                    if (journey.governingBodyMembersMap.isNullOrEmpty()) {
                        dependencies.govBodyMembersIntroBackDestination()
                    } else {
                        Destination(journey.orgGovBodyMemberListStep)
                    }
                }
                nextStep { journey.orgGovBodyMemberNameStep }
            }
            step(journey.orgGovBodyMemberNameStep) {
                routeSegment(OrgGovBodyMemberNameStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyWhoToProvideStep.isComplete() }
                nextStep { journey.orgGovBodyMemberDobStep }
            }
            step(journey.orgGovBodyMemberDobStep) {
                routeSegment(OrgGovBodyMemberDobStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyMemberNameStep.isComplete() }
                nextStep { journey.govBodyMemberAddressTask.firstStep }
            }
            task(journey.govBodyMemberAddressTask, GovBodyMemberAddressTask.ROUTE_SEGMENT) {
                parents { journey.orgGovBodyMemberDobStep.isComplete() }
                nextStep { journey.saveGovBodyMemberStep }
                configureStep(journey.govBodyMemberAddressTask.lookupAddressStep) {
                    withAdditionalContentProperties {
                        val editingMember = journey.editingGovBodyMember
                        if (editingMember != null) {
                            mapOf(
                                LookupAddressStepConfig.PREFILL_POSTCODE to editingMember.addressSearchPostcode,
                                LookupAddressStepConfig.PREFILL_HOUSE_NAME_OR_NUMBER to editingMember.addressSearchHouseNameOrNumber,
                            )
                        } else {
                            emptyMap()
                        }
                    }
                }
                configureStep(journey.govBodyMemberAddressTask.selectAddressStep) {
                    withAdditionalContentProperties {
                        val editingMember = journey.editingGovBodyMember
                        mapOf(
                            SelectAddressStepConfig.PREFILL_SELECTED_ADDRESS to editingMember?.selectedAddress,
                        )
                    }
                }
                configureStep(journey.govBodyMemberAddressTask.manualAddressStep) {
                    withAdditionalContentProperties {
                        val editingMember = journey.editingGovBodyMember
                        if (editingMember?.manualAddressLineOne != null) {
                            mapOf(
                                ManualAddressStepConfig.PREFILL_ADDRESS_LINE_ONE to editingMember.manualAddressLineOne,
                                ManualAddressStepConfig.PREFILL_ADDRESS_LINE_TWO to editingMember.manualAddressLineTwo,
                                ManualAddressStepConfig.PREFILL_TOWN_OR_CITY to editingMember.manualTownOrCity,
                                ManualAddressStepConfig.PREFILL_COUNTY to editingMember.manualCounty,
                                ManualAddressStepConfig.PREFILL_POSTCODE to editingMember.manualPostcode,
                            )
                        } else {
                            emptyMap()
                        }
                    }
                }
            }
            step(journey.saveGovBodyMemberStep) {
                parents { journey.govBodyMemberAddressTask.isComplete() }
                nextStep { journey.orgGovBodyMemberListStep }
            }
            step(journey.orgGovBodyMemberListStep) {
                routeSegment(OrgGovBodyMemberListStep.ROUTE_SEGMENT)
                parents {
                    journey.hasAnyGovBodyMembersStep.hasOutcome(AnyMembers.SOME_MEMBERS)
                }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.orgGovBodyMemberListStep.isComplete() }
            }
        }
}
