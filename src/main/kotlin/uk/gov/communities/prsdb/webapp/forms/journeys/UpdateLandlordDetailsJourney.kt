package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.UpdateLandlordDetailsJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Component
class UpdateLandlordDetailsJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    val landlordService: LandlordService,
) : Journey<UpdateDetailsStepId>(
        journeyType = JourneyType.UPDATE_DETAILS,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val initialStepId = UpdateDetailsStepId.UpdateEmail

    private val updateSessionStep =
        Step(
            id = UpdateDetailsStepId.ChangeDetailsSession,
            page = Page(NoInputFormModel::class, "error/500", mapOf()),
            handleSubmitAndRedirect = { journeyData, _ -> submitAllChanges(journeyData) },
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
                        ),
                ),
            handleSubmitAndRedirect = { _, _ -> getRedirectToUpdateSessionPage() },
            nextAction = { _, _ -> Pair(UpdateDetailsStepId.ChangeDetailsSession, null) },
            saveAfterSubmit = false,
        )

    override val sections =
        createSingleSectionWithSingleTaskFromSteps(
            initialStepId,
            setOf(
                emailStep,
                updateSessionStep,
            ),
        )

    override fun getUnreachableStepRedirect(journeyData: JourneyData) =
        "/${JourneyType.UPDATE_DETAILS.urlPathSegment}/${getLastReachableStep(journeyData)?.step?.id?.urlPathSegment}"

    override fun getPrevStep(
        journeyData: JourneyData,
        targetStep: Step<UpdateDetailsStepId>,
        targetSubPageNumber: Int?,
    ): StepDetails<UpdateDetailsStepId>? {
        val originalLandlordData = JourneyDataHelper.getPageData(journeyData, originalLandlordJourneyDataKey)!!
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

    private fun submitAllChanges(journeyData: JourneyData): String {
        val landlordUpdate =
            LandlordUpdateModel(
                email = UpdateLandlordDetailsJourneyDataHelper.getEmailUpdateIfPresent(journeyData),
            )

        landlordService.updateLandlordForBaseUserId(
            SecurityContextHolder.getContext().authentication.name,
            landlordUpdate,
        )

        journeyDataService.clearJourneyDataFromSession()

        return "/landlord-details"
    }

    private fun getRedirectToUpdateSessionPage(): String =
        "/${JourneyType.UPDATE_DETAILS.urlPathSegment}/${UpdateDetailsStepId.ChangeDetailsSession.urlPathSegment}"

    fun initialiseJourneyDataIfNotInitialised(landlordId: String) {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (journeyData[originalLandlordJourneyDataKey] == null) {
            val landlord = landlordService.retrieveLandlordByBaseUserId(landlordId)!!
            journeyData[originalLandlordJourneyDataKey] = createOriginalLandlordJourneyData(landlord)
            journeyDataService.setJourneyData(journeyData)
        }
    }

    private fun createOriginalLandlordJourneyData(landlord: Landlord): JourneyData =
        mutableMapOf(
            UpdateDetailsStepId.UpdateEmail.urlPathSegment to mutableMapOf("emailAddress" to landlord.email),
        )

    companion object {
        val originalLandlordJourneyDataKey = "original-landlord-data"
    }
}
