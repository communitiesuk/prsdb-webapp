package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.models.formModels.LandingPageFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosButtonViewModel
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
                                            RadiosButtonViewModel(
                                                value = PropertyType.DETACHED_HOUSE,
                                                labelMsgKey = "forms.propertyType.radios.option.detachedHouse.label",
                                                hintMsgKey = "forms.propertyType.radios.option.detachedHouse.hint",
                                            ),
                                            RadiosButtonViewModel(
                                                value = PropertyType.SEMI_DETACHED_HOUSE,
                                                labelMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.label",
                                                hintMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.hint",
                                            ),
                                            RadiosButtonViewModel(
                                                value = PropertyType.TERRACED_HOUSE,
                                                labelMsgKey = "forms.propertyType.radios.option.terracedHouse.label",
                                                hintMsgKey = "forms.propertyType.radios.option.terracedHouse.hint",
                                            ),
                                            RadiosButtonViewModel(
                                                value = PropertyType.FLAT,
                                                labelMsgKey = "forms.propertyType.radios.option.flat.label",
                                                hintMsgKey = "forms.propertyType.radios.option.flat.hint",
                                            ),
                                            RadiosButtonViewModel(
                                                value = PropertyType.OTHER,
                                                labelMsgKey = "forms.propertyType.radios.option.other.label",
                                                hintMsgKey = "forms.propertyType.radios.option.other.hint",
                                                conditionalFragment = "customPropertyTypeInput",
                                            ),
                                        ),
                                ),
                        ),
                    nextAction = { _, _ -> Pair(RegisterPropertyStepId.OwnershipType, null) },
                ),
                Step(
                    id = RegisterPropertyStepId.OwnershipType,
                    page =
                        Page(
                            formModel = OwnershipTypeFormModel::class,
                            templateName = "forms/ownershipTypeForm.html",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                    "fieldSetHeading" to "forms.ownershipType.fieldSetHeading",
                                    "radioOptions" to
                                        listOf(
                                            RadiosButtonViewModel(
                                                value = OwnershipType.FREEHOLD,
                                                labelMsgKey = "forms.ownershipType.radios.option.freehold.label",
                                                hintMsgKey = "forms.ownershipType.radios.option.freehold.hint",
                                            ),
                                            RadiosButtonViewModel(
                                                value = OwnershipType.LEASEHOLD,
                                                labelMsgKey = "forms.ownershipType.radios.option.leasehold.label",
                                                hintMsgKey = "forms.ownershipType.radios.option.leasehold.hint",
                                            ),
                                        ),
                                ),
                        ),
                    nextAction = { _, _ -> Pair(RegisterPropertyStepId.Occupancy, null) },
                ),
                Step(
                    id = RegisterPropertyStepId.Occupancy,
                    page =
                        Page(
                            formModel = OccupancyFormModel::class,
                            templateName = "forms/propertyOccupancyForm",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                    "fieldSetHeading" to "forms.occupancy.fieldSetHeading",
                                    "radioOptions" to
                                        listOf(
                                            RadiosButtonViewModel(
                                                value = true,
                                                labelMsgKey = "forms.occupancy.radios.option.yes.label",
                                                hintMsgKey = "forms.occupancy.radios.option.yes.hint",
                                            ),
                                            RadiosButtonViewModel(
                                                value = false,
                                                labelMsgKey = "forms.occupancy.radios.option.no.label",
                                                hintMsgKey = "forms.occupancy.radios.option.no.hint",
                                            ),
                                        ),
                                ),
                        ),
                    nextAction = { journeyData, subpage -> occupancyNextAction(journeyData, subpage) },
                ),
                Step(
                    id = RegisterPropertyStepId.NumberOfHouseholds,
                    page =
                        Page(
                            formModel = NumberOfHouseholdsFormModel::class,
                            templateName = "forms/numberOfHouseholdsForm",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                    "fieldSetHeading" to "forms.numberOfHouseholds.fieldSetHeading",
                                    "label" to "forms.numberOfHouseholds.label",
                                ),
                        ),
                    nextAction = { _, _ -> Pair(RegisterPropertyStepId.NumberOfPeople, null) },
                ),
                Step(
                    id = RegisterPropertyStepId.NumberOfPeople,
                    page =
                        Page(
                            formModel = NumberOfPeopleFormModel::class,
                            templateName = "forms/numberOfPeopleForm",
                            content =
                                mapOf(
                                    "title" to "registerProperty.title",
                                    "fieldSetHeading" to "forms.numberOfPeople.fieldSetHeading",
                                    "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
                                    "label" to "forms.numberOfPeople.label",
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
                                    "fieldSetHeading" to "forms.numberOfHouseholds.fieldSetHeading",
                                ),
                        ),
                ),
            ),
    ) {
    companion object {
        private fun occupancyNextAction(
            journeyData: JourneyData,
            subPage: Int?,
        ): Pair<RegisterPropertyStepId, Int?> =
            when (
                val propertyIsOccupied =
                    objectToStringKeyedMap(journeyData[RegisterPropertyStepId.Occupancy.urlPathSegment])
                        ?.get("occupied")
                        .toString()
            ) {
                "true" -> Pair(RegisterPropertyStepId.NumberOfHouseholds, subPage)
                "false" -> Pair(RegisterPropertyStepId.PlaceholderPage, null)
                else -> throw IllegalArgumentException(
                    "Invalid value for journeyData[\"${RegisterPropertyStepId.Occupancy.urlPathSegment}\"][\"occupied\"]:" +
                        propertyIsOccupied,
                )
            }
    }
}
