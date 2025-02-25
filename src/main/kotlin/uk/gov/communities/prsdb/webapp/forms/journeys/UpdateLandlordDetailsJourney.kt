package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.SelectAddressPage
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.UpdateLandlordDetailsJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Component
class UpdateLandlordDetailsJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    val landlordService: LandlordService,
    val addressDataService: AddressDataService,
    val addressLookupService: AddressLookupService,
) : Journey<UpdateDetailsStepId>(
        journeyType = JourneyType.UPDATE_LANDLORD_DETAILS,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val initialStepId = UpdateDetailsStepId.UpdateEmail

    private val updateDetailsStep =
        Step(
            id = UpdateDetailsStepId.UpdateDetails,
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
            id = UpdateDetailsStepId.UpdateEmail,
            page =
                Page(
                    formModel = EmailFormModel::class,
                    templateName = "forms/emailForm",
                    content =
                        mapOf(
                            "title" to "forms.update.title",
                            "fieldSetHeading" to "forms.update.email.fieldSetHeading",
                            "fieldSetHint" to "forms.email.fieldSetHint",
                            "label" to "forms.email.label",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to UpdateDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdateDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdateDetailsStepId.UpdateName, null) },
            saveAfterSubmit = false,
        )

    private val nameStep =
        Step(
            id = UpdateDetailsStepId.UpdateName,
            page =
                Page(
                    formModel = NameFormModel::class,
                    templateName = "forms/nameForm",
                    content =
                        mapOf(
                            "title" to "forms.update.title",
                            "fieldSetHeading" to "forms.update.name.fieldSetHeading",
                            "fieldSetHint" to "forms.name.fieldSetHint",
                            "label" to "forms.name.label",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to UpdateDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> UpdateDetailsStepId.UpdateDetails.urlPathSegment },
            nextAction = { _, _ -> Pair(UpdateDetailsStepId.LookupEnglandAndWalesAddress, null) },
            saveAfterSubmit = false,
        )

    private val lookupAddressStep =
        Step(
            id = UpdateDetailsStepId.LookupEnglandAndWalesAddress,
            page =
                Page(
                    formModel = LookupAddressFormModel::class,
                    templateName = "forms/lookupAddressForm",
                    content =
                        mapOf(
                            "title" to "forms.update.title",
                            "fieldSetHeading" to "forms.update.lookupAddress.fieldSetHeading",
                            "fieldSetHint" to "forms.lookupAddress.fieldSetHint",
                            "postcodeLabel" to "forms.lookupAddress.postcode.label",
                            "postcodeHint" to "forms.lookupAddress.postcode.hint",
                            "houseNameOrNumberLabel" to "forms.lookupAddress.houseNameOrNumber.label",
                            "houseNameOrNumberHint" to "forms.lookupAddress.houseNameOrNumber.hint",
                            "submitButtonText" to "forms.buttons.continue",
                            BACK_URL_ATTR_NAME to UpdateDetailsStepId.UpdateDetails.urlPathSegment,
                        ),
                    shouldDisplaySectionHeader = false,
                ),
            nextAction = { _, _ -> Pair(UpdateDetailsStepId.SelectEnglandAndWalesAddress, null) },
            saveAfterSubmit = false,
        )

    private val selectAddressStep =
        Step(
            id = UpdateDetailsStepId.SelectEnglandAndWalesAddress,
            page =
                SelectAddressPage(
                    formModel = SelectAddressFormModel::class,
                    templateName = "forms/selectAddressForm",
                    content =
                        mapOf(
                            "title" to "forms.update.title",
                            "fieldSetHeading" to "forms.selectAddress.fieldSetHeading",
                            "submitButtonText" to "forms.buttons.useThisAddress",
                            "searchAgainUrl" to
                                "${LandlordDetailsController.UPDATE_ROUTE}/" +
                                UpdateDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment,
                        ),
                    lookupAddressPathSegment = UpdateDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment,
                    addressLookupService = addressLookupService,
                    addressDataService = addressDataService,
                    displaySectionHeader = false,
                ),
            nextAction = { journeyData, _ -> selectAddressNextAction(journeyData) },
            saveAfterSubmit = false,
        )

    private fun selectAddressNextAction(journeyData: JourneyData): Pair<UpdateDetailsStepId, Int?> =
        if (LandlordRegistrationJourneyDataHelper.isManualAddressChosen(journeyData)) {
            Pair(UpdateDetailsStepId.ManualEnglandAndWalesAddress, null)
        } else {
            Pair(UpdateDetailsStepId.UpdateDetails, null)
        }

    private val manualAddressStep =
        Step(
            id = UpdateDetailsStepId.ManualEnglandAndWalesAddress,
            page =
                Page(
                    formModel = ManualAddressFormModel::class,
                    templateName = "forms/manualAddressForm",
                    content =
                        mapOf(
                            "title" to "forms.update.title",
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
            nextAction = { _, _ -> Pair(UpdateDetailsStepId.UpdateDetails, null) },
            saveAfterSubmit = false,
        )

    // The next action flow must have the `updateDetailsStep` after all data changing steps to ensure that validation for all of them is run
    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                emailStep,
                nameStep,
                lookupAddressStep,
                selectAddressStep,
                manualAddressStep,
                updateDetailsStep,
            ),
        )

    override fun getUnreachableStepRedirect(journeyData: JourneyData): String {
        val updatedJourneyData = getUpdatedLandlordData(journeyData)
        return if (updatedJourneyData == null) {
            UpdateDetailsStepId.UpdateDetails.urlPathSegment
        } else {
            getLastReachableStep(updatedJourneyData)!!.step.id.urlPathSegment
        }
    }

    override fun getPrevStep(
        journeyData: JourneyData,
        targetStep: Step<UpdateDetailsStepId>,
        targetSubPageNumber: Int?,
    ): StepDetails<UpdateDetailsStepId>? {
        val updatedLandlordData = getUpdatedLandlordData(journeyData) ?: return null

        return super.getPrevStep(updatedLandlordData, targetStep, targetSubPageNumber)
    }

    private fun getUpdatedLandlordData(journeyData: JourneyData): JourneyData? {
        val landlordData = JourneyDataHelper.getPageData(journeyData, ORIGINAL_LANDLORD_DATA_KEY) ?: return null

        // For any fields where the data is updated, replace the original value with the new value
        for (key in journeyData.keys) {
            landlordData[key] = journeyData[key]
        }
        return landlordData
    }

    private fun updateLandlordWithChangesAndRedirect(journeyData: JourneyData): String {
        val landlordUpdate =
            LandlordUpdateModel(
                email = UpdateLandlordDetailsJourneyDataHelper.getEmailUpdateIfPresent(journeyData),
                fullName = UpdateLandlordDetailsJourneyDataHelper.getNameUpdateIfPresent(journeyData),
                address = UpdateLandlordDetailsJourneyDataHelper.getAddressIfPresent(journeyData, addressDataService),
            )

        landlordService.updateLandlordForBaseUserId(
            SecurityContextHolder.getContext().authentication.name,
            landlordUpdate,
        )

        journeyDataService.clearJourneyDataFromSession()

        return LandlordDetailsController.LANDLORD_DETAILS_ROUTE
    }

    fun initialiseJourneyDataIfNotInitialised(landlordId: String) {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (journeyData[ORIGINAL_LANDLORD_DATA_KEY] == null) {
            val landlord = landlordService.retrieveLandlordByBaseUserId(landlordId)!!
            journeyData[ORIGINAL_LANDLORD_DATA_KEY] = createOriginalLandlordJourneyData(landlord)
            journeyDataService.setJourneyData(journeyData)
            addressDataService.setAddressData(listOf(AddressDataModel.fromAddress(landlord.address)))
        }
    }

    private fun createOriginalLandlordJourneyData(landlord: Landlord): JourneyData {
        val originalLandlordData: JourneyData =
            mutableMapOf(
                UpdateDetailsStepId.UpdateEmail.urlPathSegment to mutableMapOf("emailAddress" to landlord.email),
                UpdateDetailsStepId.UpdateName.urlPathSegment to mutableMapOf("name" to landlord.name),
                UpdateDetailsStepId.LookupEnglandAndWalesAddress.urlPathSegment to
                    mutableMapOf(
                        "postcode" to landlord.address.getPostcodeSearchTerm(),
                        "houseNameOrNumber" to landlord.address.getHouseNameOrNumber(),
                    ),
                UpdateDetailsStepId.SelectEnglandAndWalesAddress.urlPathSegment to
                    mutableMapOf(
                        "address" to landlord.address.getSelectedAddress(),
                    ),
            )

        if (landlord.address.uprn == null) {
            originalLandlordData[UpdateDetailsStepId.ManualEnglandAndWalesAddress.urlPathSegment] =
                mutableMapOf(
                    "addressLineOne" to landlord.address.singleLineAddress,
                    "townOrCity" to landlord.address.getTownOrCity(),
                    "postcode" to landlord.address.getPostcodeSearchTerm(),
                )
        }
        return originalLandlordData
    }

    private fun Address.getHouseNameOrNumber(): String = buildingName ?: buildingNumber ?: singleLineAddress

    private fun Address.getPostcodeSearchTerm(): String = postcode ?: singleLineAddress

    private fun Address.getSelectedAddress(): String = if (uprn == null) MANUAL_ADDRESS_CHOSEN else singleLineAddress

    private fun Address.getTownOrCity(): String = townName ?: singleLineAddress

    companion object {
        const val ORIGINAL_LANDLORD_DATA_KEY = "original-landlord-data"
    }
}
