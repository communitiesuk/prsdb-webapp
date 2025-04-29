package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.steps.LookupAddressStep
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.UpdateLandlordDetailsJourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.getSerializedLookedUpAddresses
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.JourneyDataExtensions.Companion.withUpdatedLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import kotlin.reflect.KFunction

class UpdateLandlordDetailsJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    addressLookupService: AddressLookupService,
    private val landlordService: LandlordService,
    private val landlordBaseUserId: String,
) : UpdateJourney<UpdateLandlordDetailsStepId>(
        journeyType = JourneyType.LANDLORD_DETAILS_UPDATE,
        initialStepId = UpdateLandlordDetailsStepId.UpdateEmail,
        validator = validator,
        journeyDataService = journeyDataService,
        updateStepId = UpdateLandlordDetailsStepId.UpdateDetails,
        updateEntityId = landlordBaseUserId,
    ) {
    init {
        initializeJourneyDataIfNotInitialized()
    }

    override fun createOriginalJourneyData(): JourneyData {
        val landlord = landlordService.retrieveLandlordByBaseUserId(landlordBaseUserId)!!

        infix fun <T : FormModel> StepId.toPageData(fromLandlordFunc: KFunction<T>): Pair<String, PageData> =
            this.urlPathSegment to fromLandlordFunc.call(landlord).toPageData()

        val originalLandlordData =
            mutableMapOf(
                IS_IDENTITY_VERIFIED_KEY to landlord.isVerified,
                LOOKED_UP_ADDRESSES_JOURNEY_DATA_KEY to Json.encodeToString(listOf(AddressDataModel.fromAddress(landlord.address))),
                UpdateLandlordDetailsStepId.UpdateEmail toPageData EmailFormModel::fromLandlord,
                UpdateLandlordDetailsStepId.UpdateName toPageData NameFormModel::fromLandlord,
                UpdateLandlordDetailsStepId.UpdatePhoneNumber toPageData PhoneNumberFormModel::fromLandlord,
                UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress toPageData LookupAddressFormModel::fromLandlord,
                UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress toPageData SelectAddressFormModel::fromLandlord,
                UpdateLandlordDetailsStepId.UpdateDateOfBirth toPageData DateOfBirthFormModel::fromLandlord,
            )

        if (landlord.address.uprn == null) {
            originalLandlordData += UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress toPageData ManualAddressFormModel::fromLandlord
        }

        return originalLandlordData
    }

    override fun initializeJourneyDataIfNotInitialized() {
        if (!isJourneyDataInitialised()) {
            super.initializeJourneyDataIfNotInitialized()

            val journeyData = journeyDataService.getJourneyDataFromSession()
            val lookedUpAddresses = JourneyDataHelper.getPageData(journeyData, originalDataKey)!!.getSerializedLookedUpAddresses()!!
            val journeyDataWithLookedUpAddresses = journeyData.withUpdatedLookedUpAddresses(lookedUpAddresses)
            journeyDataService.setJourneyDataInSession(journeyDataWithLookedUpAddresses)
        }
    }

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
        LookupAddressStep(
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
            nextStepIfAddressesFound = UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress,
            nextStepIfNoAddressesFound = UpdateLandlordDetailsStepId.NoAddressFound,
            addressLookupService = addressLookupService,
            journeyDataService = journeyDataService,
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
                    journeyDataService = journeyDataService,
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

    private val noAddressFoundStep =
        Step(
            id = UpdateLandlordDetailsStepId.NoAddressFound,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "noAddressFoundPage",
                    content =
                        mapOf(
                            "title" to "landlordDetails.update.title",
                            "postcode" to getHouseNameOrNumberAndPostcode().second,
                            "houseNameOrNumber" to getHouseNameOrNumberAndPostcode().first,
                            "searchAgainUrl" to
                                "${LandlordDetailsController.UPDATE_ROUTE}/" +
                                UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment,
                        ),
                ),
            nextAction = { _, _ -> Pair(UpdateLandlordDetailsStepId.ManualEnglandAndWalesAddress, null) },
        )

    private fun getHouseNameOrNumberAndPostcode() =
        JourneyDataHelper
            .getLookupAddressHouseNameOrNumberAndPostcode(
                journeyDataService.getJourneyDataFromSession(),
                UpdateLandlordDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment,
            )!!

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
                noAddressFoundStep,
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
                address = UpdateLandlordDetailsJourneyDataHelper.getAddressIfPresent(journeyData),
                dateOfBirth = UpdateLandlordDetailsJourneyDataHelper.getDateOfBirthIfPresent(journeyData),
            )

        landlordService.updateLandlordForBaseUserId(
            SecurityContextHolder.getContext().authentication.name,
            landlordUpdate,
        )

        journeyDataService.removeJourneyDataAndContextIdFromSession()

        return LandlordDetailsController.LANDLORD_DETAILS_ROUTE
    }

    companion object {
        const val IS_IDENTITY_VERIFIED_KEY = "isIdentityVerified"
    }
}
