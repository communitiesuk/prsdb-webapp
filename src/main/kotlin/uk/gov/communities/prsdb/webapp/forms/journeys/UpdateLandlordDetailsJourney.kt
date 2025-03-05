package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LANDLORD_DETAILS_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.UpdateLandlordDetailsJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Component
class UpdateLandlordDetailsJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val landlordService: LandlordService,
    private val addressDataService: AddressDataService,
    addressLookupService: AddressLookupService,
) : UpdateJourney<UpdateLandlordDetailsStepId>(
        journeyType = JourneyType.LANDLORD_DETAILS_UPDATE,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    final override val initialStepId = UpdateLandlordDetailsStepId.UpdateEmail
    override val updateStepId = UpdateLandlordDetailsStepId.UpdateDetails

    override val journeyPathSegment: String = UPDATE_LANDLORD_DETAILS_URL

    private val updateDetailsStep =
        Step(
            id = UpdateLandlordDetailsStepId.UpdateDetails,
            page =
                Page(
                    NoInputFormModel::class,
                    "landlordDetailsView",
                    mapOf(
                        BACK_URL_ATTR_NAME to LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
                    ),
                ),
            handleSubmitAndRedirect = { journeyData, _ -> updateLandlordWithChangesAndRedirect(journeyData) },
        )

    private val emailStep =
        Step(
            id = UpdateLandlordDetailsStepId.UpdateEmail,
            page =
                Page(
                    formModel = EmailFormModel::class,
                    templateName = "forms/emailForm",
                    content =
                        mapOf(
                            "title" to "landlordDetails.update.title",
                            "fieldSetHeading" to "forms.update.email.fieldSetHeading",
                            "fieldSetHint" to "forms.email.fieldSetHint",
                            "label" to "forms.email.label",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { journeyData, _ ->
                if (UpdateLandlordDetailsJourneyDataHelper.getIsIdentityVerified(journeyData)) {
                    Pair(UpdateLandlordDetailsStepId.UpdatePhoneNumber, null)
                } else {
                    Pair(UpdateLandlordDetailsStepId.UpdateName, null)
                }
            },
            saveAfterSubmit = false,
        )

    private val nameStep =
        Step(
            id = UpdateLandlordDetailsStepId.UpdateName,
            page =
                Page(
                    formModel = NameFormModel::class,
                    templateName = "forms/nameForm",
                    content =
                        mapOf(
                            "title" to "landlordDetails.update.title",
                            "fieldSetHeading" to "forms.update.name.fieldSetHeading",
                            "fieldSetHint" to "forms.name.fieldSetHint",
                            "label" to "forms.name.label",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdateLandlordDetailsStepId.UpdateDateOfBirth, null) },
            saveAfterSubmit = false,
        )

    private val dateOfBirthStep =
        Step(
            id = UpdateLandlordDetailsStepId.UpdateDateOfBirth,
            page =
                Page(
                    formModel = DateOfBirthFormModel::class,
                    templateName = "forms/dateForm",
                    content =
                        mapOf(
                            "title" to "forms.update.title",
                            "fieldSetHeading" to "forms.update.dateOfBirth.fieldSetHeading",
                            "fieldSetHint" to "forms.dateOfBirth.fieldSetHint",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdateLandlordDetailsStepId.UpdatePhoneNumber, null) },
            saveAfterSubmit = false,
        )

    private val phoneNumberStep =
        Step(
            id = UpdateLandlordDetailsStepId.UpdatePhoneNumber,
            page =
                Page(
                    formModel = PhoneNumberFormModel::class,
                    templateName = "forms/phoneNumberForm",
                    content =
                        mapOf(
                            "title" to "landlordDetails.update.title",
                            "fieldSetHeading" to "forms.update.phoneNumber.fieldSetHeading",
                            "fieldSetHint" to "forms.phoneNumber.fieldSetHint",
                            "label" to "forms.phoneNumber.label",
                            "submitButtonText" to "forms.buttons.continue",
                            "hint" to "forms.phoneNumber.hint",
                            BACK_URL_ATTR_NAME to UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress, null) },
            saveAfterSubmit = false,
        )

    private val lookupAddressStep =
        Step(
            id = UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress,
            page =
                Page(
                    formModel = LookupAddressFormModel::class,
                    templateName = "forms/lookupAddressForm",
                    content =
                        mapOf(
                            "title" to "landlordDetails.update.title",
                            "fieldSetHeading" to "forms.update.lookupAddress.fieldSetHeading",
                            "fieldSetHint" to "forms.lookupAddress.fieldSetHint",
                            "postcodeLabel" to "forms.lookupAddress.postcode.label",
                            "postcodeHint" to "forms.lookupAddress.postcode.hint",
                            "houseNameOrNumberLabel" to "forms.lookupAddress.houseNameOrNumber.label",
                            "houseNameOrNumberHint" to "forms.lookupAddress.houseNameOrNumber.hint",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to UpdateLandlordDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                    shouldDisplaySectionHeader = false,
                ),
            nextAction = { _, _ -> Pair(UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress, null) },
            saveAfterSubmit = false,
        )

    private val selectAddressStep =
        Step(
            id = UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress,
            page =
                SelectAddressPage(
                    formModel = SelectAddressFormModel::class,
                    templateName = "forms/selectAddressForm",
                    content =
                        mapOf(
                            "title" to "landlordDetails.update.title",
                            "fieldSetHeading" to "forms.selectAddress.fieldSetHeading",
                            "submitButtonText" to "forms.buttons.useThisAddress",
                            "searchAgainUrl" to
                                "${LandlordDetailsController.UPDATE_ROUTE}/" +
                                UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment,
                        ),
                    lookupAddressPathSegment = UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                    displaySectionHeader = false,
                ),
            nextAction = { journeyData, _ -> selectAddressNextAction(journeyData) },
            saveAfterSubmit = false,
        )

    private fun selectAddressNextAction(journeyData: JourneyData): Pair<UpdateLandlordDetailsStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData)) {
            Pair(UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress, null)
        } else {
            Pair(UpdateLandlordDetailsStepId.UpdateDetails, null)
        }

    private val manualAddressStep =
        Step(
            id = UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress,
            page =
                Page(
                    formModel = ManualAddressFormModel::class,
                    templateName = "forms/manualAddressForm",
                    content =
                        mapOf(
                            "title" to "landlordDetails.update.title",
                            "fieldSetHeading" to "forms.manualAddress.landlordRegistration.fieldSetHeading",
                            "fieldSetHint" to "forms.manualAddress.fieldSetHint",
                            "addressLineOneLabel" to "forms.manualAddress.addressLineOne.label",
                            "addressLineTwoLabel" to "forms.manualAddress.addressLineTwo.label",
                            "townOrCityLabel" to "forms.manualAddress.townOrCity.label",
                            "countyLabel" to "forms.manualAddress.county.label",
                            "postcodeLabel" to "forms.lookupAddress.postcode.label",
                            "submitButtonText" to "forms.buttons.continue",
                        ),
                    shouldDisplaySectionHeader = false,
                ),
            nextAction = { _, _ -> Pair(UpdateLandlordDetailsStepId.UpdateDetails, null) },
            saveAfterSubmit = false,
        )

    // The next action flow must have the `updateDetailsStep` after all data changing steps to ensure that validation for all of them is run
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                emailStep,
                nameStep,
                dateOfBirthStep,
                phoneNumberStep,
                lookupAddressStep,
                selectAddressStep,
                manualAddressStep,
                updateDetailsStep,
            ),
        )

    private fun updateLandlordWithChangesAndRedirect(journeyData: JourneyData): String {
        val landlordUpdate =
            LandlordUpdateModel(
                email = UpdateLandlordDetailsJourneyDataHelper.getEmailUpdateIfPresent(journeyData),
                name = UpdateLandlordDetailsJourneyDataHelper.getNameUpdateIfPresent(journeyData),
                phoneNumber = UpdateLandlordDetailsJourneyDataHelper.getPhoneNumberIfPresent(journeyData),
                address = UpdateLandlordDetailsJourneyDataHelper.getAddressIfPresent(journeyData, addressDataService),
                dateOfBirth = UpdateLandlordDetailsJourneyDataHelper.getDateOfBirthIfPresent(journeyData),
            )

        landlordService.updateLandlordForBaseUserId(
            SecurityContextHolder.getContext().authentication.name,
            landlordUpdate,
        )

        journeyDataService.clearJourneyDataFromSession()

        return LandlordDetailsController.LANDLORD_DETAILS_ROUTE
    }

    override fun createOriginalJourneyData(updateEntityId: String): JourneyData {
        val landlord = landlordService.retrieveLandlordByBaseUserId(updateEntityId)!!

        val originalLandlordData =
            mutableMapOf(
                IS_IDENTITY_VERIFIED_KEY to landlord.isVerified,
                UpdateLandlordDetailsStepId.UpdateEmail.urlPathSegment to mapOf("emailAddress" to landlord.email),
                UpdateLandlordDetailsStepId.UpdateName.urlPathSegment to mapOf("name" to landlord.name),
                UpdateLandlordDetailsStepId.UpdatePhoneNumber.urlPathSegment to mapOf("phoneNumber" to landlord.phoneNumber),
                UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment to
                    mapOf(
                        "postcode" to landlord.address.getPostcodeSearchTerm(),
                        "houseNameOrNumber" to landlord.address.getHouseNameOrNumber(),
                    ),
                UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress.urlPathSegment to
                    mapOf(
                        "address" to landlord.address.getSelectedAddress(),
                    ),
                ORIGINAL_ADDRESS_DATA_KEY to
                    mapOf(
                        "address" to Json.encodeToString(AddressDataModel.fromAddress(landlord.address)),
                    ),
                UpdateLandlordDetailsStepId.UpdateDateOfBirth.urlPathSegment to
                    mapOf(
                        "day" to landlord.dateOfBirth?.dayOfMonth.toString(),
                        "month" to landlord.dateOfBirth?.monthValue.toString(),
                        "year" to landlord.dateOfBirth?.year.toString(),
                    ),
            )

        if (landlord.address.uprn == null) {
            originalLandlordData[UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress.urlPathSegment] =
                mapOf(
                    "addressLineOne" to landlord.address.singleLineAddress,
                    "townOrCity" to landlord.address.getTownOrCity(),
                    "postcode" to landlord.address.getPostcodeSearchTerm(),
                )
        }

        return originalLandlordData
    }

    override fun initialiseJourneyDataIfNotInitialised(
        updateEntityId: String,
        journeyDataKey: String?,
    ) {
        if (!isJourneyDataInitialised(journeyDataKey)) {
            super.initialiseJourneyDataIfNotInitialised(updateEntityId, journeyDataKey)
            addressDataService.setAddressData(getOriginalAddressData())
        }
    }

    private fun Address.getHouseNameOrNumber(): String = buildingName ?: buildingNumber ?: singleLineAddress

    private fun Address.getPostcodeSearchTerm(): String = postcode ?: singleLineAddress

    private fun Address.getSelectedAddress(): String = if (uprn == null) MANUAL_ADDRESS_CHOSEN else singleLineAddress

    private fun Address.getTownOrCity(): String = townName ?: singleLineAddress

    private fun getOriginalAddressData(): List<AddressDataModel> {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val originalJourneyData = JourneyDataHelper.getPageData(journeyData, originalDataKey)!!
        val originalAddressData = JourneyDataHelper.getPageData(originalJourneyData, ORIGINAL_ADDRESS_DATA_KEY)!!
        return listOf(Json.decodeFromString(originalAddressData["address"] as String))
    }

    companion object {
        private const val ORIGINAL_ADDRESS_DATA_KEY = "original-address-data"
        const val IS_IDENTITY_VERIFIED_KEY = "isIdentityVerified"
    }
}
