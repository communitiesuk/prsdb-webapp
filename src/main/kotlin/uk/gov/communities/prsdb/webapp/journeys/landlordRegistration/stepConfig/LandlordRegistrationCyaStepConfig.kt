package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberEnglandAndWalesFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberNorthernIrelandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberScotlandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompanyNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCharityFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCompanyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgMainContactFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgPhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordRegistrationService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

@JourneyFrameworkComponent
class LandlordRegistrationCyaStepConfig(
    private val landlordRegistrationService: LandlordRegistrationService,
    private val securityContextService: SecurityContextService,
    private val featureFlagManager: FeatureFlagManager,
) : AbstractCheckYourAnswersStepConfig<LandlordRegistrationState>() {
    override fun chooseTemplate(state: LandlordRegistrationState) =
        if (isOrgLandlord(state)) {
            "forms/orgLandlordRegistrationCheckAnswersForm"
        } else {
            "forms/checkAnswersForm"
        }

    override fun getStepSpecificContent(state: LandlordRegistrationState): Map<String, Any?> =
        if (isOrgLandlord(state)) {
            getOrgStepContent(state)
        } else {
            getIndividualStepContent(state)
        }

    override fun afterStepDataIsAdded(state: LandlordRegistrationState) {
        if (isOrgLandlord(state)) {
            val org = state.orgLandlordRegistrationTask

            val organisationTypes = org.orgTypeStep.formModel.getSelectedOrgTypes()
            val isTrust = OrgType.TRUST in organisationTypes
            val isRegisteredCharity =
                org.charityTask.orgIsRegisteredCharityStep.formModel.notNullValue(
                    OrgIsRegisteredCharityFormModel::charity,
                )
            val hasCompanyNumber =
                org.companiesHouseTask.orgIsRegisteredCompanyStep.formModel.notNullValue(
                    OrgIsRegisteredCompanyFormModel::companiesHouse,
                )

            val charityRegulator =
                if (isRegisteredCharity) org.charityTask.orgCharityRegisteredWithStep.formModel.charityRegisteredWith else null

            val mainContact = org.orgMainContactStep.formModel

            val governingBodyMembers =
                (org.orgGovBodyTask.governingBodyMembersMap ?: emptyMap())
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
                    if (hasCompanyNumber) {
                        org.companiesHouseTask.orgCompanyNumberStep.formModel.notNullValue(
                            OrgCompanyNumberFormModel::companyNumber,
                        )
                    } else {
                        null
                    },
                organisationCharityRegisteredWith = charityRegulator,
                organisationCharityNumber = getCharityNumber(state, charityRegulator),
                organisationLeadTrusteeName =
                    if (isTrust) org.leadTrusteeTask.leadTrusteeNameStep.formModel.notNullValue(LeadTrusteeNameFormModel::name) else null,
                organisationLeadTrusteeDateOfBirth =
                    if (isTrust) org.leadTrusteeTask.leadTrusteeDobStep.formModel.toLocalDateOrNull() else null,
                organisationLeadTrusteeEmail =
                    if (isTrust) {
                        org.leadTrusteeTask.leadTrusteeEmailStep.formModel.notNullValue(
                            LeadTrusteeEmailFormModel::emailAddress,
                        )
                    } else {
                        null
                    },
                organisationLeadTrusteePhoneNumber =
                    if (isTrust) {
                        org.leadTrusteeTask.leadTrusteePhoneStep.formModel.notNullValue(
                            LeadTrusteePhoneFormModel::phoneNumber,
                        )
                    } else {
                        null
                    },
                organisationLeadTrusteeAddress = if (isTrust) org.leadTrusteeTask.trusteeAddressTask.getAddress() else null,
                organisationMainContactName = mainContact.notNullValue(OrgMainContactFormModel::name),
                organisationMainContactEmail = mainContact.notNullValue(OrgMainContactFormModel::emailAddress),
                organisationMainContactPhoneNumber = mainContact.notNullValue(OrgMainContactFormModel::phoneNumber),
                organisationRegistrantName = state.identityTask.getName(),
                organisationRegistrantDateOfBirth = state.identityTask.getDateOfBirth(),
                organisationRegistrantEmail = state.emailStep.formModel.notNullValue(EmailFormModel::emailAddress),
                organisationRegistrantPhoneNumber = state.phoneNumberStep.formModel.notNullValue(PhoneNumberFormModel::phoneNumber),
                organisationGoverningBodyMembers = governingBodyMembers,
            )
        } else {
            landlordRegistrationService.registerIndividualLandlord(
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                name = state.identityTask.getName(),
                email =
                    state.emailStep.formModel
                        .notNullValue(EmailFormModel::emailAddress),
                phoneNumber =
                    state.phoneNumberStep.formModel.notNullValue(
                        PhoneNumberFormModel::phoneNumber,
                    ),
                address = state.individualLandlordLocationTask.addressTask.getAddress(),
                countryOfResidence = ENGLAND_OR_WALES,
                isVerified = state.identityTask.getIsIdentityVerified(),
                hasAcceptedPrivacyNotice = state.privacyNoticeStep.formModel.notNullValue(PrivacyNoticeFormModel::agreesToPrivacyNotice),
                nonEnglandOrWalesAddress = null,
                dateOfBirth = state.identityTask.getDateOfBirth(),
            )
        }

        securityContextService.refreshContext()
    }

    // Overrides AbstractCheckYourAnswersStepConfig, which deleted the journey
    // We don't want to delete the journey at this stage when this page is included within another journey,
    // such as accepting a joint landlord invitation
    override fun resolveNextDestination(
        state: LandlordRegistrationState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    private fun isOrgLandlord(state: LandlordRegistrationState) =
        featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION) &&
            state.landlordTypeStep.outcome == LandlordTypeMode.ORGANISATION

    // Individual landlord content

    private fun getIndividualStepContent(state: LandlordRegistrationState): Map<String, Any?> =
        mapOf(
            "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "insetText" to false,
            "summaryListData" to getSummaryList(state),
        )

    private fun getSummaryList(state: LandlordRegistrationState) =
        getIdentityRows(state) +
            getLandlordTypeRows(state) +
            getEmailAndPhoneRows(state) +
            getAddressRows(state)

    private fun getLandlordTypeRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> =
        if (featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION) &&
            state.landlordTypeStep.outcome == LandlordTypeMode.INDIVIDUAL
        ) {
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.checkAnswers.rowHeading.landlordType",
                    "registerAsALandlord.checkAnswers.landlordTypeValue",
                    Destination.VisitableStep(state.landlordTypeStep, state.getCyaJourneyId(state.landlordTypeStep)),
                ),
            )
        } else {
            emptyList()
        }

    private fun getIdentityRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> {
        val isIdentityVerified = state.identityTask.getIsIdentityVerified()
        return listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.name",
                state.identityTask.getName(),
                if (isIdentityVerified) {
                    Destination.Nowhere()
                } else {
                    Destination.VisitableStep(state.identityTask.nameStep, state.getCyaJourneyId(state.identityTask.nameStep))
                },
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.dateOfBirth",
                state.identityTask.getDateOfBirth(),
                if (isIdentityVerified) {
                    Destination.Nowhere()
                } else {
                    Destination.VisitableStep(
                        state.identityTask.dateOfBirthStep,
                        state.getCyaJourneyId(state.identityTask.dateOfBirthStep),
                    )
                },
            ),
        )
    }

    private fun getEmailAndPhoneRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.email",
                state.emailStep.formModel
                    .notNullValue(EmailFormModel::emailAddress),
                Destination.VisitableStep(
                    state.emailStep,
                    state.getCyaJourneyId(state.emailStep),
                ),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                state.phoneNumberStep.formModel
                    .notNullValue(PhoneNumberFormModel::phoneNumber),
                Destination.VisitableStep(
                    state.phoneNumberStep,
                    state.getCyaJourneyId(state.phoneNumberStep),
                ),
            ),
        )

    private fun getAddressRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                state.individualLandlordLocationTask.countryOfResidenceStep.formModel.notNullValue(
                    CountryOfResidenceFormModel::livesInEnglandOrWales,
                ),
                Destination.VisitableStep(
                    state.individualLandlordLocationTask.countryOfResidenceStep,
                    state.getCyaJourneyId(state.individualLandlordLocationTask.countryOfResidenceStep),
                ),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                state.individualLandlordLocationTask.addressTask
                    .getAddress()
                    .singleLineAddress,
                Destination.VisitableStep(
                    state.individualLandlordLocationTask.addressTask.lookupAddressStep,
                    state.getCyaJourneyId(state.individualLandlordLocationTask.addressTask.lookupAddressStep),
                ),
            ),
        )

    // Organisation landlord content

    private fun getOrgStepContent(state: LandlordRegistrationState): Map<String, Any?> =
        mapOf(
            "title" to "registerAsALandlord.title",
            "submitButtonText" to "registerAsALandlord.orgCheckAnswers.submitButton",
            "yourDetailsCard" to getYourDetailsCard(state),
            "landlordDetails" to getLandlordDetailsRows(state),
            "governingBodyMemberCards" to (listOfNotNull(getLeadTrusteeCard(state)) + getGovBodyMemberCards(state)),
            "mainContactCard" to getMainContactCard(state),
        )

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

            val isRegisteredCharity =
                org.charityTask.orgIsRegisteredCharityStep.formModel.notNullValue(
                    OrgIsRegisteredCharityFormModel::charity,
                )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.registeredCharity",
                    isRegisteredCharity,
                    Destination.VisitableStep(
                        org.charityTask.orgIsRegisteredCharityStep,
                        state.getCyaJourneyId(org.charityTask.orgIsRegisteredCharityStep),
                    ),
                ),
            )

            val charityRegulator =
                if (isRegisteredCharity) {
                    org.charityTask.orgCharityRegisteredWithStep.formModel.charityRegisteredWith
                } else {
                    null
                }
            val showCharityRegulator = charityRegulator != null
            val showCharityNumber = charityRegulator != null && charityRegulator != CharityRegulator.NONE

            if (showCharityRegulator) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "registerAsALandlord.orgCheckAnswers.landlordDetails.charityCommission",
                        regulatorMessageKey(charityRegulator),
                        Destination.VisitableStep(
                            org.charityTask.orgCharityRegisteredWithStep,
                            state.getCyaJourneyId(org.charityTask.orgCharityRegisteredWithStep),
                        ),
                    ),
                )
            }
            if (showCharityNumber) {
                add(charityNumberRow(state, charityRegulator))
            }

            val registeredWithCompaniesHouse =
                org.companiesHouseTask.orgIsRegisteredCompanyStep.formModel.notNullValue(
                    OrgIsRegisteredCompanyFormModel::companiesHouse,
                )
            add(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.landlordDetails.registeredWithCompaniesHouse",
                    registeredWithCompaniesHouse,
                    Destination.VisitableStep(
                        org.companiesHouseTask.orgIsRegisteredCompanyStep,
                        state.getCyaJourneyId(org.companiesHouseTask.orgIsRegisteredCompanyStep),
                    ),
                ),
            )
            if (registeredWithCompaniesHouse) {
                add(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "registerAsALandlord.orgCheckAnswers.landlordDetails.companiesHouseNumber",
                        org.companiesHouseTask.orgCompanyNumberStep.formModel.notNullValue(OrgCompanyNumberFormModel::companyNumber),
                        Destination.VisitableStep(
                            org.companiesHouseTask.orgCompanyNumberStep,
                            state.getCyaJourneyId(org.companiesHouseTask.orgCompanyNumberStep),
                        ),
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
                    org.charityTask.orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(
                        OrgCharityNumberEnglandAndWalesFormModel::charityNumber,
                    ),
                    Destination.VisitableStep(
                        org.charityTask.orgCharityNumberEnglandAndWalesStep,
                        state.getCyaJourneyId(org.charityTask.orgCharityNumberEnglandAndWalesStep),
                    ),
                )

            CharityRegulator.NORTHERN_IRELAND ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    org.charityTask.orgCharityNumberNorthernIrelandStep.formModel.notNullValue(
                        OrgCharityNumberNorthernIrelandFormModel::charityNumber,
                    ),
                    Destination.VisitableStep(
                        org.charityTask.orgCharityNumberNorthernIrelandStep,
                        state.getCyaJourneyId(org.charityTask.orgCharityNumberNorthernIrelandStep),
                    ),
                )

            CharityRegulator.SCOTLAND ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    headingKey,
                    org.charityTask.orgCharityNumberScotlandStep.formModel.notNullValue(OrgCharityNumberScotlandFormModel::charityNumber),
                    Destination.VisitableStep(
                        org.charityTask.orgCharityNumberScotlandStep,
                        state.getCyaJourneyId(org.charityTask.orgCharityNumberScotlandStep),
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
                    org.leadTrusteeTask.leadTrusteeNameStep.formModel.notNullValue(LeadTrusteeNameFormModel::name),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.dateOfBirth",
                    org.leadTrusteeTask.leadTrusteeDobStep.formModel.toLocalDateOrNull(),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.email",
                    org.leadTrusteeTask.leadTrusteeEmailStep.formModel.notNullValue(LeadTrusteeEmailFormModel::emailAddress),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.phoneNumber",
                    org.leadTrusteeTask.leadTrusteePhoneStep.formModel.notNullValue(LeadTrusteePhoneFormModel::phoneNumber),
                    Destination.Nowhere(),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "registerAsALandlord.orgCheckAnswers.governingBody.address",
                    org.leadTrusteeTask.trusteeAddressTask.getAddress().toMultiLineAddress().split("\n"),
                    Destination.Nowhere(),
                ),
            )
        return SummaryCardViewModel(
            title = "registerAsALandlord.orgCheckAnswers.governingBody.leadTrusteeCardTitle",
            summaryList = rows,
            actions =
                SummaryCardActionViewModel.changeAction(
                    Destination.VisitableStep(
                        org.leadTrusteeTask.leadTrusteeNameStep,
                        state.getCyaJourneyId(org.leadTrusteeTask.leadTrusteeNameStep),
                    ),
                ),
        )
    }

    private fun getGovBodyMemberCards(state: LandlordRegistrationState): List<SummaryCardViewModel> {
        val members = state.orgLandlordRegistrationTask.orgGovBodyTask.governingBodyMembersMap ?: emptyMap()
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
                    actions =
                        SummaryCardActionViewModel.changeAction(
                            Destination.VisitableStep(
                                state.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMemberListStep,
                                state.getCyaJourneyId(
                                    state.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMemberListStep,
                                ),
                            ),
                        ),
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
                org.charityTask.orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(
                    OrgCharityNumberEnglandAndWalesFormModel::charityNumber,
                )

            CharityRegulator.NORTHERN_IRELAND ->
                org.charityTask.orgCharityNumberNorthernIrelandStep.formModel.notNullValue(
                    OrgCharityNumberNorthernIrelandFormModel::charityNumber,
                )

            CharityRegulator.SCOTLAND ->
                org.charityTask.orgCharityNumberScotlandStep.formModel.notNullValue(
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
}

@JourneyFrameworkComponent
final class LandlordRegistrationCyaStep(
    stepConfig: LandlordRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<LandlordRegistrationState>(stepConfig)
