package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.PLACE_NAMES
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.CountryOfResidenceFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NameFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SelectViewModel
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
                            content =
                                mapOf(
                                    "title" to "registerAsALandlord.title",
                                    "fieldSetHeading" to "forms.name.fieldSetHeading",
                                    "fieldSetHint" to "forms.name.fieldSetHint",
                                    "label" to "forms.name.label",
                                    "submitButtonText" to "forms.buttons.continue",
                                    "backUrl" to "/${JourneyType.LANDLORD_REGISTRATION.urlPathSegment}",
                                ),
                        ),
                    nextAction = { _, _ -> Pair(LandlordRegistrationStepId.Email, null) },
                    saveAfterSubmit = false,
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
                                    "submitButtonText" to "forms.buttons.continue",
                                ),
                        ),
                    nextAction = { _, _ -> Pair(LandlordRegistrationStepId.PhoneNumber, null) },
                    saveAfterSubmit = false,
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
                                    "submitButtonText" to "forms.buttons.continue",
                                    "hint" to "forms.phoneNumber.hint",
                                ),
                        ),
                    nextAction = { _, _ -> Pair(LandlordRegistrationStepId.CountryOfResidence, null) },
                ),
                Step(
                    id = LandlordRegistrationStepId.CountryOfResidence,
                    page =
                        Page(
                            formModel = CountryOfResidenceFormModel::class,
                            templateName = "forms/countryOfResidenceForm",
                            content =
                                mapOf(
                                    "title" to "registerAsALandlord.title",
                                    "fieldSetHeading" to "forms.countryOfResidence.fieldSetHeading",
                                    "selectOptions" to PLACE_NAMES.map { SelectViewModel(it) },
                                    "radioOptions" to
                                        listOf(
                                            RadiosViewModel(true, "forms.countryOfResidence.radios.option.yes.label"),
                                            RadiosViewModel(
                                                value = false,
                                                labelMsgKey = "forms.countryOfResidence.radios.option.no.label",
                                                conditionalFragCall =
                                                    "fragments/forms/select :: select(" +
                                                        "#{forms.countryOfResidence.radios.option.no.select.label}, " +
                                                        "'countryOfResidence', null, \${selectOptions})",
                                            ),
                                        ),
                                ),
                        ),
                    // TODO PRSD-561: Set nextAction to InternationalAddress step
                    nextAction = { _, _ -> Pair(LandlordRegistrationStepId.CheckAnswers, 0) },
                    saveAfterSubmit = false,
                ),
            ),
    )
