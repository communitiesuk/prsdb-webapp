package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_PLACE_NAMES
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.constants.NON_ENGLAND_OR_WALES_ADDRESS_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.pages.ConfirmIdentityPage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordRegistrationCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.pages.VerifyIdentityPage
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NonEnglandOrWalesAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Component
class LandlordRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    addressLookupService: AddressLookupService,
    addressDataService: AddressDataService,
    landlordService: LandlordService,
    emailNotificationService: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
) : Journey<LandlordRegistrationStepId>(
        journeyType = JourneyType.LANDLORD_REGISTRATION,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    final override val initialStepId = LandlordRegistrationStepId.VerifyIdentity

    override val sections =
        listOf(
            JourneySection(privacyNoticeTasks(), "registerAsALandlord.section.privacyNotice.heading"),
            JourneySection(
                registerDetailsTasks(addressLookupService, addressDataService),
                "registerAsALandlord.section.yourDetails.heading",
            ),
            JourneySection(
                checkAndSubmitDetailsTasks(addressDataService, landlordService, emailNotificationService),
                "registerAsALandlord.section.checkAndSubmit.heading",
            ),
        )

    private fun privacyNoticeTasks(): List<JourneyTask<LandlordRegistrationStepId>> = emptyList()

    private fun registerDetailsTasks(
        addressLookupService: AddressLookupService,
        addressDataService: AddressDataService,
    ): List<JourneyTask<LandlordRegistrationStepId>> =
        listOf(
            identityTask(),
            JourneyTask.withOneStep(emailStep()),
            JourneyTask.withOneStep(phoneNumberStep()),
            JourneyTask.withOneStep(countryOfResidenceStep()),
            landlordAddressesTask(addressLookupService, addressDataService),
        )

    private fun checkAndSubmitDetailsTasks(
        addressDataService: AddressDataService,
        landlordService: LandlordService,
        emailNotificationService: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
    ): List<JourneyTask<LandlordRegistrationStepId>> =
        listOf(
            JourneyTask.withOneStep(checkAnswersStep(addressDataService)),
            JourneyTask.withOneStep(declarationStep(journeyDataService, landlordService, addressDataService, emailNotificationService)),
        )

    private fun identityTask() =
        JourneyTask<LandlordRegistrationStepId>(
            LandlordRegistrationStepId.VerifyIdentity,
            setOf(
                verifyIdentityStep(),
                nameStep(),
                dateOfBirthStep(),
                confirmIdentityStep(),
            ),
        )

    private fun landlordAddressesTask(
        addressLookupService: AddressLookupService,
        addressDataService: AddressDataService,
    ) = JourneyTask(
        LandlordRegistrationStepId.LookupAddress,
        setOf(
            lookupAddressStep(),
            selectAddressStep(addressLookupService, addressDataService),
            manualAddressStep(),
            nonEnglandOrWalesAddressStep(),
            lookupContactAddressStep(),
            selectContactAddressStep(addressLookupService, addressDataService),
            manualContactAddressStep(),
        ),
    )

    private fun verifyIdentityStep() =
        Step(
            id = LandlordRegistrationStepId.VerifyIdentity,
            page = VerifyIdentityPage(),
            nextAction = { journeyData, _ ->
                if (LandlordRegistrationJourneyDataHelper.isIdentityVerified(journeyData)) {
                    Pair(LandlordRegistrationStepId.ConfirmIdentity, null)
                } else {
                    Pair(LandlordRegistrationStepId.Name, null)
                }
            },
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
                            "backUrl" to "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}",
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
                    formModel = CheckAnswersFormModel::class,
                    templateName = "forms/confirmIdentityForm",
                    content =
                        mapOf(
                            "title" to "registerAsALandlord.title",
                            "fieldSetHeading" to "forms.confirmDetails.heading",
                            "fieldSetHint" to "forms.confirmDetails.summary",
                            "submitButtonText" to "forms.buttons.confirmAndContinue",
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
                                        labelMsgKey = "forms.countryOfResidence.radios.option.yes.label",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        valueStr = "no",
                                        labelMsgKey = "forms.countryOfResidence.radios.option.no.label",
                                        conditionalFragment = "countryOfResidenceSelect",
                                    ),
                                ),
                        ),
                    shouldDisplaySectionHeader = true,
                ),
            nextAction = { journeyData, _ -> countryOfResidenceNextAction(journeyData) },
            saveAfterSubmit = false,
        )

    private fun lookupAddressStep() =
        Step(
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
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.SelectAddress, null) },
            saveAfterSubmit = false,
        )

    private fun selectAddressStep(
        addressLookupService: AddressLookupService,
        addressDataService: AddressDataService,
    ) = Step(
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
                            "/${REGISTER_LANDLORD_JOURNEY_URL}/" +
                            LandlordRegistrationStepId.LookupAddress.urlPathSegment,
                    ),
                lookupAddressPathSegment = LandlordRegistrationStepId.LookupAddress.urlPathSegment,
                addressLookupService = addressLookupService,
                addressDataService = addressDataService,
                displaySectionHeader = true,
            ),
        nextAction = { journeyData, _ -> selectAddressNextAction(journeyData) },
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
        Step(
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
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.SelectContactAddress, null) },
            saveAfterSubmit = false,
        )

    private fun selectContactAddressStep(
        addressLookupService: AddressLookupService,
        addressDataService: AddressDataService,
    ) = Step(
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
                            "/${REGISTER_LANDLORD_JOURNEY_URL}/" +
                            LandlordRegistrationStepId.LookupContactAddress.urlPathSegment,
                    ),
                lookupAddressPathSegment = LandlordRegistrationStepId.LookupContactAddress.urlPathSegment,
                addressLookupService = addressLookupService,
                addressDataService = addressDataService,
                displaySectionHeader = true,
            ),
        nextAction = { journeyData, _ ->
            selectContactAddressNextAction(
                journeyData,
            )
        },
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

    private fun checkAnswersStep(addressDataService: AddressDataService) =
        Step(
            id = LandlordRegistrationStepId.CheckAnswers,
            page = LandlordRegistrationCheckAnswersPage(addressDataService, displaySectionHeader = true),
            nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Declaration, null) },
            saveAfterSubmit = false,
        )

    private fun declarationStep(
        journeyDataService: JourneyDataService,
        landlordService: LandlordService,
        addressDataService: AddressDataService,
        emailNotificationService: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
    ) = Step(
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
        handleSubmitAndRedirect = { journeyData, _ ->
            declarationHandleSubmitAndRedirect(
                journeyData,
                journeyDataService,
                landlordService,
                addressDataService,
                emailNotificationService,
            )
        },
        saveAfterSubmit = false,
    )

    private fun countryOfResidenceNextAction(journeyData: JourneyData): Pair<LandlordRegistrationStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.getLivesInEnglandOrWales(journeyData)!!) {
            Pair(LandlordRegistrationStepId.LookupAddress, null)
        } else {
            Pair(LandlordRegistrationStepId.NonEnglandOrWalesAddress, null)
        }

    private fun selectAddressNextAction(journeyData: JourneyData): Pair<LandlordRegistrationStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData)) {
            Pair(LandlordRegistrationStepId.ManualAddress, null)
        } else {
            Pair(LandlordRegistrationStepId.CheckAnswers, null)
        }

    private fun selectContactAddressNextAction(journeyData: JourneyData): Pair<LandlordRegistrationStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData, isContactAddress = true)
        ) {
            Pair(LandlordRegistrationStepId.ManualContactAddress, null)
        } else {
            Pair(LandlordRegistrationStepId.CheckAnswers, null)
        }

    private fun declarationHandleSubmitAndRedirect(
        journeyData: JourneyData,
        journeyDataService: JourneyDataService,
        landlordService: LandlordService,
        addressDataService: AddressDataService,
        emailNotificationService: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
    ): String {
        val landlord =
            landlordService.createLandlord(
                baseUserId = SecurityContextHolder.getContext().authentication.name,
                name = LandlordRegistrationJourneyDataHelper.getName(journeyData)!!,
                email = LandlordRegistrationJourneyDataHelper.getEmail(journeyData)!!,
                phoneNumber = LandlordRegistrationJourneyDataHelper.getPhoneNumber(journeyData)!!,
                addressDataModel =
                    LandlordRegistrationJourneyDataHelper.getAddress(journeyData, addressDataService)!!,
                countryOfResidence = LandlordRegistrationJourneyDataHelper.getCountryOfResidence(journeyData),
                nonEnglandOrWalesAddress =
                    LandlordRegistrationJourneyDataHelper.getNonEnglandOrWalesAddress(journeyData),
                dateOfBirth = LandlordRegistrationJourneyDataHelper.getDOB(journeyData)!!,
            )

        emailNotificationService.sendEmail(
            landlord.email,
            LandlordRegistrationConfirmationEmail(
                RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                LANDLORD_DASHBOARD_URL,
            ),
        )

        journeyDataService.clearJourneyDataFromSession()

        return "/$REGISTER_LANDLORD_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT"
    }
}
