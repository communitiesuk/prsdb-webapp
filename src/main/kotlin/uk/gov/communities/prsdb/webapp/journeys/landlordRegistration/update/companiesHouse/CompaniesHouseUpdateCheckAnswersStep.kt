package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompanyNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCompanyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompaniesHouseUpdateCheckAnswersStepConfig(
    private val landlordService: LandlordService,
) : AbstractCheckYourAnswersStepConfig<UpdateCompaniesHouseJourneyState>() {
    override fun chooseTemplate(state: UpdateCompaniesHouseJourneyState) = "forms/companiesHouseUpdateCheckAnswersForm"

    override fun getStepSpecificContent(state: UpdateCompaniesHouseJourneyState): Map<String, Any?> {
        val registeredWithCompaniesHouse = isRegisteredWithCompaniesHouse(state)
        return mapOf(
            "title" to "landlordDetails.update.title",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
            "companyVariant" to registeredWithCompaniesHouse,
            "summaryListData" to getCompanyDetailsRows(state, registeredWithCompaniesHouse),
            "governingBodyMemberCards" to
                if (registeredWithCompaniesHouse) emptyList() else getGovBodyMemberCards(state),
        )
    }

    override fun afterStepDataIsAdded(state: UpdateCompaniesHouseJourneyState) {
        if (isRegisteredWithCompaniesHouse(state)) {
            val companyNumber =
                state.updateCompaniesHouseTask.orgCompanyNumberStep.formModel
                    .notNullValue(OrgCompanyNumberFormModel::companyNumber)
            landlordService.updateOrganisationalLandlordToRegisteredCompany(companyNumber)
        } else {
            landlordService.updateOrganisationalLandlordToNonRegisteredCompany(
                state.updateCompaniesHouseTask.governingBodyMembersMap?.values?.toList().orEmpty(),
            )
        }
    }

    private fun isRegisteredWithCompaniesHouse(state: UpdateCompaniesHouseJourneyState): Boolean =
        state.updateCompaniesHouseTask.orgIsRegisteredCompanyStep.formModel
            .notNullValue(OrgIsRegisteredCompanyFormModel::companiesHouse)

    private fun getCompanyDetailsRows(
        state: UpdateCompaniesHouseJourneyState,
        registeredWithCompaniesHouse: Boolean,
    ): List<SummaryListRowViewModel> {
        val task = state.updateCompaniesHouseTask
        return buildList {
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "landlordDetails.org.registeredWithCompaniesHouse",
                    registeredWithCompaniesHouse,
                    Destination.VisitableStep(
                        task.orgIsRegisteredCompanyStep,
                        state.getCyaJourneyId(task.orgIsRegisteredCompanyStep),
                    ),
                ),
            )
            if (registeredWithCompaniesHouse) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "landlordDetails.org.companyNumber",
                        task.orgCompanyNumberStep.formModel.notNullValue(OrgCompanyNumberFormModel::companyNumber),
                        Destination.VisitableStep(
                            task.orgCompanyNumberStep,
                            state.getCyaJourneyId(task.orgCompanyNumberStep),
                        ),
                    ),
                )
            }
        }
    }

    private fun getGovBodyMemberCards(state: UpdateCompaniesHouseJourneyState): List<SummaryCardViewModel> {
        val task = state.updateCompaniesHouseTask
        val members = task.governingBodyMembersMap ?: emptyMap()
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
                                memberRoleKey(member.type),
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
                                task.orgGovBodyMembersTask.orgGovBodyMemberListStep,
                                state.getCyaJourneyId(task.orgGovBodyMembersTask.orgGovBodyMemberListStep),
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

    private fun memberRoleKey(type: GoverningBodyMemberType) =
        when (type) {
            GoverningBodyMemberType.DIRECTOR -> "landlordDetails.org.governingBody.type.director"
            GoverningBodyMemberType.TRUSTEE -> "landlordDetails.org.governingBody.type.trustee"
            GoverningBodyMemberType.PARTNER -> "landlordDetails.org.governingBody.type.partner"
            GoverningBodyMemberType.OTHER -> "landlordDetails.org.governingBody.type.other"
        }
}

@JourneyFrameworkComponent
final class CompaniesHouseUpdateCheckAnswersStep(
    stepConfig: CompaniesHouseUpdateCheckAnswersStepConfig,
) : AbstractCheckYourAnswersStep<UpdateCompaniesHouseJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-answers"
    }
}
