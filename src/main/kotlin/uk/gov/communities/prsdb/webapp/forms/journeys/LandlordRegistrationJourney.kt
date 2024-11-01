package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLandlordStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class LandlordRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<RegisterLandlordStepId>(
        journeyType = JourneyType.LANDLORD_REGISTRATION,
        initialStepId = RegisterLandlordStepId.PhoneNumber,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            listOf(
                Step(
                    id = RegisterLandlordStepId.PhoneNumber,
                    page =
                        Page(
                            formModel = PhoneNumberFormModel::class,
                            templateName = "forms/phoneNumberForm",
                            contentKeys =
                                mapOf(
                                    "title" to "registerAsALandlord.title",
                                    "fieldSetHeading" to "forms.phoneNumber.fieldSetHeading",
                                    "fieldSetHint" to "forms.phoneNumber.fieldSetHint",
                                    "label" to "forms.phoneNumber.label",
                                    "hint" to "forms.phoneNumber.hint",
                                    "backUrl" to "/",
                                ),
                        ),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLandlordStepId.Email, 0) },
                ),
                Step(
                    id = RegisterLandlordStepId.Email,
                    page =
                        Page(
                            formModel = EmailFormModel::class,
                            templateName = "forms/emailForm",
                            contentKeys =
                                mapOf(
                                    "title" to "registerAsALandlord.title",
                                    "fieldSetHeading" to "forms.email.fieldSetHeading",
                                    "fieldSetHint" to "forms.email.fieldSetHint",
                                    "label" to "forms.email.label",
                                ),
                        ),
                    nextAction = { _, subPageNumber: Int? -> Pair(RegisterLandlordStepId.Email, subPageNumber?.plus(1)) },
                ),
            ),
    )
