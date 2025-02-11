package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
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
    override val initialStepId = UpdateDetailsStepId.ReturnToDetails

    private val viewDetailsStep =
        Step(
            id = UpdateDetailsStepId.ReturnToDetails,
            page = Page(NoInputFormModel::class, "redirect:/${UpdateDetailsStepId.ReturnToDetails}", mapOf()),
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
            handleSubmitAndRedirect = { journeyData, _ -> handleChangeSubmitAndRedirect(journeyData) },
            saveAfterSubmit = false,
        )

    override val steps = setOf(emailStep)

    override fun isStepReachable(
        journeyData: JourneyData,
        targetStep: Step<UpdateDetailsStepId>,
        targetSubPageNumber: Int?,
    ): Boolean = true

    override fun getPrevStep(
        journeyData: JourneyData,
        targetStep: Step<UpdateDetailsStepId>,
        targetSubPageNumber: Int?,
    ): StepDetails<UpdateDetailsStepId>? {
        // This stores journeyData for only the page the user is on
        // and excludes user data for other pages in the journey
        val stepData = JourneyDataHelper.getPageData(journeyData, targetStep.name)
        val filteredJourneyData = mutableMapOf<String, Any?>(targetStep.name to stepData)

        return StepDetails(viewDetailsStep, null, filteredJourneyData)
    }

    private fun handleChangeSubmitAndRedirect(journeyData: JourneyData): String {
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
}
