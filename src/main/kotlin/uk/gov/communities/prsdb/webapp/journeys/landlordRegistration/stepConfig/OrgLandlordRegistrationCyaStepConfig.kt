package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberEnglandAndWalesFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberNorthernIrelandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberScotlandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompaniesHouseFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompanyNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgPhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class OrgLandlordRegistrationCyaStepConfig : AbstractCheckYourAnswersStepConfig<LandlordRegistrationState>() {
    override fun chooseTemplate(state: LandlordRegistrationState) = "forms/orgLandlordRegistrationCheckAnswersForm"

    override fun getStepSpecificContent(state: LandlordRegistrationState): Map<String, Any?> =
        mapOf(
            "title" to "registerAsALandlord.title",
            "submitButtonText" to "registerAsALandlord.orgCheckAnswers.submitButton",
            "yourDetailsCard" to getYourDetailsCard(state),
            "landlordDetails" to getLandlordDetailsRows(state),
            "leadTrusteeCard" to getLeadTrusteeCard(state),
            "governingBodyMemberCards" to getGovBodyMemberCards(state),
            "mainContactCard" to getMainContactCard(state),
        )

    override fun afterStepDataIsAdded(state: LandlordRegistrationState) {
        // TODO: PDJB-1180 - persist the organisation landlord on submit.
    }

    // Overrides AbstractCheckYourAnswersStepConfig, which deleted the journey
    // We don't want to delete the journey at this stage when this page is included within another journey,
    // such as accepting a joint landlord invitation
    override fun resolveNextDestination(
        state: LandlordRegistrationState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun getYourDetailsCard(state: LandlordRegistrationState): SummaryCardViewModel {
        val verified = state.identityTask.getIsIdentityVerified()
        val rows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.yourDetails.name",
                    state.identityTask.getName(),
                    if (verified) {
                        Destination.Nowhere()
                    } else {
                        Destination.VisitableStep(state.identityTask.nameStep, state.getCyaJourneyId(state.identityTask.nameStep))
                    },
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.yourDetails.dateOfBirth",
                    state.identityTask.getDateOfBirth(),
                    if (verified) {
                        Destination.Nowhere()
                    } else {
                        Destination.VisitableStep(
                            state.identityTask.dateOfBirthStep,
                            state.getCyaJourneyId(state.identityTask.dateOfBirthStep),
                        )
                    },
                ),
                // TODO: PDJB-1282 - replace these dummy email and phone rows with the user's real contact details once collected.
                dummyRow("registerAsALandlord.orgCheckAnswers.yourDetails.email", "Indiana.jones@marshallCollege.com"),
                dummyRow("registerAsALandlord.orgCheckAnswers.yourDetails.phoneNumber", "020 7123 4567"),
            )
        return SummaryCardViewModel(
            title = "registerAsALandlord.orgCheckAnswers.yourDetails.cardTitle",
            summaryList = rows,
        )
    }

    private fun getLandlordDetailsRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> {
        val org = state.orgLandlordRegistrationTask

        return buildList {
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.landlordType",
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.landlordTypeValue",
                    Destination.VisitableStep(state.landlordTypeStep, state.getCyaJourneyId(state.landlordTypeStep)),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationName",
                    org.orgNameStep.formModel.notNullValue(OrgNameFormModel::orgName),
                    Destination.VisitableStep(org.orgNameStep, state.getCyaJourneyId(org.orgNameStep)),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationAddress",
                    orgAddressLines(org.orgAddressStep.formModel),
                    Destination.VisitableStep(org.orgAddressStep, state.getCyaJourneyId(org.orgAddressStep)),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationEmail",
                    org.orgEmailStep.formModel.notNullValue(EmailFormModel::emailAddress),
                    Destination.VisitableStep(org.orgEmailStep, state.getCyaJourneyId(org.orgEmailStep)),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationPhone",
                    org.orgPhoneNumberStep.formModel.notNullValue(OrgPhoneNumberFormModel::phoneNumber),
                    Destination.VisitableStep(org.orgPhoneNumberStep, state.getCyaJourneyId(org.orgPhoneNumberStep)),
                ),
            )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationType",
                    org.orgTypeStep.formModel.orgTypes
                        .filterNotNull()
                        .filter { it.isNotBlank() }
                        .map { orgTypeMessageKey(it) },
                    Destination.VisitableStep(org.orgTypeStep, state.getCyaJourneyId(org.orgTypeStep)),
                ),
            )

            val isCharity = org.orgCharityStep.formModel.notNullValue(OrgCharityFormModel::charity)
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.registeredCharity",
                    isCharity,
                    Destination.VisitableStep(org.orgCharityStep, state.getCyaJourneyId(org.orgCharityStep)),
                ),
            )
            if (isCharity) {
                val regulator = org.orgCharityRegisteredWithStep.formModel.charityRegisteredWith
                if (regulator != null) {
                    add(
                        SummaryListRowViewModel.forCheckYourAnswersPage(
                            "registerAsALandlord.orgCheckAnswers.landlordDetails.charityCommission",
                            regulatorMessageKey(regulator),
                            Destination.VisitableStep(
                                org.orgCharityRegisteredWithStep,
                                state.getCyaJourneyId(org.orgCharityRegisteredWithStep),
                            ),
                        ),
                    )
                    charityNumberRow(state, regulator)?.let { add(it) }
                }
            }

            val registeredWithCompaniesHouse =
                org.orgCompaniesHouseStep.formModel.notNullValue(
                    OrgCompaniesHouseFormModel::companiesHouse,
                )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.registeredWithCompaniesHouse",
                    registeredWithCompaniesHouse,
                    Destination.VisitableStep(org.orgCompaniesHouseStep, state.getCyaJourneyId(org.orgCompaniesHouseStep)),
                ),
            )
            if (registeredWithCompaniesHouse) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "registerAsALandlord.orgCheckAnswers.landlordDetails.companiesHouseNumber",
                        org.orgCompanyNumberStep.formModel.notNullValue(OrgCompanyNumberFormModel::companyNumber),
                        Destination.VisitableStep(org.orgCompanyNumberStep, state.getCyaJourneyId(org.orgCompanyNumberStep)),
                    ),
                )
            }
        }
    }

    private fun charityNumberRow(
        state: LandlordRegistrationState,
        regulator: CharityRegulator,
    ): SummaryListRowViewModel? {
        val org = state.orgLandlordRegistrationTask
        val headingKey = "registerAsALandlord.orgCheckAnswers.landlordDetails.charityNumber"
        return when (regulator) {
            CharityRegulator.ENGLAND_AND_WALES ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    org.orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(OrgCharityNumberEnglandAndWalesFormModel::charityNumber),
                    Destination.VisitableStep(
                        org.orgCharityNumberEnglandAndWalesStep,
                        state.getCyaJourneyId(org.orgCharityNumberEnglandAndWalesStep),
                    ),
                )

            CharityRegulator.NORTHERN_IRELAND ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    org.orgCharityNumberNorthernIrelandStep.formModel.notNullValue(OrgCharityNumberNorthernIrelandFormModel::charityNumber),
                    Destination.VisitableStep(
                        org.orgCharityNumberNorthernIrelandStep,
                        state.getCyaJourneyId(org.orgCharityNumberNorthernIrelandStep),
                    ),
                )

            CharityRegulator.SCOTLAND ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    org.orgCharityNumberScotlandStep.formModel.notNullValue(OrgCharityNumberScotlandFormModel::charityNumber),
                    Destination.VisitableStep(
                        org.orgCharityNumberScotlandStep,
                        state.getCyaJourneyId(org.orgCharityNumberScotlandStep),
                    ),
                )

            CharityRegulator.NONE -> null
        }
    }

    private fun getLeadTrusteeCard(state: LandlordRegistrationState): SummaryCardViewModel {
        val org = state.orgLandlordRegistrationTask
        val rows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.name",
                    org.leadTrusteeNameStep.formModel.notNullValue(LeadTrusteeNameFormModel::name),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.dateOfBirth",
                    org.leadTrusteeDobStep.formModel.toLocalDateOrNull(),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.email",
                    org.leadTrusteeEmailStep.formModel.notNullValue(LeadTrusteeEmailFormModel::emailAddress),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.phoneNumber",
                    org.leadTrusteePhoneStep.formModel.notNullValue(LeadTrusteePhoneFormModel::phoneNumber),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.address",
                    org.trusteeAddressTask.getAddress().toMultiLineAddress().split("\n"),
                    Destination.Nowhere(),
                ),
            )
        return SummaryCardViewModel(
            title = "registerAsALandlord.orgCheckAnswers.governingBody.leadTrusteeCardTitle",
            summaryList = rows,
            actions = orgCardChangeAction(state, org.leadTrusteeNameStep),
        )
    }

    private fun getGovBodyMemberCards(state: LandlordRegistrationState): List<SummaryCardViewModel> {
        val members = state.orgLandlordRegistrationTask.governingBodyMembersMap ?: emptyMap()
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
                                "registerAsALandlord.orgCheckAnswers.governingBody.role",
                                memberRoleKey(member.type),
                                Destination.Nowhere(),
                            ),
                            SummaryListRowViewModel.forCheckYourAnswersPage(
                                "registerAsALandlord.orgCheckAnswers.governingBody.name",
                                member.name,
                                Destination.Nowhere(),
                            ),
                            SummaryListRowViewModel.forCheckYourAnswersPage(
                                "registerAsALandlord.orgCheckAnswers.governingBody.dateOfBirth",
                                member.dateOfBirth,
                                Destination.Nowhere(),
                            ),
                            SummaryListRowViewModel.forCheckYourAnswersPage(
                                "registerAsALandlord.orgCheckAnswers.governingBody.address",
                                member.address.toMultiLineAddress().split("\n"),
                                Destination.Nowhere(),
                            ),
                        ),
                    // TODO: PDJB-1290 - replace with the real change URL once governing body member editing is wired up.
                    actions = listOf(SummaryCardActionViewModel(text = "forms.links.change", url = PLACEHOLDER_CHANGE_URL)),
                )
            }
    }

    private fun memberCardTitleKey(type: GoverningBodyMemberType) =
        when (type) {
            GoverningBodyMemberType.DIRECTOR -> "registerAsALandlord.orgCheckAnswers.governingBody.memberCardTitle.director"
            GoverningBodyMemberType.TRUSTEE -> "registerAsALandlord.orgCheckAnswers.governingBody.memberCardTitle.trustee"
            GoverningBodyMemberType.PARTNER -> "registerAsALandlord.orgCheckAnswers.governingBody.memberCardTitle.partner"
            GoverningBodyMemberType.OTHER -> "registerAsALandlord.orgCheckAnswers.governingBody.memberCardTitle.other"
        }

    private fun memberRoleKey(type: GoverningBodyMemberType) =
        when (type) {
            GoverningBodyMemberType.DIRECTOR -> "registerAsALandlord.orgGovBodyWhoToProvide.radios.director"
            GoverningBodyMemberType.TRUSTEE -> "registerAsALandlord.orgGovBodyWhoToProvide.radios.trustee"
            GoverningBodyMemberType.PARTNER -> "registerAsALandlord.orgGovBodyWhoToProvide.radios.partner"
            GoverningBodyMemberType.OTHER -> "registerAsALandlord.orgGovBodyWhoToProvide.radios.otherMember"
        }

    private fun getMainContactCard(state: LandlordRegistrationState): SummaryCardViewModel {
        val org = state.orgLandlordRegistrationTask
        val mainContact = org.orgMainContactStep.formModel
        val rows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.mainContact.name",
                    mainContact.notNullValue(OrgMainContactFormModel::name),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.mainContact.email",
                    mainContact.notNullValue(OrgMainContactFormModel::emailAddress),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.mainContact.phoneNumber",
                    mainContact.notNullValue(OrgMainContactFormModel::phoneNumber),
                    Destination.Nowhere(),
                ),
            )
        return SummaryCardViewModel(
            title = "registerAsALandlord.orgCheckAnswers.mainContact.cardTitle",
            summaryList = rows,
            actions = orgCardChangeAction(state, org.orgMainContactStep),
        )
    }

    // TODO: PDJB-1282 - dummy row with a non-functional Change link; replace once the underlying step exists.
    private fun dummyRow(
        headingKey: String,
        value: Any?,
    ) = SummaryListRowViewModel(
        fieldHeading = headingKey,
        fieldValue = value,
        actions = listOf(SummaryListRowActionsViewModel("forms.links.change", PLACEHOLDER_CHANGE_URL)),
    )

    private fun orgCardChangeAction(
        state: LandlordRegistrationState,
        step: RequestableStep<*, *, *>,
    ): List<SummaryCardActionViewModel> =
        listOfNotNull(
            Destination.VisitableStep(step, state.getCyaJourneyId(step)).toUrlStringOrNull()?.let {
                SummaryCardActionViewModel(text = "forms.links.change", url = it)
            },
        )

    // TODO: PDJB-1133 - this only handles the manually-entered organisation address; handle looked-up (auto) address data once org address lookup exists.
    private fun orgAddressLines(address: ManualAddressFormModel) =
        AddressDataModel
            .fromManualAddressData(
                addressLineOne = address.notNullValue(ManualAddressFormModel::addressLineOne),
                addressLineTwo = address.addressLineTwo,
                townOrCity = address.notNullValue(ManualAddressFormModel::townOrCity),
                county = address.county,
                postcode = address.notNullValue(ManualAddressFormModel::postcode),
            ).toMultiLineAddress()
            .split("\n")

    private fun orgTypeMessageKey(orgTypeName: String) =
        when (orgTypeName) {
            OrgType.COMPANY.name -> "registerAsALandlord.orgType.checkbox.company"
            OrgType.CHARITY.name -> "registerAsALandlord.orgType.checkbox.charity"
            OrgType.TRUST.name -> "registerAsALandlord.orgType.checkbox.trust"
            else -> "commonText.other"
        }

    private fun regulatorMessageKey(regulator: CharityRegulator) =
        when (regulator) {
            CharityRegulator.ENGLAND_AND_WALES -> "forms.orgCharityRegisteredWith.radios.option.englandAndWales"
            CharityRegulator.NORTHERN_IRELAND -> "forms.orgCharityRegisteredWith.radios.option.northernIreland"
            CharityRegulator.SCOTLAND -> "forms.orgCharityRegisteredWith.radios.option.scotland"
            CharityRegulator.NONE -> "commonText.other"
        }

    companion object {
        // TODO: PDJB-1282 (your details email/phone) / PDJB-1290 (governing body member change link) -
        // non-functional Change link placeholder until the underlying steps exist.
        private const val PLACEHOLDER_CHANGE_URL = "#"
    }
}

@JourneyFrameworkComponent
final class OrgLandlordRegistrationCyaStep(
    stepConfig: OrgLandlordRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<LandlordRegistrationState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-check-answers"
    }
}
