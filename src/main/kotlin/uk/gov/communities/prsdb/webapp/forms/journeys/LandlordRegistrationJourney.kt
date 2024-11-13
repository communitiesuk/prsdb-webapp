package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class LandlordRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<LandlordRegistrationStepId>(
        journeyType = JourneyType.LANDLORD_REGISTRATION,
        initialStepId = LandlordRegistrationStepId.Name,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            listOf(
                Step(
                    id = LandlordRegistrationStepId.Name,
                    page =
                        Page(
                            formModel = NameFormModel::class,
                            templateName = "forms/nameForm",
                            contentKeys =
                                mapOf(
                                    "title" to "registerAsALandlord.title",
                                    "fieldSetHeading" to "forms.name.fieldSetHeading",
                                    "fieldSetHint" to "forms.name.fieldSetHint",
                                    "label" to "forms.name.label",
                                    "submitButtonText" to "forms.buttons.saveAndContinue",
                                    "backUrl" to "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}",
                                ),
                        ),
                    nextAction = { _, subPageNumber: Int? -> Pair(LandlordRegistrationStepId.Email, null) },
                ),
                Step(
                    id = LandlordRegistrationStepId.Email,
                    page =
                        Page(
                            formModel = EmailFormModel::class,
                            templateName = "forms/emailForm",
                            content =
                                mapOf(
                                    "title" to "registerAsALandlord.title",
                                    "fieldSetHeading" to "forms.email.fieldSetHeading",
                                    "fieldSetHint" to "forms.email.fieldSetHint",
                                    "label" to "forms.email.label",
                                    "submitButtonText" to "forms.buttons.saveAndContinue",
                                ),
                        ),
                    nextAction = { _, _: Int? -> Pair(LandlordRegistrationStepId.PhoneNumber, null) },
                ),
                Step(
                    id = LandlordRegistrationStepId.PhoneNumber,
                    page =
                        Page(
                            formModel = PhoneNumberFormModel::class,
                            templateName = "forms/phoneNumberForm",
                            content =
                                mapOf(
                                    "title" to "registerAsALandlord.title",
                                    "fieldSetHeading" to "forms.phoneNumber.fieldSetHeading",
                                    "fieldSetHint" to "forms.phoneNumber.fieldSetHint",
                                    "label" to "forms.phoneNumber.label",
                                    "hint" to "forms.phoneNumber.hint",
                                ),
                        ),
                    // TODO PRSD-371 the next action should be updated to the `Select Country` step
                    nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Email, null) },
                ),
            ),
    )
