package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class LaUserRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<RegisterLaUserStepId>(
        journeyType = JourneyType.LA_USER_REGISTRATION,
        initialStepId = RegisterLaUserStepId.Name,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            listOf(
                Step(
                    id = RegisterLaUserStepId.Name,
                    page =
                        Page(
                            formModel = NameFormModel::class,
                            templateName = "forms/nameForm",
                            contentKeys =
                                mapOf(
                                    "title" to "registerLAUser.title",
                                    "fieldSetHeading" to "forms.name.fieldSetHeading",
                                    "fieldSetHint" to "forms.name.fieldSetHint",
                                    "label" to "forms.name.label",
                                    "submitButtonText" to "forms.buttons.continue",
                                    "backUrl" to "/${JourneyType.LA_USER_REGISTRATION.urlPathSegment}/",
                                ),
                        ),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLaUserStepId.Email, subPageNumber?.plus(1)) },
                ),
                Step(
                    id = RegisterLaUserStepId.Email,
                    page =
                        Page(
                            formModel = EmailFormModel::class,
                            templateName = "forms/emailForm",
                            contentKeys =
                                mapOf(
                                    "title" to "registerLAUser.title",
                                    "fieldSetHeading" to "registerLAUser.email.fieldSetHeading",
                                    "fieldSetHint" to "registerLAUser.email.fieldSetHint",
                                    "label" to "registerLAUser.email.label",
                                    "submitButtonText" to "forms.buttons.continue",
                                    "backUrl" to "/${JourneyType.LA_USER_REGISTRATION.urlPathSegment}/${RegisterLaUserStepId.Name}",
                                ),
                        ),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLaUserStepId.CheckAnswers, subPageNumber?.plus(1)) },
                ),
                /*TODO: PRSD-541 - check answers page
                Step(
                    id = RegisterLaUserStepId.CheckAnswers,
                    page =
                        Page(),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLaUserStepId.CheckAnswers, subPageNumber?.plus(1)) },
                ),*/
            ),
    )
