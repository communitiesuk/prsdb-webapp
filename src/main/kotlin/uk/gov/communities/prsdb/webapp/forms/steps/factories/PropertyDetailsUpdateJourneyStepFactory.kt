package uk.gov.communities.prsdb.webapp.forms.steps.factories

import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyRegistrationNumberOfPeoplePage
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsGroupIdentifier
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.GroupedUpdateJourneyExtensions.Companion.withBackUrlIfNotChangingAnswer
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel

class PropertyDetailsUpdateJourneyStepFactory(
    stepName: String,
    private val isChangingAnswer: Boolean,
    private val propertyDetailsPath: String,
) {
    private val stepGroupId = UpdatePropertyDetailsStepId.fromPathSegment(stepName)!!.groupIdentifier

    fun createNumberOfHouseholdsStep() =
        when (stepGroupId) {
            UpdatePropertyDetailsGroupIdentifier.Occupancy ->
                createNumberOfHouseholdsStep(
                    stepId = UpdatePropertyDetailsStepId.UpdateOccupancyNumberOfHouseholds,
                    fieldSetHeadingKey = "forms.numberOfHouseholds.fieldSetHeading",
                    nextActionStepId = UpdatePropertyDetailsStepId.UpdateOccupancyNumberOfPeople,
                )
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds ->
                createNumberOfHouseholdsStep(
                    stepId = UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds,
                    fieldSetHeadingKey = "forms.update.numberOfHouseholds.fieldSetHeading",
                    nextActionStepId = UpdatePropertyDetailsStepId.UpdateHouseholdsNumberOfPeople,
                    backUrl = propertyDetailsPath,
                )
            else ->
                createNumberOfHouseholdsStep(
                    stepId = UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds,
                    fieldSetHeadingKey = "forms.update.numberOfHouseholds.fieldSetHeading",
                    nextActionStepId = UpdatePropertyDetailsStepId.UpdateNumberOfPeople,
                )
        }

    fun createNumberOfPeopleStep(latestNumberOfHouseholds: Int) =
        when (stepGroupId) {
            UpdatePropertyDetailsGroupIdentifier.Occupancy ->
                createNumberOfPeopleStep(
                    stepId = UpdatePropertyDetailsStepId.UpdateOccupancyNumberOfPeople,
                    fieldSetHeadingKey = "forms.numberOfPeople.fieldSetHeading",
                    latestNumberOfHouseholds = latestNumberOfHouseholds,
                    nextActionStepId = UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers,
                )
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds ->
                createNumberOfPeopleStep(
                    stepId = UpdatePropertyDetailsStepId.UpdateHouseholdsNumberOfPeople,
                    fieldSetHeadingKey = "forms.update.numberOfPeople.fieldSetHeading",
                    latestNumberOfHouseholds = latestNumberOfHouseholds,
                    nextActionStepId = UpdatePropertyDetailsStepId.CheckYourHouseholdsAnswers,
                )
            else ->
                createNumberOfPeopleStep(
                    stepId = UpdatePropertyDetailsStepId.UpdateNumberOfPeople,
                    fieldSetHeadingKey = "forms.update.numberOfPeople.fieldSetHeading",
                    latestNumberOfHouseholds = latestNumberOfHouseholds,
                    nextActionStepId = UpdatePropertyDetailsStepId.CheckYourPeopleAnswers,
                    backUrl = propertyDetailsPath,
                )
        }

    fun getCheckOccupancyAnswersStepId() =
        when (stepGroupId) {
            UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.CheckYourHouseholdsAnswers
            else -> UpdatePropertyDetailsStepId.CheckYourPeopleAnswers
        }

    private fun createNumberOfHouseholdsStep(
        stepId: UpdatePropertyDetailsStepId,
        fieldSetHeadingKey: String,
        nextActionStepId: UpdatePropertyDetailsStepId,
        backUrl: String? = null,
    ) = Step(
        id = stepId,
        page =
            Page(
                formModel = NumberOfHouseholdsFormModel::class,
                templateName = "forms/numberOfHouseholdsForm",
                content =
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "fieldSetHeading" to fieldSetHeadingKey,
                        "label" to "forms.numberOfHouseholds.label",
                    ).withBackUrlIfNotChangingAnswer(backUrl, isChangingAnswer),
            ),
        nextAction = { _, _ -> Pair(nextActionStepId, null) },
        saveAfterSubmit = false,
    )

    private fun createNumberOfPeopleStep(
        stepId: UpdatePropertyDetailsStepId,
        fieldSetHeadingKey: String,
        latestNumberOfHouseholds: Int,
        nextActionStepId: UpdatePropertyDetailsStepId,
        backUrl: String? = null,
    ) = Step(
        id = stepId,
        page =
            PropertyRegistrationNumberOfPeoplePage(
                formModel = NumberOfPeopleFormModel::class,
                templateName = "forms/numberOfPeopleForm",
                content =
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "fieldSetHeading" to fieldSetHeadingKey,
                        "fieldSetHint" to "forms.numberOfPeople.fieldSetHint",
                        "label" to "forms.numberOfPeople.label",
                    ).withBackUrlIfNotChangingAnswer(backUrl, isChangingAnswer),
                latestNumberOfHouseholds = latestNumberOfHouseholds,
            ),
        nextAction = { _, _ -> Pair(nextActionStepId, null) },
        saveAfterSubmit = false,
    )
}
