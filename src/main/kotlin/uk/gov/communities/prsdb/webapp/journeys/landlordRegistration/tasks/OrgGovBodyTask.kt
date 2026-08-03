package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.HasAnyGovBodyMembersStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMustProvideInfoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyWhoToProvideStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.RemoveGovBodyMemberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.SaveGovBodyMemberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.SetStateForGovBodyMemberEditStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode

@JourneyFrameworkComponent
class OrgGovBodyTask(
    journeyStateService: JourneyStateService,
    override val orgGovBodyDetailsStep: OrgGovBodyDetailsStep,
    override val orgGovBodyMustProvideInfoStep: OrgGovBodyMustProvideInfoStep,
    override val orgGovBodyWhoToProvideStep: OrgGovBodyWhoToProvideStep,
    override val orgGovBodyMemberNameStep: OrgGovBodyMemberNameStep,
    override val orgGovBodyMemberDobStep: OrgGovBodyMemberDobStep,
    override val govBodyMemberAddressTask: GovBodyMemberAddressTask,
    override val orgGovBodyMemberListStep: OrgGovBodyMemberListStep,
    override val hasAnyGovBodyMembersStep: HasAnyGovBodyMembersStep,
    override val saveGovBodyMemberStep: SaveGovBodyMemberStep,
    override val setStateForGovBodyMemberEditStep: SetStateForGovBodyMemberEditStep,
    override val removeGovBodyMemberStep: RemoveGovBodyMemberStep,
) : TaskWithoutDependencies<OrgGovBodyState>(journeyStateService),
    OrgGovBodyState {
    override val taskState get() = this

    override var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>? by delegateProvider.nullableDelegate(
        "governingBodyMembersMap",
    )
    override var nextGoverningBodyMemberId: Int? by delegateProvider.nullableDelegate("nextGoverningBodyMemberId")
    override var editingGovBodyMemberId: Int? by delegateProvider.nullableDelegate("editingGovBodyMemberId")

    override fun makeSubJourney(state: OrgGovBodyState) =
        subJourney(state) {
            step(journey.orgGovBodyDetailsStep) {
                routeSegment(OrgGovBodyDetailsStep.ROUTE_SEGMENT)
                nextDestination { mode ->
                    when (mode) {
                        OrgGovBodyDetailsMode.HAS_DETAILS -> Destination(journey.hasAnyGovBodyMembersStep)
                        OrgGovBodyDetailsMode.NO_DETAILS -> Destination(journey.orgGovBodyMustProvideInfoStep)
                    }
                }
            }
            step(journey.orgGovBodyMustProvideInfoStep) {
                routeSegment(OrgGovBodyMustProvideInfoStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyDetailsStep.hasOutcome(OrgGovBodyDetailsMode.NO_DETAILS) }
                noNextDestination()
            }
            step(journey.hasAnyGovBodyMembersStep) {
                parents { journey.orgGovBodyDetailsStep.hasOutcome(OrgGovBodyDetailsMode.HAS_DETAILS) }
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
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> journey.orgGovBodyMemberListStep
                        AnyMembers.NO_MEMBERS -> journey.orgGovBodyDetailsStep
                    }
                }
            }
            step(journey.orgGovBodyWhoToProvideStep) {
                routeSegment(OrgGovBodyWhoToProvideStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyDetailsStep.hasOutcome(OrgGovBodyDetailsMode.HAS_DETAILS) }
                backDestination {
                    if (journey.governingBodyMembersMap.isNullOrEmpty()) {
                        Destination(journey.orgGovBodyDetailsStep)
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
            }
            step(journey.saveGovBodyMemberStep) {
                parents { journey.govBodyMemberAddressTask.isComplete() }
                nextStep { journey.orgGovBodyMemberListStep }
                configureStep(journey.govBodyMemberAddressTask.selectAddressStep) {
                    withAdditionalContentProperties {
                        mapOf("fieldSetHeading" to "forms.selectAddress.govBodyMemberRegistration.fieldSetHeading")
                    }
                }
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
