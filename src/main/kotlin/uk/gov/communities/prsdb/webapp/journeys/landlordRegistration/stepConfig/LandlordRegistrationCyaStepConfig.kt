package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

@JourneyFrameworkComponent
class LandlordRegistrationCyaStepConfig(
    private val landlordService: LandlordService,
    private val securityContextService: SecurityContextService,
    private val featureFlagManager: FeatureFlagManager,
) : AbstractCheckYourAnswersStepConfig<LandlordRegistrationState>() {
    override fun chooseTemplate(state: LandlordRegistrationState) =
        if (isOrganisation(state)) {
            "forms/orgLandlordRegistrationCheckAnswersForm"
        } else {
            "forms/checkAnswersForm"
        }

    override fun getStepSpecificContent(state: LandlordRegistrationState): Map<String, Any?> =
        if (isOrganisation(state)) {
            getOrgStepSpecificContent(state)
        } else {
            getIndividualStepSpecificContent(state)
        }

    override fun afterStepDataIsAdded(state: LandlordRegistrationState) {
        if (isOrganisation(state)) {
            // TODO: PDJB-1180 - persist the organisation landlord on submit.
            return
        }

        landlordService.createLandlord(
            baseUserId = SecurityContextHolder.getContext().authentication.name,
            name = state.identityTask.getName(),
            email =
                state.individualLandlordRegistrationTask.emailStep.formModel
                    .notNullValue(EmailFormModel::emailAddress),
            phoneNumber =
                state.individualLandlordRegistrationTask.phoneNumberStep.formModel.notNullValue(
                    PhoneNumberFormModel::phoneNumber,
                ),
            addressDataModel = state.individualLandlordRegistrationTask.addressTask.getAddress(),
            countryOfResidence = ENGLAND_OR_WALES,
            isVerified = state.identityTask.getIsIdentityVerified(),
            hasAcceptedPrivacyNotice = state.privacyNoticeStep.formModel.notNullValue(PrivacyNoticeFormModel::agreesToPrivacyNotice),
            nonEnglandOrWalesAddress = null,
            dateOfBirth = state.identityTask.getDateOfBirth(),
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

    private fun isOrganisation(state: LandlordRegistrationState) =
        featureFlagManager.checkFeature(ORGANISATION_LANDLORD_REGISTRATION) &&
            state.landlordTypeStep.formModelOrNull?.landlordType == LandlordType.ORGANISATION

    private fun getIndividualStepSpecificContent(state: LandlordRegistrationState): Map<String, Any?> =
        mapOf(
            "summaryName" to "registerAsALandlord.checkAnswers.summaryName",
            "showWarning" to true,
            "submitButtonText" to "forms.buttons.confirmAndContinue",
            "insetText" to false,
            "summaryListData" to getSummaryList(state),
        )

    private fun getSummaryList(state: LandlordRegistrationState) =
        getIdentityRows(state) +
            getEmailAndPhoneRows(state) +
            getAddressRows(state)

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
                state.individualLandlordRegistrationTask.emailStep.formModel
                    .notNullValue(EmailFormModel::emailAddress),
                Destination.VisitableStep(
                    state.individualLandlordRegistrationTask.emailStep,
                    state.getCyaJourneyId(state.individualLandlordRegistrationTask.emailStep),
                ),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.telephoneNumber",
                state.individualLandlordRegistrationTask.phoneNumberStep.formModel
                    .notNullValue(PhoneNumberFormModel::phoneNumber),
                Destination.VisitableStep(
                    state.individualLandlordRegistrationTask.phoneNumberStep,
                    state.getCyaJourneyId(state.individualLandlordRegistrationTask.phoneNumberStep),
                ),
            ),
        )

    private fun getAddressRows(state: LandlordRegistrationState): List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.englandOrWalesResident",
                state.individualLandlordRegistrationTask.countryOfResidenceStep.formModel.notNullValue(
                    CountryOfResidenceFormModel::livesInEnglandOrWales,
                ),
                Destination.VisitableStep(
                    state.individualLandlordRegistrationTask.countryOfResidenceStep,
                    state.getCyaJourneyId(state.individualLandlordRegistrationTask.countryOfResidenceStep),
                ),
            ),
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "registerAsALandlord.checkAnswers.rowHeading.contactAddress",
                state.individualLandlordRegistrationTask.addressTask
                    .getAddress()
                    .singleLineAddress,
                Destination.VisitableStep(
                    state.individualLandlordRegistrationTask.addressTask.lookupAddressStep,
                    state.getCyaJourneyId(state.individualLandlordRegistrationTask.addressTask.lookupAddressStep),
                ),
            ),
        )

    private fun getOrgStepSpecificContent(state: LandlordRegistrationState): Map<String, Any?> =
        mapOf(
            "title" to "registerAsALandlord.title",
            "submitButtonText" to "registerAsALandlord.orgCheckAnswers.submitButton",
            "yourDetailsCard" to getYourDetailsCard(state),
            "landlordDetails" to getLandlordDetailsRows(state),
            "leadTrusteeCard" to getLeadTrusteeCard(state),
            "mainContactCard" to getMainContactCard(state),
        )

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
                // TODO: PDJB-1172 - add email and phone rows once the user's own contact details are collected.
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
                orgAddressSingleLine(org.orgAddressStep.formModel),
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
                    org.trusteeAddressTask.getAddress().singleLineAddress,
                ),
            )
        // TODO: PDJB-1289 - render an additional summary card per governing body member once member enumeration exists.
        return SummaryCardViewModel(
            title = "registerAsALandlord.orgCheckAnswers.governingBody.leadTrusteeCardTitle",
            summaryList = rows,
            actions = orgCardChangeAction(state, org.leadTrusteeNameStep),
        )
    }

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

    private fun orgAddressSingleLine(address: ManualAddressFormModel) =
        AddressDataModel
            .fromManualAddressData(
                addressLineOne = address.notNullValue(ManualAddressFormModel::addressLineOne),
                addressLineTwo = address.addressLineTwo,
                townOrCity = address.notNullValue(ManualAddressFormModel::townOrCity),
                county = address.county,
                postcode = address.notNullValue(ManualAddressFormModel::postcode),
            ).singleLineAddress

    private fun orgTypeMessageKey(orgTypeName: String) =
        when (orgTypeName) {
            OrgType.COMPANY.name -> "registerAsALandlord.orgType.checkbox.company"
            OrgType.CHARITY.name -> "registerAsALandlord.orgType.checkbox.charity"
            OrgType.TRUST.name -> "registerAsALandlord.orgType.checkbox.trust"
            else -> "registerAsALandlord.orgType.checkbox.none"
        }

    private fun regulatorMessageKey(regulator: CharityRegulator) =
        when (regulator) {
            CharityRegulator.ENGLAND_AND_WALES -> "forms.orgCharityRegisteredWith.radios.option.englandAndWales"
            CharityRegulator.NORTHERN_IRELAND -> "forms.orgCharityRegisteredWith.radios.option.northernIreland"
            CharityRegulator.SCOTLAND -> "forms.orgCharityRegisteredWith.radios.option.scotland"
            CharityRegulator.NONE -> "forms.orgCharityRegisteredWith.radios.option.none"
        }
}

@JourneyFrameworkComponent
final class LandlordRegistrationCyaStep(
    stepConfig: LandlordRegistrationCyaStepConfig,
) : AbstractCheckYourAnswersStep<LandlordRegistrationState>(stepConfig)
