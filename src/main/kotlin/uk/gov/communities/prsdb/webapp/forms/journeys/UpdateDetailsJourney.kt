package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepDetails
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.getEmailUpdateIfPresent
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Component
class UpdateDetailsJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    val landlordService: LandlordService,
) : Journey<UpdateDetailsStepId>(
        journeyType = JourneyType.UPDATE_DETAILS,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    override val initialStepId = UpdateDetailsStepId.InitialStep

    private val updateSessionStep =
        Step(
            id = UpdateDetailsStepId.ChangeDetailsSession,
            page = Page(NoInputFormModel::class, "error/500", mapOf()),
            handleSubmitAndRedirect = { journeyData, _ -> submitAllChanges(journeyData) },
        )

    private val initialStep =
        Step(
            id = UpdateDetailsStepId.InitialStep,
            page = Page(NoInputFormModel::class, "error/500", mapOf()),
            nextAction = { _, _ -> Pair(UpdateDetailsStepId.UpdateEmail, null) },
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
            handleSubmitAndRedirect = { _, _ -> redirectToSessionPage() },
            nextAction = { _, _ -> Pair(UpdateDetailsStepId.ChangeDetailsSession, null) },
            saveAfterSubmit = false,
        )

    override val steps =
        setOf(
            initialStep,
            emailStep,
            updateSessionStep,
        )

    override fun getPrevStep(
        journeyData: JourneyData,
        targetStep: Step<UpdateDetailsStepId>,
        targetSubPageNumber: Int?,
    ): StepDetails<UpdateDetailsStepId>? {
        val updatedLandlordData = JourneyDataHelper.getPageData(journeyData, UpdateDetailsStepId.InitialStep.urlPathSegment)!!
        for (key in journeyData.keys) {
            updatedLandlordData[key] = journeyData[key]
        }

        return super.getPrevStep(updatedLandlordData, targetStep, targetSubPageNumber)
    }

    private fun submitAllChanges(journeyData: JourneyData): String {
        val landlordUpdate =
            LandlordUpdateModel(
                email = journeyData.getEmailUpdateIfPresent(),
            )

        landlordService.updateLandlordEmailForBaseUserId(
            SecurityContextHolder.getContext().authentication.name,
            landlordUpdate,
        )

        journeyDataService.deleteJourneyData()

        return "/landlord-details"
    }

    private fun redirectToSessionPage(): String =
        UriComponentsBuilder
            .newInstance()
            .path("/${JourneyType.UPDATE_DETAILS.urlPathSegment}/${UpdateDetailsStepId.ChangeDetailsSession.urlPathSegment}")
            .build(true)
            .toUriString()

    fun initialiseJourneyDataIfNotInitialised(landlordId: String) {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        if (journeyData[UpdateDetailsStepId.InitialStep.urlPathSegment] == null) {
            val landlord = landlordService.retrieveLandlordByBaseUserId(landlordId)!!
            journeyData[UpdateDetailsStepId.InitialStep.urlPathSegment] = journeyDataFromLandlord(landlord)
            journeyDataService.setJourneyData(journeyData)
        }
    }

    private fun journeyDataFromLandlord(landlord: Landlord): JourneyData =
        mutableMapOf(
            UpdateDetailsStepId.UpdateEmail.urlPathSegment to mutableMapOf("emailAddress" to landlord.email),
        )
}
