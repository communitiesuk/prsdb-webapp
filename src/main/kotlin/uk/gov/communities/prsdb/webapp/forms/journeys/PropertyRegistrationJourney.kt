package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.LandingPageFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class PropertyRegistrationJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<RegisterPropertyStepId>(
        journeyType = JourneyType.PROPERTY_REGISTRATION,
        initialStepId = RegisterPropertyStepId.PropertyType,
        validator = validator,
        journeyDataService = journeyDataService,
        steps =
            listOf(
                Step(
                    id = RegisterPropertyStepId.PropertyType,
                    page =
                        Page(
                            formModel = PropertyTypeFormModel::class,
                            templateName = "forms/propertyTypeForm.html",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                    "fieldSetHeading" to "forms.propertyType.fieldSetHeading",
                                    "radioOptions" to
                                        listOf(
                                            RadiosViewModel(
                                                value = PropertyType.DETACHED_HOUSE,
                                                labelMsgKey = "forms.propertyType.radios.option.detachedHouse.label",
                                                hintMsgKey = "forms.propertyType.radios.option.detachedHouse.hint",
                                            ),
                                            RadiosViewModel(
                                                value = PropertyType.SEMI_DETACHED_HOUSE,
                                                labelMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.label",
                                                hintMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.hint",
                                            ),
                                            RadiosViewModel(
                                                value = PropertyType.TERRACED_HOUSE,
                                                labelMsgKey = "forms.propertyType.radios.option.terracedHouse.label",
                                                hintMsgKey = "forms.propertyType.radios.option.terracedHouse.hint",
                                            ),
                                            RadiosViewModel(
                                                value = PropertyType.FLAT,
                                                labelMsgKey = "forms.propertyType.radios.option.flat.label",
                                                hintMsgKey = "forms.propertyType.radios.option.flat.hint",
                                            ),
                                            RadiosViewModel(
                                                value = PropertyType.OTHER,
                                                labelMsgKey = "forms.propertyType.radios.option.other.label",
                                                hintMsgKey = "forms.propertyType.radios.option.other.hint",
                                                conditionalFragment = "customPropertyTypeInput",
                                            ),
                                        ),
                                ),
                        ),
                    nextAction = { _, _ -> Pair(RegisterPropertyStepId.PlaceholderPage, null) },
                ),
                Step(
                    id = RegisterPropertyStepId.PlaceholderPage,
                    page =
                        Page(
                            formModel = LandingPageFormModel::class,
                            templateName = "placeholder",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                ),
                        ),
                ),
            ),
    )
