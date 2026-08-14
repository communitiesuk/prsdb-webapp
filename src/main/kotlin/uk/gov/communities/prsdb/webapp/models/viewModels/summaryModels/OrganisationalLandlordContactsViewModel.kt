package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationMainContactController
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody.InitialiseGovBodyMembersForGovBodyUpdateStep

class OrganisationalLandlordContactsViewModel(
    orgLandlord: OrganisationalLandlord,
    governingBodyMembers: List<OrganisationGoverningBodyMember>,
) {
    val mainContactCard: SummaryCardViewModel =
        SummaryCardViewModel(
            title = "landlordDetails.org.mainContactHeading",
            actions =
                SummaryCardActionViewModel.changeAction(
                    "${UpdateOrganisationMainContactController.UPDATE_ORG_MAIN_CONTACT_ROUTE}/${OrgMainContactStep.ROUTE_SEGMENT}",
                ),
            summaryList =
                mutableListOf<SummaryListRowViewModel>()
                    .apply {
                        addRow("landlordDetails.org.mainContactName", orgLandlord.mainContactName)
                        addRow("landlordDetails.org.mainContactEmail", orgLandlord.mainContactEmail)
                        addRow("landlordDetails.org.mainContactPhone", orgLandlord.mainContactPhone)
                    }.toList(),
        )

    val leadTrusteeCard: SummaryCardViewModel? =
        if (!orgLandlord.hasLeadTrustee) {
            null
        } else {
            SummaryCardViewModel(
                title = "landlordDetails.org.leadTrusteeHeading",
                actions =
                    SummaryCardActionViewModel.changeAction(
                        "${UpdateLeadTrusteeController.UPDATE_LEAD_TRUSTEE_ROUTE}/${LeadTrusteeNameStep.ROUTE_SEGMENT}",
                    ),
                summaryList =
                    mutableListOf<SummaryListRowViewModel>()
                        .apply {
                            addRow("landlordDetails.org.leadTrusteeName", orgLandlord.leadTrusteeName)
                            addRow("landlordDetails.org.leadTrusteeDateOfBirth", orgLandlord.leadTrusteeDateOfBirth)
                            addRow("landlordDetails.org.leadTrusteeEmail", orgLandlord.leadTrusteeEmail)
                            addRow("landlordDetails.org.leadTrusteePhone", orgLandlord.leadTrusteePhone)
                            addRow(
                                "landlordDetails.org.leadTrusteeAddress",
                                orgLandlord.leadTrusteeAddress?.toMultiLineAddress()?.split("\n"),
                            )
                        }.toList(),
            )
        }

    val showGoverningBody: Boolean = orgLandlord.hasGoverningBody

    val governingBodyMembersLinkUrl: String =
        "${UpdateGoverningBodyController.UPDATE_GOVERNING_BODY_ROUTE}/${InitialiseGovBodyMembersForGovBodyUpdateStep.ROUTE_SEGMENT}"

    val governingBodyMemberCards: List<SummaryCardViewModel> =
        governingBodyMembers.mapIndexed { index, member ->
            SummaryCardViewModel(
                title = memberCardTitleKey(member.type),
                cardNumber = (index + 1).toString(),
                summaryList =
                    mutableListOf<SummaryListRowViewModel>()
                        .apply {
                            addRow("landlordDetails.org.governingBody.role", member.type)
                            addRow("landlordDetails.org.governingBody.memberName", member.name)
                            addRow("landlordDetails.org.governingBody.memberDateOfBirth", member.dateOfBirth)
                            addRow(
                                "landlordDetails.org.governingBody.memberAddress",
                                member.address.toMultiLineAddress().split("\n"),
                            )
                        }.toList(),
            )
        }

    val registrationContactCard: SummaryCardViewModel =
        SummaryCardViewModel(
            title = "landlordDetails.org.registrationContactHeading",
            summaryList =
                mutableListOf<SummaryListRowViewModel>()
                    .apply {
                        addRow("landlordDetails.org.registrationContactName", orgLandlord.registrantName)
                        addRow("landlordDetails.org.registrationContactDateOfBirth", orgLandlord.registrantDateOfBirth)
                        addRow("landlordDetails.org.registrationContactEmail", orgLandlord.registrantEmail)
                        addRow("landlordDetails.org.registrationContactPhone", orgLandlord.registrantPhoneNumber)
                    }.toList(),
        )

    companion object {
        private fun memberCardTitleKey(type: GoverningBodyMemberType): String =
            when (type) {
                GoverningBodyMemberType.DIRECTOR -> "landlordDetails.org.governingBody.memberCardTitle.director"
                GoverningBodyMemberType.TRUSTEE -> "landlordDetails.org.governingBody.memberCardTitle.trustee"
                GoverningBodyMemberType.PARTNER -> "landlordDetails.org.governingBody.memberCardTitle.partner"
                GoverningBodyMemberType.OTHER -> "landlordDetails.org.governingBody.memberCardTitle.other"
            }
    }
}
