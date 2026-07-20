package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
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
            "governingBodyMemberCards" to getGovBodyMemberCards(),
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
                    if (verified) Destination.Nowhere() else orgChangeDestination(state, state.identityTask.nameStep),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.yourDetails.dateOfBirth",
                    state.identityTask.getDateOfBirth(),
                    if (verified) Destination.Nowhere() else orgChangeDestination(state, state.identityTask.dateOfBirthStep),
                ),
                // TODO: PDJB-1172 - replace these dummy email and phone rows with the user's real contact details once collected.
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
        val rows = mutableListOf<SummaryListRowViewModel>()

        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.landlordType",
                "registerAsALandlord.landlordType.radios.organisation.label",
                state.landlordTypeStep,
            )
        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationName",
                org.orgNameStep.formModel.notNullValue(OrgNameFormModel::orgName),
                org.orgNameStep,
            )
        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationAddress",
                orgAddressLines(org.orgAddressStep.formModel),
                org.orgAddressStep,
            )
        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationEmail",
                org.orgEmailStep.formModel.notNullValue(EmailFormModel::emailAddress),
                org.orgEmailStep,
            )
        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationPhone",
                org.orgPhoneNumberStep.formModel.notNullValue(OrgPhoneNumberFormModel::phoneNumber),
                org.orgPhoneNumberStep,
            )
        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.organisationType",
                org.orgTypeStep.formModel.orgTypes
                    .filterNotNull()
                    .filter { it.isNotBlank() }
                    .map { orgTypeMessageKey(it) },
                org.orgTypeStep,
            )

        val isCharity = org.orgCharityStep.formModel.notNullValue(OrgCharityFormModel::charity)
        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.registeredCharity",
                isCharity,
                org.orgCharityStep,
            )
        if (isCharity) {
            val regulator = org.orgCharityRegisteredWithStep.formModel.charityRegisteredWith
            if (regulator != null) {
                rows +=
                    orgRow(
                        state,
                        "registerAsALandlord.orgCheckAnswers.landlordDetails.charityCommission",
                        regulatorMessageKey(regulator),
                        org.orgCharityRegisteredWithStep,
                    )
                charityNumberRow(state, regulator)?.let { rows += it }
            }
        }

        val registeredWithCompaniesHouse = org.orgCompaniesHouseStep.formModel.notNullValue(OrgCompaniesHouseFormModel::companiesHouse)
        rows +=
            orgRow(
                state,
                "registerAsALandlord.orgCheckAnswers.landlordDetails.registeredWithCompaniesHouse",
                registeredWithCompaniesHouse,
                org.orgCompaniesHouseStep,
            )
        if (registeredWithCompaniesHouse) {
            rows +=
                orgRow(
                    state,
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.companiesHouseNumber",
                    org.orgCompanyNumberStep.formModel.notNullValue(OrgCompanyNumberFormModel::companyNumber),
                    org.orgCompanyNumberStep,
                )
        }

        return rows
    }

    private fun charityNumberRow(
        state: LandlordRegistrationState,
        regulator: CharityRegulator,
    ): SummaryListRowViewModel? {
        val org = state.orgLandlordRegistrationTask
        val headingKey = "registerAsALandlord.orgCheckAnswers.landlordDetails.charityNumber"
        return when (regulator) {
            CharityRegulator.ENGLAND_AND_WALES ->
                orgRow(
                    state,
                    headingKey,
                    org.orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(OrgCharityNumberEnglandAndWalesFormModel::charityNumber),
                    org.orgCharityNumberEnglandAndWalesStep,
                )

            CharityRegulator.NORTHERN_IRELAND ->
                orgRow(
                    state,
                    headingKey,
                    org.orgCharityNumberNorthernIrelandStep.formModel.notNullValue(OrgCharityNumberNorthernIrelandFormModel::charityNumber),
                    org.orgCharityNumberNorthernIrelandStep,
                )

            CharityRegulator.SCOTLAND ->
                orgRow(
                    state,
                    headingKey,
                    org.orgCharityNumberScotlandStep.formModel.notNullValue(OrgCharityNumberScotlandFormModel::charityNumber),
                    org.orgCharityNumberScotlandStep,
                )

            CharityRegulator.NONE -> null
        }
    }

    private fun getLeadTrusteeCard(state: LandlordRegistrationState): SummaryCardViewModel {
        val org = state.orgLandlordRegistrationTask
        val rows =
            listOf(
                orgCardRow(
                    "registerAsALandlord.orgCheckAnswers.governingBody.name",
                    org.leadTrusteeNameStep.formModel.notNullValue(LeadTrusteeNameFormModel::name),
                ),
                orgCardRow(
                    "registerAsALandlord.orgCheckAnswers.governingBody.dateOfBirth",
                    org.leadTrusteeDobStep.formModel.toLocalDateOrNull(),
                ),
                orgCardRow(
                    "registerAsALandlord.orgCheckAnswers.governingBody.email",
                    org.leadTrusteeEmailStep.formModel.notNullValue(LeadTrusteeEmailFormModel::emailAddress),
                ),
                orgCardRow(
                    "registerAsALandlord.orgCheckAnswers.governingBody.phoneNumber",
                    org.leadTrusteePhoneStep.formModel.notNullValue(LeadTrusteePhoneFormModel::phoneNumber),
                ),
                orgCardRow(
                    "registerAsALandlord.orgCheckAnswers.governingBody.address",
                    org.trusteeAddressTask.getAddress().toMultiLineAddress().split("\n"),
                ),
            )
        // TODO: PDJB-1289 - dummy governing body member cards are rendered separately; replace with real member enumeration.
        return SummaryCardViewModel(
            title = "registerAsALandlord.orgCheckAnswers.governingBody.leadTrusteeCardTitle",
            summaryList = rows,
            actions = orgCardChangeAction(state, org.leadTrusteeNameStep),
        )
    }

    // TODO: PDJB-1289 - replace these dummy governing body member cards with real member enumeration once it exists.
    private fun getGovBodyMemberCards(): List<SummaryCardViewModel> =
        listOf(
            dummyMemberCard("1. Director", "Director", "Indiana jones", "18 March 1874"),
            dummyMemberCard("2. Partner", "Partner", "George Goof", "8 March 2001"),
        )

    private fun dummyMemberCard(
        numberedTitle: String,
        role: String,
        name: String,
        dateOfBirth: String,
    ) = SummaryCardViewModel(
        title = "registerAsALandlord.orgCheckAnswers.governingBody.memberCardTitle",
        cardNumber = numberedTitle,
        summaryList =
            listOf(
                orgCardRow("registerAsALandlord.orgCheckAnswers.governingBody.role", role),
                orgCardRow("registerAsALandlord.orgCheckAnswers.governingBody.name", name),
                orgCardRow("registerAsALandlord.orgCheckAnswers.governingBody.dateOfBirth", dateOfBirth),
                orgCardRow("registerAsALandlord.orgCheckAnswers.governingBody.address", DUMMY_ADDRESS_LINES),
            ),
        actions = listOf(SummaryCardActionViewModel(text = "forms.links.change", url = DUMMY_CHANGE_URL)),
    )

    private fun getMainContactCard(state: LandlordRegistrationState): SummaryCardViewModel {
        val org = state.orgLandlordRegistrationTask
        val mainContact = org.orgMainContactStep.formModel
        val rows =
            listOf(
                orgCardRow("registerAsALandlord.orgCheckAnswers.mainContact.name", mainContact.notNullValue(OrgMainContactFormModel::name)),
                orgCardRow(
                    "registerAsALandlord.orgCheckAnswers.mainContact.email",
                    mainContact.notNullValue(OrgMainContactFormModel::emailAddress),
                ),
                orgCardRow(
                    "registerAsALandlord.orgCheckAnswers.mainContact.phoneNumber",
                    mainContact.notNullValue(OrgMainContactFormModel::phoneNumber),
                ),
            )
        return SummaryCardViewModel(
            title = "registerAsALandlord.orgCheckAnswers.mainContact.cardTitle",
            summaryList = rows,
            actions = orgCardChangeAction(state, org.orgMainContactStep),
        )
    }

    private fun orgRow(
        state: LandlordRegistrationState,
        headingKey: String,
        value: Any?,
        step: RequestableStep<*, *, *>,
    ) = SummaryListRowViewModel.forCheckYourAnswersPage(headingKey, value, orgChangeDestination(state, step))

    private fun orgCardRow(
        headingKey: String,
        value: Any?,
    ) = SummaryListRowViewModel.forCheckYourAnswersPage(headingKey, value, Destination.Nowhere())

    // TODO: PDJB-1172 - dummy row with a non-functional Change link; replace once the underlying step exists.
    private fun dummyRow(
        headingKey: String,
        value: Any?,
    ) = SummaryListRowViewModel(
        fieldHeading = headingKey,
        fieldValue = value,
        actions = listOf(SummaryListRowActionsViewModel("forms.links.change", DUMMY_CHANGE_URL)),
    )

    private fun orgChangeDestination(
        state: LandlordRegistrationState,
        step: RequestableStep<*, *, *>,
    ) = Destination.VisitableStep(step, state.getCyaJourneyId(step))

    private fun orgCardChangeAction(
        state: LandlordRegistrationState,
        step: RequestableStep<*, *, *>,
    ): List<SummaryCardActionViewModel> =
        listOfNotNull(
            orgChangeDestination(state, step).toUrlStringOrNull()?.let {
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
        // TODO: PDJB-1172 / PDJB-1289 - dummy placeholders for sections whose data is not yet collected.
        private const val DUMMY_CHANGE_URL = "#"
        private val DUMMY_ADDRESS_LINES = listOf("3rd Floor", "88 Kingsway Square", "London", "ZX1 4QP")
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
