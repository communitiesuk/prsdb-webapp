package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class LaUserRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<RegisterLaUserStepId>(
        journeyType = JourneyType.LA_USER_REGISTRATION,
        initialStepId = RegisterLaUserStepId.Email,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            listOf(
                /*Step(
                    id = RegisterLaUserStepId.Name,
                    page =
                        Page(),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLaUserStepId.Email, subPageNumber?.plus(1)) },
                ),*/
                Step(
                    id = RegisterLaUserStepId.Email,
                    page =
                        Page(
                            formModel = EmailFormModel::class,
                            templateName = "forms/emailForm",
                            contentKeys =
                                mapOf(
                                    "title" to "registerLAUser.title",
                                    "fieldSetHeading" to "forms.email.fieldSetHeading",
                                    "fieldSetHint" to "forms.email.fieldSetHint",
                                    "label" to "forms.email.label",
                                ),
                        ),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLaUserStepId.CheckAnswers, subPageNumber?.plus(1)) },
                ),
                /*Step(
                    id = RegisterLaUserStepId.CheckAnswers,
                    page =
                        Page(),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLaUserStepId.CheckAnswers, subPageNumber?.plus(1)) },
                ),*/
            ),
    )
