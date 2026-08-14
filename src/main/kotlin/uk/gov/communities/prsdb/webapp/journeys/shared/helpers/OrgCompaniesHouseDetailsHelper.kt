package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompanyNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCompanyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class OrgCompaniesHouseDetailsHelper {
    fun getCompanyDetailsRows(
        state: CheckYourAnswersJourneyState,
        orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
        orgCompanyNumberStep: OrgCompanyNumberStep,
    ): List<SummaryListRowViewModel> {
        val registeredWithCompaniesHouse =
            orgIsRegisteredCompanyStep.formModel.notNullValue(OrgIsRegisteredCompanyFormModel::companiesHouse)
        return buildList {
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "landlordDetails.org.registeredWithCompaniesHouse",
                    registeredWithCompaniesHouse,
                    Destination.VisitableStep(orgIsRegisteredCompanyStep, state.getCyaJourneyId(orgIsRegisteredCompanyStep)),
                ),
            )
            if (registeredWithCompaniesHouse) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.companyNumber",
                        orgCompanyNumberStep.formModel.notNullValue(OrgCompanyNumberFormModel::companyNumber),
                        Destination.VisitableStep(orgCompanyNumberStep, state.getCyaJourneyId(orgCompanyNumberStep)),
                    ),
                )
            }
        }
    }

    fun getGovBodyMemberCards(
        state: CheckYourAnswersJourneyState,
        membersTask: OrgGovBodyMembersState,
    ): List<SummaryCardViewModel> {
        val members = membersTask.governingBodyMembersMap ?: emptyMap()
        return members
            .toList()
            .sortedBy { it.first }
            .mapIndexed { displayIndex, (_, member) ->
                SummaryCardViewModel(
                    title = memberCardTitleKey(member.type),
                    cardNumber = (displayIndex + 1).toString(),
                    summaryList =
                        listOf(
                            SummaryListRowViewModel.forCheckYourAnswersPage(
                                "landlordDetails.org.governingBody.role",
                                member.type,
                                Destination.Nowhere(),
                            ),
                            SummaryListRowViewModel.forCheckYourAnswersPage(
                                "landlordDetails.org.governingBody.memberName",
                                member.name,
                                Destination.Nowhere(),
                            ),
                            SummaryListRowViewModel.forCheckYourAnswersPage(
                                "landlordDetails.org.governingBody.memberDateOfBirth",
                                member.dateOfBirth,
                                Destination.Nowhere(),
                            ),
                            SummaryListRowViewModel.forCheckYourAnswersPage(
                                "landlordDetails.org.governingBody.memberAddress",
                                member.address.toMultiLineAddress().split("\n"),
                                Destination.Nowhere(),
                            ),
                        ),
                    actions =
                        SummaryCardActionViewModel.changeAction(
                            Destination.VisitableStep(
                                membersTask.orgGovBodyMemberListStep,
                                state.getCyaJourneyId(membersTask.orgGovBodyMemberListStep),
                            ),
                        ),
                )
            }
    }

    private fun memberCardTitleKey(type: GoverningBodyMemberType) =
        when (type) {
            GoverningBodyMemberType.DIRECTOR -> "landlordDetails.org.governingBody.memberCardTitle.director"
            GoverningBodyMemberType.TRUSTEE -> "landlordDetails.org.governingBody.memberCardTitle.trustee"
            GoverningBodyMemberType.PARTNER -> "landlordDetails.org.governingBody.memberCardTitle.partner"
            GoverningBodyMemberType.OTHER -> "landlordDetails.org.governingBody.memberCardTitle.other"
        }
}
