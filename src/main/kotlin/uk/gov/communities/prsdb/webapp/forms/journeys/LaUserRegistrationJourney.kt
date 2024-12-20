package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.LaUserRegistrationCheckAnswersPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

@Component
class LaUserRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    invitationService: LocalAuthorityInvitationService,
) : Journey<RegisterLaUserStepId>(
        journeyType = JourneyType.LA_USER_REGISTRATION,
        initialStepId = RegisterLaUserStepId.LandingPage,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            setOf(
                landingPageStep(),
                registerUserStep(),
                emailStep(),
                checkAnswersStep(journeyDataService, invitationService),
            ),
    ) {
    companion object {
        private fun landingPageStep() =
            Step(
                id = RegisterLaUserStepId.LandingPage,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "registerLaUser",
                        content =
                            mapOf(
                                "title" to "registerLAUser.title",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterLaUserStepId.Name, null) },
            )

        private fun registerUserStep() =
            Step(
                id = RegisterLaUserStepId.Name,
                page =
                    Page(
                        formModel = NameFormModel::class,
                        templateName = "forms/nameForm",
                        content =
                            mapOf(
                                "title" to "registerLAUser.title",
                                "fieldSetHeading" to "forms.name.fieldSetHeading",
                                "fieldSetHint" to "forms.name.fieldSetHint",
                                "label" to "forms.name.label",
                                "submitButtonText" to "forms.buttons.continue",
                                "backUrl" to "/${JourneyType.LA_USER_REGISTRATION.urlPathSegment}/",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterLaUserStepId.Email, null) },
            )

        private fun emailStep() =
            Step(
                id = RegisterLaUserStepId.Email,
                page =
                    Page(
                        formModel = EmailFormModel::class,
                        templateName = "forms/emailForm",
                        content =
                            mapOf(
                                "title" to "registerLAUser.title",
                                "fieldSetHeading" to "registerLAUser.email.fieldSetHeading",
                                "fieldSetHint" to "registerLAUser.email.fieldSetHint",
                                "label" to "registerLAUser.email.label",
                                "submitButtonText" to "forms.buttons.continue",
                            ),
                    ),
                nextAction = { _, _ -> Pair(RegisterLaUserStepId.CheckAnswers, null) },
            )

        private fun checkAnswersStep(
            journeyDataService: JourneyDataService,
            invitationService: LocalAuthorityInvitationService,
        ) = Step(
            id = RegisterLaUserStepId.CheckAnswers,
            page =
                LaUserRegistrationCheckAnswersPage(
                    formModel = CheckAnswersFormModel::class,
                    templateName = "forms/checkAnswersForm",
                    content =
                        mapOf(
                            "title" to "registerLAUser.title",
                            "summaryName" to "registerLaUser.checkAnswers.summaryName",
                            "submitButtonText" to "forms.buttons.confirm",
                        ),
                    journeyDataService,
                    invitationService,
                ),
            handleSubmitAndRedirect = { _, _ -> "/${JourneyType.LA_USER_REGISTRATION.urlPathSegment}/success" },
        )
    }
}
