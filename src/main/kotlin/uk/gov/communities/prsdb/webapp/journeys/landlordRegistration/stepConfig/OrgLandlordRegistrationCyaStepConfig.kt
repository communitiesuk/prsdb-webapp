package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordRegistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

@JourneyFrameworkComponent
class OrgLandlordRegistrationCyaStepConfig(
    private val landlordRegistrationService: LandlordRegistrationService,
    private val securityContextService: SecurityContextService,
) : AbstractCheckYourAnswersStepConfig<LandlordRegistrationState>() {
    override fun chooseTemplate(state: LandlordRegistrationState) = "forms/orgLandlordRegistrationCheckAnswersForm"

    override fun getStepSpecificContent(state: LandlordRegistrationState): Map<String, Any?> =
        mapOf(
            "title" to "registerAsALandlord.title",
            "submitButtonText" to "registerAsALandlord.orgCheckAnswers.submitButton",
            "yourDetailsCard" to getYourDetailsCard(state),
            "landlordDetails" to getLandlordDetailsRows(state),
            "governingBodyMemberCards" to (listOfNotNull(getLeadTrusteeCard(state)) + getGovBodyMemberCards(state)),
            "mainContactCard" to getMainContactCard(state),
        )

    override fun afterStepDataIsAdded(state: LandlordRegistrationState) {
        val org = state.orgLandlordRegistrationTask

        val organisationTypes = org.orgTypeStep.formModel.getSelectedOrgTypes()
        val isTrust = OrgType.TRUST in organisationTypes
        val isRegisteredCharity = org.orgCharityStep.formModel.notNullValue(OrgCharityFormModel::charity)
        val hasCompanyNumber = org.orgCompaniesHouseStep.formModel.notNullValue(OrgCompaniesHouseFormModel::companiesHouse)

        val charityRegulator = if (isRegisteredCharity) org.orgCharityRegisteredWithStep.formModel.charityRegisteredWith else null

        val mainContact = org.orgMainContactStep.formModel

        val governingBodyMembers =
            (org.governingBodyMembersMap ?: emptyMap())
                .values
                .toList()

        landlordRegistrationService.registerOrganisationLandlord(
            baseUserId = SecurityContextHolder.getContext().authentication.name,
            organisationTypes = organisationTypes,
            organisationHasCompanyNumber = hasCompanyNumber,
            orgIsRegisteredCharity = isRegisteredCharity,
            organisationName = org.orgNameStep.formModel.notNullValue(OrgNameFormModel::orgName),
            organisationAddress = getOrgAddress(org.orgAddressStep.formModel),
            organisationEmail = org.orgEmailStep.formModel.notNullValue(EmailFormModel::emailAddress),
            organisationPhoneNumber = org.orgPhoneNumberStep.formModel.notNullValue(OrgPhoneNumberFormModel::phoneNumber),
            organisationCompanyNumber =
                if (hasCompanyNumber) org.orgCompanyNumberStep.formModel.notNullValue(OrgCompanyNumberFormModel::companyNumber) else null,
            organisationCharityRegisteredWith = charityRegulator,
            organisationCharityNumber = getCharityNumber(state, charityRegulator),
            organisationLeadTrusteeName =
                if (isTrust) org.leadTrusteeNameStep.formModel.notNullValue(LeadTrusteeNameFormModel::name) else null,
            organisationLeadTrusteeDateOfBirth = if (isTrust) org.leadTrusteeDobStep.formModel.toLocalDateOrNull() else null,
            organisationLeadTrusteeEmail =
                if (isTrust) org.leadTrusteeEmailStep.formModel.notNullValue(LeadTrusteeEmailFormModel::emailAddress) else null,
            organisationLeadTrusteePhoneNumber =
                if (isTrust) org.leadTrusteePhoneStep.formModel.notNullValue(LeadTrusteePhoneFormModel::phoneNumber) else null,
            organisationLeadTrusteeAddress = if (isTrust) org.trusteeAddressTask.getAddress() else null,
            organisationMainContactName = mainContact.notNullValue(OrgMainContactFormModel::name),
            organisationMainContactEmail = mainContact.notNullValue(OrgMainContactFormModel::emailAddress),
            organisationMainContactPhoneNumber = mainContact.notNullValue(OrgMainContactFormModel::phoneNumber),
            organisationRegistrantName = state.identityTask.getName(),
            organisationRegistrantDateOfBirth = state.identityTask.getDateOfBirth(),
            // TODO: PDJB-1282 - replace with real registrant email and phone once those steps exist
            organisationRegistrantEmail = "",
            organisationRegistrantPhoneNumber = "",
            organisationGoverningBodyMembers = governingBodyMembers,
        )

        securityContextService.refreshContext()
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
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.yourDetails.email",
                    state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress),
                    Destination.VisitableStep(
                        state.emailStep,
                        state.getCyaJourneyId(state.emailStep),
                    ),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.yourDetails.phoneNumber",
                    state.phoneNumberStep.formModel.notNullValue(PhoneNumberFormModel::phoneNumber),
                    Destination.VisitableStep(
                        state.phoneNumberStep,
                        state.getCyaJourneyId(state.phoneNumberStep),
                    ),
                ),
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

            val isRegisteredCharity = org.orgCharityStep.formModel.notNullValue(OrgCharityFormModel::charity)
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.registeredCharity",
                    isRegisteredCharity,
                    Destination.VisitableStep(org.orgCharityStep, state.getCyaJourneyId(org.orgCharityStep)),
                ),
            )

            val charityRegulator = if (isRegisteredCharity) org.orgCharityRegisteredWithStep.formModel.charityRegisteredWith else null
            val showCharityRegulator = charityRegulator != null
            val showCharityNumber = charityRegulator != null && charityRegulator != CharityRegulator.NONE

            if (showCharityRegulator) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "registerAsALandlord.orgCheckAnswers.landlordDetails.charityCommission",
                        regulatorMessageKey(charityRegulator!!),
                        Destination.VisitableStep(
                            org.orgCharityRegisteredWithStep,
                            state.getCyaJourneyId(org.orgCharityRegisteredWithStep),
                        ),
                    ),
                )
            }
            if (showCharityNumber) {
                add(charityNumberRow(state, charityRegulator!!))
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
    ): SummaryListRowViewModel {
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

            CharityRegulator.NONE -> error("charityNumberRow should only be called for a regulator that issues a charity number")
        }
    }

    private fun getLeadTrusteeCard(state: LandlordRegistrationState): SummaryCardViewModel? {
        val org = state.orgLandlordRegistrationTask
        if (OrgType.TRUST !in org.orgTypeStep.formModel.getSelectedOrgTypes()) {
            return null
        }
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
            actions =
                SummaryCardActionViewModel.changeAction(
                    Destination.VisitableStep(org.leadTrusteeNameStep, state.getCyaJourneyId(org.leadTrusteeNameStep)),
                ),
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
                    // TODO: PDJB-1168 (PR2) - wire this to the real governing-body-member edit round-trip that returns to the CYA page.
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
            actions =
                SummaryCardActionViewModel.changeAction(
                    Destination.VisitableStep(org.orgMainContactStep, state.getCyaJourneyId(org.orgMainContactStep)),
                ),
        )
    }

    // TODO: PDJB-1133 - this only handles the manually-entered organisation address; handle looked-up (auto) address data once org address lookup exists.
    private fun getOrgAddress(address: ManualAddressFormModel) =
        AddressDataModel
            .fromManualAddressData(
                addressLineOne = address.notNullValue(ManualAddressFormModel::addressLineOne),
                addressLineTwo = address.addressLineTwo,
                townOrCity = address.notNullValue(ManualAddressFormModel::townOrCity),
                county = address.county,
                postcode = address.notNullValue(ManualAddressFormModel::postcode),
            )

    private fun orgAddressLines(address: ManualAddressFormModel) =
        getOrgAddress(address)
            .toMultiLineAddress()
            .split("\n")

    private fun getCharityNumber(
        state: LandlordRegistrationState,
        charityRegulator: CharityRegulator?,
    ): String? {
        val org = state.orgLandlordRegistrationTask
        return when (charityRegulator) {
            CharityRegulator.ENGLAND_AND_WALES ->
                org.orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(
                    OrgCharityNumberEnglandAndWalesFormModel::charityNumber,
                )

            CharityRegulator.NORTHERN_IRELAND ->
                org.orgCharityNumberNorthernIrelandStep.formModel.notNullValue(
                    OrgCharityNumberNorthernIrelandFormModel::charityNumber,
                )

            CharityRegulator.SCOTLAND ->
                org.orgCharityNumberScotlandStep.formModel.notNullValue(
                    OrgCharityNumberScotlandFormModel::charityNumber,
                )

            CharityRegulator.NONE, null -> null
        }
    }

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
        // TODO: PDJB-1168 - non-functional Change link placeholder for governing body member rows until edit flow exists.
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
