package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.helpers.LaUserRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import kotlin.reflect.KClass

class LaUserRegistrationCheckAnswersPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val invitationService: LocalAuthorityInvitationService,
) : Page(formModel, templateName, content) {
    override fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): ModelAndView {
        journeyData!!
        val formData = mutableListOf<SummaryListRowViewModel>()
        val sessionToken = invitationService.getTokenFromSession()

        val localAuthority =
            if (sessionToken != null) {
                invitationService.getAuthorityForToken(sessionToken)
            } else {
                throw PrsdbWebException("Local authority not found for this invitation token")
            }

        formData.addAll(
            listOf(
                SummaryListRowViewModel(
                    "registerLaUser.checkAnswers.rowHeading.localAuthority",
                    localAuthority.name,
                    null,
                ),
                SummaryListRowViewModel(
                    "registerLaUser.checkAnswers.rowHeading.name",
                    LaUserRegistrationJourneyDataHelper.getName(journeyData),
                    RegisterLaUserStepId.Name.urlPathSegment,
                ),
                SummaryListRowViewModel(
                    "registerLaUser.checkAnswers.rowHeading.email",
                    LaUserRegistrationJourneyDataHelper.getEmail(journeyData),
                    RegisterLaUserStepId.Email.urlPathSegment,
                ),
            ),
        )

        val modelAndView = super.getModelAndView(validator, pageData, prevStepUrl)
        modelAndView.addObject("formData", formData)

        return modelAndView
    }
}
