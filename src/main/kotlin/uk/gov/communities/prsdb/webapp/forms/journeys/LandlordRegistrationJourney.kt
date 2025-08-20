package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_PLACE_NAMES
import uk.gov.communities.prsdb.webapp.constants.NON_ENGLAND_OR_WALES_ADDRESS_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordPrivacyNoticeController.Companion.LANDLORD_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.ConfirmIdentityPage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordRegistrationCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.pages.VerifyIdentityPage
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.LookupAddressStep
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NonEnglandOrWalesAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PrivacyNoticeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService

class LandlordRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    val addressLookupService: AddressLookupService,
    val landlordService: LandlordService,
    val securityContextService: SecurityContextService,
) : Journey<LandlordRegistrationStepId>(
        journeyType = JourneyType.LANDLORD_REGISTRATION,
        initialStepId = LandlordRegistrationStepId.PrivacyNotice,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    protected class LandlordRegistrationStepRouter(
        journey: Iterable<StepDetails<LandlordRegistrationStepId>>,
    ) : GroupedStepRouter<LandlordRegistrationStepId>(journey) {
        override fun isDestinationAllowedWhenCheckingAnswersFor(
            destinationStep: LandlordRegistrationStepId?,
            stepBeingChecked: LandlordRegistrationStepId?,
        ): Boolean =
            when (stepBeingChecked) {
                LandlordRegistrationStepId.NonEnglandOrWalesAddress ->
                    destinationStep ==
                        LandlordRegistrationStepId.NonEnglandOrWalesAddress
                else -> super.isDestinationAllowedWhenCheckingAnswersFor(destinationStep, stepBeingChecked)
            }
    }

    override val stepRouter = LandlordRegistrationStepRouter(this)

    override val checkYourAnswersStepId = LandlordRegistrationStepId.CheckAnswers

    override val sections =
        listOf(
            JourneySection.withOneTask(
                JourneyTask.withOneStep(privacyNoticeStep()),
                "registerAsALandlord.section.privacyNotice.heading",
                LandlordRegistrationStepId.PrivacyNotice.urlPathSegment,
            ),
            JourneySection(
                registerDetailsTasks(),
                "registerAsALandlord.section.yourDetails.heading",
                "your-details",
            ),
            JourneySection(
                checkAndSubmitDetailsTasks(),
                "registerAsALandlord.section.checkAndSubmit.heading",
                "check-and-submit",
            ),
        )

    private fun registerDetailsTasks(): List<JourneyTask<LandlordRegistrationStepId>> =
        listOf(
            identityTask(),
            JourneyTask.withOneStep(emailStep()),
            JourneyTask.withOneStep(phoneNumberStep()),
            JourneyTask.withOneStep(countryOfResidenceStep()),
            landlordAddressesTask(),
        )

    private fun checkAndSubmitDetailsTasks(): List<JourneyTask<LandlordRegistrationStepId>> =
        listOf(
            JourneyTask.withOneStep(checkAnswersStep()),
            JourneyTask.withOneStep(declarationStep()),
        )

    private fun identityTask() =
        JourneyTask(
            LandlordRegistrationStepId.VerifyIdentity,
            setOf(
                verifyIdentityStep(),
                identityNotVerifiedStep(),
                nameStep(),
                dateOfBirthStep(),
                confirmIdentityStep(),
            ),
        )

    private fun landlordAddressesTask() =
        JourneyTask(
            LandlordRegistrationStepId.LookupAddress,
            setOf(
                lookupAddressStep(),
                noAddressFoundStep(),
                selectAddressStep(),
                manualAddressStep(),
                nonEnglandOrWalesAddressStep(),
                lookupContactAddressStep(),
                noContactAddressFoundStep(),
                selectContactAddressStep(),
                manualContactAddressStep(),
            ),
        )

    private fun privacyNoticeStep() =
        Step(
            id = LandlordRegistrationStepId.PrivacyNotice,
            page =
                Page(
                    formModel = PrivacyNoticeFormModel::class,
                    templateName = "forms/landlordPrivacyNoticeForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "registerAsALandlord.privacyNotice.fieldSetHeading",
                            "submitButtonText" to "forms.buttons.continue",
                            "landlordPrivacyNoticeUrl" to LANDLORD_PRIVACY_NOTICE_ROUTE,
                            "options" to
                                listOf(
                                    CheckboxViewModel(
                                        value = "true",
                                        labelMsgKey = "registerAsALandlord.privacyNotice.checkBox.label",
                                    ),
                                ),
                            BACK_URL_ATTR_NAME to RegisterLandlordController.LANDLORD_REGISTRATION_START_PAGE_ROUTE,
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.VerifyIdentity, null) },
            saveAfterSubmit = false,
        )

    private fun verifyIdentityStep() =
        Step(
            id = LandlordRegistrationStepId.VerifyIdentity,
            page = VerifyIdentityPage(),
            nextAction = { filteredJourneyData, _ -> verifyIdentityNextAction(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    private fun identityNotVerifiedStep() =
        Step(
            id = LandlordRegistrationStepId.IdentityNotVerified,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/identityNotVerifiedForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.identityNotVerified.fieldSetHeading",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to RegisterLandlordController.LANDLORD_REGISTRATION_PRIVACY_NOTICE_ROUTE,
                        ),
                    shouldDisplaySectionHeader = false,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Name, null) },
            saveAfterSubmit = false,
        )

    private fun nameStep() =
        Step(
            id = LandlordRegistrationStepId.Name,
            page =
                Page(
                    formModel = NameFormModel::class,
                    templateName = "forms/nameForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.name.fieldSetHeading",
                            "fieldSetHint" to "forms.name.fieldSetHint",
                            "label" to "forms.name.label",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.DateOfBirth, null) },
            saveAfterSubmit = false,
        )

    private fun dateOfBirthStep() =
        Step(
            id = LandlordRegistrationStepId.DateOfBirth,
            page =
                Page(
                    formModel = DateOfBirthFormModel::class,
                    templateName = "forms/dateForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.dateOfBirth.fieldSetHeading",
                            "fieldSetHint" to "forms.dateOfBirth.fieldSetHint",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Email, null) },
            saveAfterSubmit = false,
        )

    private fun confirmIdentityStep() =
        Step(
            id = LandlordRegistrationStepId.ConfirmIdentity,
            page =
                ConfirmIdentityPage(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/confirmIdentityForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.confirmDetails.heading",
                            "fieldSetHint" to "forms.confirmDetails.summary",
                            "submitButtonText" to "forms.buttons.confirmAndContinue",
                            BACK_URL_ATTR_NAME to RegisterLandlordController.LANDLORD_REGISTRATION_PRIVACY_NOTICE_ROUTE,
                        ),
                    displaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Email, null) },
            saveAfterSubmit = false,
        )

    private fun emailStep() =
        Step(
            id = LandlordRegistrationStepId.Email,
            page =
                Page(
                    formModel = EmailFormModel::class,
                    templateName = "forms/emailForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.email.fieldSetHeading",
                            "fieldSetHint" to "forms.email.fieldSetHint",
                            "label" to "forms.email.label",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.PhoneNumber, null) },
            saveAfterSubmit = false,
        )

    private fun phoneNumberStep() =
        Step(
            id = LandlordRegistrationStepId.PhoneNumber,
            page =
                Page(
                    formModel = PhoneNumberFormModel::class,
                    templateName = "forms/phoneNumberForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.phoneNumber.fieldSetHeading",
                            "fieldSetHint" to "forms.phoneNumber.fieldSetHint",
                            "label" to "forms.phoneNumber.label",
                            "submitButtonText" to "forms.buttons.continue",
                            "hint" to "forms.phoneNumber.hint",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.CountryOfResidence, null) },
            saveAfterSubmit = false,
        )

    private fun countryOfResidenceStep() =
        Step(
            id = LandlordRegistrationStepId.CountryOfResidence,
            page =
                Page(
                    formModel = CountryOfResidenceFormModel::class,
                    templateName = "forms/countryOfResidenceForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.countryOfResidence.fieldSetHeading",
                            "selectOptions" to INTERNATIONAL_PLACE_NAMES.map { SelectViewModel(it.name) },
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = true,
                                        valueStr = "yes",
                                        labelMsgKey = "forms.radios.option.yes.label",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        valueStr = "no",
                                        labelMsgKey = "forms.radios.option.no.label",
                                        conditionalFragment = "countryOfResidenceSelect",
                                    ),
                                ),
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { filteredJourneyData, _ -> countryOfResidenceNextAction(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    private fun lookupAddressStep() =
        LookupAddressStep(
            id = LandlordRegistrationStepId.LookupAddress,
            page =
                Page(
                    formModel = LookupAddressFormModel::class,
                    templateName = "forms/lookupAddressForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.lookupAddress.landlordRegistration.fieldSetHeading",
                            "fieldSetHint" to "forms.lookupAddress.fieldSetHint",
                            "postcodeLabel" to "forms.lookupAddress.postcode.label",
                            "postcodeHint" to "forms.lookupAddress.postcode.hint",
                            "houseNameOrNumberLabel" to "forms.lookupAddress.houseNameOrNumber.label",
                            "houseNameOrNumberHint" to "forms.lookupAddress.houseNameOrNumber.hint",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextStepIfAddressesFound = LandlordRegistrationStepId.SelectAddress,
            nextStepIfNoAddressesFound = LandlordRegistrationStepId.NoAddressFound,
            addressLookupService = addressLookupService,
            journeyDataService = journeyDataService,
            saveAfterSubmit = false,
        )

    private fun noAddressFoundStep() =
        Step(
            id = LandlordRegistrationStepId.NoAddressFound,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/noAddressFoundForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "postcode" to getHouseNameOrNumberAndPostcode(LandlordRegistrationStepId.LookupAddress).second,
                            "houseNameOrNumber" to getHouseNameOrNumberAndPostcode(LandlordRegistrationStepId.LookupAddress).first,
                            "searchAgainUrl" to LandlordRegistrationStepId.LookupAddress.urlPathSegment,
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.ManualAddress, null) },
        )

    private fun getHouseNameOrNumberAndPostcode(lookupAddressStepId: LandlordRegistrationStepId) =
        JourneyDataHelper
            .getLookupAddressHouseNameOrNumberAndPostcode(
                journeyDataService.getJourneyDataFromSession(),
                lookupAddressStepId.urlPathSegment,
            ) ?: Pair("", "")

    private fun selectAddressStep() =
        Step(
            id = LandlordRegistrationStepId.SelectAddress,
            page =
                SelectAddressPage(
                    formModel = SelectAddressFormModel::class,
                    templateName = "forms/selectAddressForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.selectAddress.fieldSetHeading",
                            "submitButtonText" to "forms.buttons.useThisAddress",
                            "searchAgainUrl" to
                                "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/" +
                                LandlordRegistrationStepId.LookupAddress.urlPathSegment,
                        ),
                    lookupAddressPathSegment = LandlordRegistrationStepId.LookupAddress.urlPathSegment,
                    journeyDataService = journeyDataService,
                    displaySectionHeader = true,
                ),
            nextAction = { filteredJourneyData, _ -> selectAddressNextAction(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    private fun manualAddressStep() =
        Step(
            id = LandlordRegistrationStepId.ManualAddress,
            page =
                Page(
                    formModel = ManualAddressFormModel::class,
                    templateName = "forms/manualAddressForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.manualAddress.landlordRegistration.fieldSetHeading",
                            "fieldSetHint" to "forms.manualAddress.fieldSetHint",
                            "addressLineOneLabel" to "forms.manualAddress.addressLineOne.label",
                            "addressLineTwoLabel" to "forms.manualAddress.addressLineTwo.label",
                            "townOrCityLabel" to "forms.manualAddress.townOrCity.label",
                            "countyLabel" to "forms.manualAddress.county.label",
                            "postcodeLabel" to "forms.lookupAddress.postcode.label",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.CheckAnswers, null) },
            saveAfterSubmit = false,
        )

    private fun nonEnglandOrWalesAddressStep() =
        Step(
            id = LandlordRegistrationStepId.NonEnglandOrWalesAddress,
            page =
                Page(
                    formModel = NonEnglandOrWalesAddressFormModel::class,
                    templateName = "forms/nonEnglandOrWalesAddressForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.nonEnglandOrWalesAddress.fieldSetHeading",
                            "fieldSetHint" to "forms.nonEnglandOrWalesAddress.fieldSetHint",
                            "label" to "forms.nonEnglandOrWalesAddress.label",
                            "limit" to NON_ENGLAND_OR_WALES_ADDRESS_MAX_LENGTH,
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.LookupContactAddress, null) },
            saveAfterSubmit = false,
        )

    private fun lookupContactAddressStep() =
        LookupAddressStep(
            id = LandlordRegistrationStepId.LookupContactAddress,
            page =
                Page(
                    formModel = LookupAddressFormModel::class,
                    templateName = "forms/lookupAddressForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.lookupContactAddress.fieldSetHeading",
                            "postcodeLabel" to "forms.lookupAddress.postcode.label",
                            "postcodeHint" to "forms.lookupAddress.postcode.hint",
                            "houseNameOrNumberLabel" to "forms.lookupAddress.houseNameOrNumber.label",
                            "houseNameOrNumberHint" to "forms.lookupAddress.houseNameOrNumber.hint",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextStepIfAddressesFound = LandlordRegistrationStepId.SelectContactAddress,
            nextStepIfNoAddressesFound = LandlordRegistrationStepId.NoContactAddressFound,
            addressLookupService = addressLookupService,
            journeyDataService = journeyDataService,
            saveAfterSubmit = false,
        )

    private fun noContactAddressFoundStep() =
        Step(
            id = LandlordRegistrationStepId.NoContactAddressFound,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/noAddressFoundForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "postcode" to getHouseNameOrNumberAndPostcode(LandlordRegistrationStepId.LookupContactAddress).second,
                            "houseNameOrNumber" to getHouseNameOrNumberAndPostcode(LandlordRegistrationStepId.LookupContactAddress).first,
                            "searchAgainUrl" to
                                "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/" +
                                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment,
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.ManualContactAddress, null) },
        )

    private fun selectContactAddressStep() =
        Step(
            id = LandlordRegistrationStepId.SelectContactAddress,
            page =
                SelectAddressPage(
                    formModel = SelectAddressFormModel::class,
                    templateName = "forms/selectAddressForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.selectAddress.fieldSetHeading",
                            "submitButtonText" to "forms.buttons.continue",
                            "searchAgainUrl" to
                                "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/" +
                                LandlordRegistrationStepId.LookupContactAddress.urlPathSegment,
                        ),
                    lookupAddressPathSegment = LandlordRegistrationStepId.LookupContactAddress.urlPathSegment,
                    journeyDataService = journeyDataService,
                    displaySectionHeader = true,
                ),
            nextAction = { filteredJourneyData, _ -> selectContactAddressNextAction(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    private fun manualContactAddressStep() =
        Step(
            id = LandlordRegistrationStepId.ManualContactAddress,
            page =
                Page(
                    formModel = ManualAddressFormModel::class,
                    templateName = "forms/manualAddressForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.manualContactAddress.fieldSetHeading",
                            "addressLineOneLabel" to "forms.manualAddress.addressLineOne.label",
                            "addressLineTwoLabel" to "forms.manualAddress.addressLineTwo.label",
                            "townOrCityLabel" to "forms.manualAddress.townOrCity.label",
                            "countyLabel" to "forms.manualAddress.county.label",
                            "postcodeLabel" to "forms.lookupAddress.postcode.label",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.CheckAnswers, null) },
            saveAfterSubmit = false,
        )

    private fun checkAnswersStep() =
        Step(
            id = LandlordRegistrationStepId.CheckAnswers,
            page = LandlordRegistrationCheckAnswersPage(journeyDataService, unreachableStepRedirect),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Declaration, null) },
            saveAfterSubmit = false,
        )

    private fun declarationStep() =
        Step(
            id = LandlordRegistrationStepId.Declaration,
            page =
                Page(
                    formModel = DeclarationFormModel::class,
                    templateName = "forms/declarationForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "bulletOneFineAmount" to "forms.declaration.fines.bullet.one.landlordRegistrationJourneyAmount",
                            "bulletTwoFineAmount" to "forms.declaration.fines.bullet.two.landlordRegistrationJourneyAmount",
                            "options" to
                                listOf(
                                    CheckboxViewModel(
                                        value = "true",
                                        labelMsgKey = "forms.declaration.checkbox.label",
                                    ),
                                ),
                            "submitButtonText" to "forms.buttons.confirmAndCompleteRegistration",
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            handleSubmitAndRedirect = { filteredJourneyData, _, _ -> declarationHandleSubmitAndRedirect(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    private fun verifyIdentityNextAction(filteredJourneyData: JourneyData) =
        if (LandlordRegistrationJourneyDataHelper.isIdentityVerified(filteredJourneyData)) {
            Pair(LandlordRegistrationStepId.ConfirmIdentity, null)
        } else {
            Pair(LandlordRegistrationStepId.IdentityNotVerified, null)
        }

    private fun countryOfResidenceNextAction(filteredJourneyData: JourneyData): Pair<LandlordRegistrationStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.getLivesInEnglandOrWales(filteredJourneyData)!!) {
            Pair(LandlordRegistrationStepId.LookupAddress, null)
        } else {
            Pair(LandlordRegistrationStepId.NonEnglandOrWalesAddress, null)
        }

    private fun selectAddressNextAction(filteredJourneyData: JourneyData): Pair<LandlordRegistrationStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(filteredJourneyData)) {
            Pair(LandlordRegistrationStepId.ManualAddress, null)
        } else {
            Pair(LandlordRegistrationStepId.CheckAnswers, null)
        }

    private fun selectContactAddressNextAction(filteredJourneyData: JourneyData): Pair<LandlordRegistrationStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(filteredJourneyData, isContactAddress = true)) {
            Pair(LandlordRegistrationStepId.ManualContactAddress, null)
        } else {
            Pair(LandlordRegistrationStepId.CheckAnswers, null)
        }

    private fun declarationHandleSubmitAndRedirect(filteredJourneyData: JourneyData): String {
        landlordService.createLandlord(
            baseUserId = SecurityContextHolder.getContext().authentication.name,
            name = LandlordRegistrationJourneyDataHelper.getName(filteredJourneyData)!!,
            email = LandlordRegistrationJourneyDataHelper.getEmail(filteredJourneyData)!!,
            phoneNumber = LandlordRegistrationJourneyDataHelper.getPhoneNumber(filteredJourneyData)!!,
            addressDataModel = LandlordRegistrationJourneyDataHelper.getAddress(filteredJourneyData)!!,
            countryOfResidence = LandlordRegistrationJourneyDataHelper.getCountryOfResidence(filteredJourneyData),
            isVerified = LandlordRegistrationJourneyDataHelper.isIdentityVerified(filteredJourneyData),
            hasAcceptedPrivacyNotice = LandlordRegistrationJourneyDataHelper.getHasAcceptedPrivacyNotice(filteredJourneyData) ?: false,
            nonEnglandOrWalesAddress = LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesAddress(filteredJourneyData),
            dateOfBirth = LandlordRegistrationJourneyDataHelper.getDOB(filteredJourneyData)!!,
        )

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        refreshUserRoles()

        return CONFIRMATION_PATH_SEGMENT
    }

    private fun refreshUserRoles() {
        securityContextService.refreshContext()
    }
}
