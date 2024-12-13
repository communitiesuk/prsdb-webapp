package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.INTERNATIONAL_ADDRESS_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.PLACE_NAMES
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.ConfirmIdentityPage
import uk.gov.communities.prsdb.webapp.forms.pages.LandlordRegistrationSummaryPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.pages.VerifyIdentityPage
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.DeclarationFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.InternationalAddressFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.VerifiedIdentityModel
import uk.gov.communities.prsdb.webapp.models.viewModels.CheckboxViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import java.time.LocalDate

@Component
class LandlordRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    addressLookupService: AddressLookupService,
    addressDataService: AddressDataService,
) : Journey<LandlordRegistrationStepId>(
        journeyType = JourneyType.LANDLORD_REGISTRATION,
        initialStepId = LandlordRegistrationStepId.VerifyIdentity,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            listOf(
                verifyIdentityStep(),
                nameStep(),
                dateOfBirthStep(),
                confirmIdentityStep(journeyDataService),
                emailStep(),
                phoneNumberStep(),
                countryOfResidenceStep(),
                lookupAddressStep(),
                selectAddressStep(journeyDataService, addressLookupService, addressDataService),
                manualAddressStep(),
                internationalAddressStep(),
                lookupContactAddressStep(),
                selectContactAddressStep(journeyDataService, addressLookupService, addressDataService),
                manualContactAddressStep(),
                checkAnswersStep(),
                declarationStep(),
            ),
    ) {
    companion object {
        private fun verifyIdentityStep() =
            Step(
                id = LandlordRegistrationStepId.VerifyIdentity,
                page = VerifyIdentityPage(),
                nextAction = { journeyData, _ ->
                    if (doesJourneyDataContainVerifiedIdentity(journeyData)) {
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
                    ),
                nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Email, null) },
                saveAfterSubmit = false,
            )

        private fun confirmIdentityStep(journeyDataService: JourneyDataService) =
            Step(
                id = LandlordRegistrationStepId.ConfirmIdentity,
                page =
                    ConfirmIdentityPage(
                        formModel = CheckAnswersFormModel::class,
                        templateName = "forms/confirmIdentityForm",
                        content =
                            mapOf(
                                "title" to "registerAsALandlord.title",
                                "submitButtonText" to "forms.buttons.confirmAndContinue",
                            ),
                    ) {
                        val journeyData = journeyDataService.getJourneyDataFromSession()
                        journeyDataService.getPageData(
                            journeyData,
                            LandlordRegistrationStepId.VerifyIdentity.urlPathSegment,
                        )
                    },
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
                                "selectOptions" to PLACE_NAMES.map { SelectViewModel(it) },
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
                    ),
                nextAction = { _, _ -> Pair(LandlordRegistrationStepId.SelectAddress, null) },
                saveAfterSubmit = false,
            )

        private fun selectAddressStep(
            journeyDataService: JourneyDataService,
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
                    urlPathSegment = LandlordRegistrationStepId.LookupAddress.urlPathSegment,
                    journeyDataService = journeyDataService,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                ),
            nextAction = { journeyData, _ ->
                selectAddressNextAction(
                    journeyData,
                    journeyDataService,
                )
            },
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
                    ),
                // TODO: Set nextAction to next journey step
                nextAction = { _, _ -> Pair(LandlordRegistrationStepId.CheckAnswers, null) },
                saveAfterSubmit = false,
            )

        private fun internationalAddressStep() =
            Step(
                id = LandlordRegistrationStepId.InternationalAddress,
                page =
                    Page(
                        formModel = InternationalAddressFormModel::class,
                        templateName = "forms/internationalAddressForm",
                        content =
                            mapOf(
                                "title" to "registerAsALandlord.title",
                                "fieldSetHeading" to "forms.internationalAddress.fieldSetHeading",
                                "fieldSetHint" to "forms.internationalAddress.fieldSetHint",
                                "label" to "forms.internationalAddress.label",
                                "limit" to INTERNATIONAL_ADDRESS_MAX_LENGTH,
                                "submitButtonText" to "forms.buttons.continue",
                            ),
                    ),
                // TODO: Set nextAction to next journey step
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
                    ),
                nextAction = { _, _ -> Pair(LandlordRegistrationStepId.SelectContactAddress, null) },
                saveAfterSubmit = false,
            )

        private fun selectContactAddressStep(
            journeyDataService: JourneyDataService,
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
                    urlPathSegment = LandlordRegistrationStepId.LookupContactAddress.urlPathSegment,
                    journeyDataService = journeyDataService,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                ),
            nextAction = { journeyData, _ ->
                selectContactAddressNextAction(
                    journeyData,
                    journeyDataService,
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
                    ),
                // TODO: Set nextAction to next journey step
                nextAction = { _, _ -> Pair(LandlordRegistrationStepId.CheckAnswers, null) },
                saveAfterSubmit = false,
            )

        private fun checkAnswersStep() =
            Step(
                // TODO PRSD-372 update message value(s)
                id = LandlordRegistrationStepId.CheckAnswers,
                page =
                    LandlordRegistrationSummaryPage(
                        formModel = CheckAnswersFormModel::class,
                        templateName = "forms/checkAnswersForm",
                        content =
                            mapOf(
                                "title" to "registerAsALandlord.title",
                                "summaryName" to "registerAsALandlord.title",
                                "submitButtonText" to "forms.buttons.confirmAndContinue",
                            ),
                    ),
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
                                "options" to
                                    listOf(
                                        CheckboxViewModel(
                                            value = "true",
                                            labelMsgKey = "forms.declaration.checkbox.label",
                                        ),
                                    ),
                                "submitButtonText" to "forms.buttons.confirmAndCompleteRegistration",
                            ),
                    ),
                nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Confirmation, null) },
                saveAfterSubmit = false,
            )

        private fun countryOfResidenceNextAction(journeyData: JourneyData): Pair<LandlordRegistrationStepId, Int?> =
            when (
                val livesInUK =
                    objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.CountryOfResidence.urlPathSegment])
                        ?.get("livesInUK")
                        .toString()
            ) {
                "true" -> Pair(LandlordRegistrationStepId.LookupAddress, null)
                "false" -> Pair(LandlordRegistrationStepId.InternationalAddress, null)
                else -> throw IllegalArgumentException(
                    "Invalid value for journeyData[\"${LandlordRegistrationStepId.CountryOfResidence.urlPathSegment}\"][\"livesInUK\"]:" +
                        livesInUK,
                )
            }

        private fun selectAddressNextAction(
            journeyData: JourneyData,
            journeyDataService: JourneyDataService,
        ): Pair<LandlordRegistrationStepId, Int?> =
            if (journeyDataService.getFieldStringValue(
                    journeyData,
                    LandlordRegistrationStepId.SelectAddress.urlPathSegment,
                    "address",
                ) == MANUAL_ADDRESS_CHOSEN
            ) {
                Pair(LandlordRegistrationStepId.ManualAddress, null)
            } else {
                // TODO: Set nextAction to next journey step
                Pair(LandlordRegistrationStepId.CheckAnswers, null)
            }

        private fun selectContactAddressNextAction(
            journeyData: JourneyData,
            journeyDataService: JourneyDataService,
        ): Pair<LandlordRegistrationStepId, Int?> =
            if (journeyDataService.getFieldStringValue(
                    journeyData,
                    LandlordRegistrationStepId.SelectContactAddress.urlPathSegment,
                    "address",
                ) == MANUAL_ADDRESS_CHOSEN
            ) {
                Pair(LandlordRegistrationStepId.ManualContactAddress, null)
            } else {
                // TODO: Set nextAction to next journey step
                Pair(LandlordRegistrationStepId.CheckAnswers, null)
            }

        private fun doesJourneyDataContainVerifiedIdentity(journeyData: JourneyData): Boolean {
            val pageData =
                objectToStringKeyedMap(journeyData[LandlordRegistrationStepId.VerifyIdentity.urlPathSegment]) ?: mapOf()
            return pageData[VerifiedIdentityModel.NAME_KEY] is String &&
                pageData[VerifiedIdentityModel.BIRTH_DATE_KEY] is LocalDate
        }
    }
}
