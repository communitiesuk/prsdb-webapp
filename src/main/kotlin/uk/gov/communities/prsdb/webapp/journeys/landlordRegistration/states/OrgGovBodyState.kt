package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
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
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

interface OrgGovBodyState : JourneyState {
    val orgGovBodyDetailsStep: OrgGovBodyDetailsStep
    val orgGovBodyMustProvideInfoStep: OrgGovBodyMustProvideInfoStep
    val orgGovBodyWhoToProvideStep: OrgGovBodyWhoToProvideStep
    val orgGovBodyMemberNameStep: OrgGovBodyMemberNameStep
    val orgGovBodyMemberDobStep: OrgGovBodyMemberDobStep
    val govBodyMemberAddressTask: AddressTask
    val orgGovBodyMemberListStep: OrgGovBodyMemberListStep
    val hasAnyGovBodyMembersStep: HasAnyGovBodyMembersStep
    val saveGovBodyMemberStep: SaveGovBodyMemberStep
    val setStateForGovBodyMemberEditStep: SetStateForGovBodyMemberEditStep
    val removeGovBodyMemberStep: RemoveGovBodyMemberStep
    var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>?
    var nextGoverningBodyMemberId: Int?
    var editingGovBodyMemberId: Int?
}
