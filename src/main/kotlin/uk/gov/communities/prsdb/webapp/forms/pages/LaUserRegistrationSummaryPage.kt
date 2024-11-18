package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.models.dataModels.FormSummaryDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import kotlin.reflect.KClass

class LaUserRegistrationSummaryPage(
    private val formModel: KClass<out FormModel>,
    private val templateName: String,
    private val content: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
    private val invitationService: LocalAuthorityInvitationService,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
    ): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val formData = mutableListOf<FormSummaryDataModel>()
        val sessionToken = invitationService.getTokenFromSession()

        val localAuthority =
            if (sessionToken != null) {
                invitationService.getAuthorityForToken(sessionToken)
            } else {
                null
            }

        formData.add(
            FormSummaryDataModel(
                "registerLaUser.checkAnswers.rowHeading.localAuthority",
                localAuthority?.name,
                null,
            ),
        )
        formData.add(
            FormSummaryDataModel(
                "registerLaUser.checkAnswers.rowHeading.name",
                (journeyData["name"] as PageData)["name"],
                "/${RegisterLaUserStepId.Name.urlPathSegment}",
            ),
        )
        formData.add(
            FormSummaryDataModel(
                "registerLaUser.checkAnswers.rowHeading.email",
                (journeyData["email"] as PageData)["emailAddress"],
                "/${RegisterLaUserStepId.Email.urlPathSegment}",
            ),
        )
        model.addAttribute("formData", formData)
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}
