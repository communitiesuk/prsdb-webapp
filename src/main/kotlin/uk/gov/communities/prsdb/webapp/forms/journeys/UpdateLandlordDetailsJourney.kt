package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.UpdateLandlordDetailsJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Component
class UpdateLandlordDetailsJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    val landlordService: LandlordService,
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
                updateDetailsStep,
            ),
        )

    override fun getUnreachableStepRedirect(journeyData: JourneyData) =
        if (journeyData[ORIGINAL_LANDLORD_DATA_KEY] == null) {
            UpdateDetailsStepId.UpdateDetails.urlPathSegment
        } else {
            getLastReachableStep(journeyData)!!.step.id.urlPathSegment
        }

    override fun getPrevStep(
        journeyData: JourneyData,
        targetStep: Step<UpdateDetailsStepId>,
        targetSubPageNumber: Int?,
    ): StepDetails<UpdateDetailsStepId>? {
        val originalLandlordData = JourneyDataHelper.getPageData(journeyData, ORIGINAL_LANDLORD_DATA_KEY) ?: return null
        val updatedLandlordData = getUpdatedLandlordData(journeyData, originalLandlordData)

        return super.getPrevStep(updatedLandlordData, targetStep, targetSubPageNumber)
    }

    private fun getUpdatedLandlordData(
        journeyData: JourneyData,
        landlordData: PageData,
    ): JourneyData {
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
                address = null,
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
        }
    }

    private fun createOriginalLandlordJourneyData(landlord: Landlord): JourneyData =
        mutableMapOf(
            UpdateDetailsStepId.UpdateEmail.urlPathSegment to mutableMapOf("emailAddress" to landlord.email),
            UpdateDetailsStepId.UpdateName.urlPathSegment to mutableMapOf("name" to landlord.name),
        )

    companion object {
        const val ORIGINAL_LANDLORD_DATA_KEY = "original-landlord-data"
    }
}
