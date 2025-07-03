package uk.gov.communities.prsdb.webapp.forms.steps.factories

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateJourney
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyRegistrationNumberOfPeoplePage
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsGroupIdentifier
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.GroupedJourneyExtensions.Companion.withBackUrlIfNotNullAndNotChangingAnswer
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getIsOccupiedUpdateIfPresent
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLatestNumberOfHouseholds
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getOriginalIsOccupied
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

/**
 * There are three occupancy update sub-journeys: updating occupancy, updating number of households, and updating number of people.
 * Each sub-journey involves the same steps, with differing IDs, content and flow.
 * The step IDs for each sub-journey share a group ID, which this factory uses to determine which steps to create.
 * Group IDs are also used to keep sub-journey data separate in the session.
 *
 * Some sub-journeys skip steps that aren't relevant. For example, the number of households sub-journey skips the occupancy step.
 * We want to submit a full set of sub-journey data during updates, so we load skipped step data into session during journey initialisation.
 * This factory determines which steps to skip based on the group ID.
 */
class PropertyDetailsUpdateJourneyStepFactory(
    stepName: String,
    private val isChangingAnswer: Boolean,
    private val propertyDetailsPath: String,
    private val journeyDataService: JourneyDataService,
) {
    val stepGroupId =
        UpdatePropertyDetailsStepId.fromPathSegment(stepName)?.groupIdentifier
            ?: throw IllegalArgumentException("Step: $stepName does not correspond to a UpdatePropertyDetailsStepId group identifier")

    val occupancyStepId = getOccupancyStepIdFor(stepGroupId)

    val numberOfHouseholdsStepId = getNumberOfHouseholdsStepIdFor(stepGroupId)

    val numberOfPeopleStepId = getNumberOfPeopleStepIdFor(stepGroupId)

    val checkOccupancyAnswersStepId = getCheckOccupancyAnswersStepIdFor(stepGroupId)

    val skippedStepIds =
        when (stepGroupId) {
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds ->
                listOf(occupancyStepId)
            UpdatePropertyDetailsGroupIdentifier.NumberOfPeople ->
                listOf(occupancyStepId, numberOfHouseholdsStepId)
            else ->
                emptyList()
        }

    fun createOccupancyStep() =
        Step(
            id = occupancyStepId,
            page =
                Page(
                    formModel = OccupancyFormModel::class,
                    templateName = "forms/propertyOccupancyForm",
                    content =
                        mapOf(
                            "title" to "propertyDetails.update.title",
                            "fieldSetHeading" to getOccupancyStepFieldSetHeading(),
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = true,
                                        valueStr = "yes",
                                        labelMsgKey = "forms.radios.option.yes.label",
                                        hintMsgKey = "forms.occupancy.radios.option.yes.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = false,
                                        valueStr = "no",
                                        labelMsgKey = "forms.radios.option.no.label",
                                        hintMsgKey = "forms.occupancy.radios.option.no.hint",
                                    ),
                                ),
                        ).withBackUrlIfNotNullAndNotChangingAnswer(propertyDetailsPath, isChangingAnswer),
                ),
            nextAction = { filteredJourneyData, _ -> occupancyNextAction(filteredJourneyData) },
            saveAfterSubmit = false,
        )

    fun createNumberOfHouseholdsStep() =
        when (stepGroupId) {
            UpdatePropertyDetailsGroupIdentifier.Occupancy ->
                createNumberOfHouseholdsStep(
                    fieldSetHeadingKey = "forms.numberOfHouseholds.fieldSetHeading",
                )
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds ->
                createNumberOfHouseholdsStep(
                    fieldSetHeadingKey = "forms.update.numberOfHouseholds.fieldSetHeading",
                    backUrl = propertyDetailsPath,
                )
            // UpdatePropertyDetailsGroupIdentifier.NumberOfPeople or default for any non-occupancy sub-journey group ID
            else ->
                createNumberOfHouseholdsStep(
                    fieldSetHeadingKey = "forms.update.numberOfHouseholds.fieldSetHeading",
                )
        }

    fun createNumberOfPeopleStep() =
        when (stepGroupId) {
            UpdatePropertyDetailsGroupIdentifier.Occupancy ->
                createNumberOfPeopleStep(
                    fieldSetHeadingKey = "forms.numberOfPeople.fieldSetHeading",
                )
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds ->
                createNumberOfPeopleStep(
                    fieldSetHeadingKey = "forms.update.numberOfPeople.fieldSetHeading",
                )
            // UpdatePropertyDetailsGroupIdentifier.NumberOfPeople or default for any non-occupancy sub-journey group ID
            else ->
                createNumberOfPeopleStep(
                    fieldSetHeadingKey = "forms.update.numberOfPeople.fieldSetHeading",
                    backUrl = propertyDetailsPath,
                )
        }

    private fun createNumberOfHouseholdsStep(
        fieldSetHeadingKey: String,
        backUrl: String? = null,
    ) = Step(
        id = numberOfHouseholdsStepId,
        page =
            Page(
                formModel = NumberOfHouseholdsFormModel::class,
                templateName = "forms/numberOfHouseholdsForm",
                content =
                    mapOf(
                        "title" to "propertyDetails.update.title",
                        "fieldSetHeading" to fieldSetHeadingKey,
                        "label" to "forms.numberOfHouseholds.label",
                    ).withBackUrlIfNotNullAndNotChangingAnswer(backUrl, isChangingAnswer),
            ),
        nextAction = { _, _ -> Pair(numberOfPeopleStepId, null) },
        saveAfterSubmit = false,
    )

    private fun createNumberOfPeopleStep(
        fieldSetHeadingKey: String,
        backUrl: String? = null,
    ) = Step(
        id = numberOfPeopleStepId,
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
                    ).withBackUrlIfNotNullAndNotChangingAnswer(backUrl, isChangingAnswer),
                latestNumberOfHouseholds = getLatestNumberOfHouseholds(),
            ),
        nextAction = { _, _ -> Pair(checkOccupancyAnswersStepId, null) },
        saveAfterSubmit = false,
    )

    private fun getOccupancyStepFieldSetHeading() =
        if (wasPropertyOriginallyOccupied()) {
            "forms.update.occupancy.occupied.fieldSetHeading"
        } else {
            "forms.occupancy.fieldSetHeading"
        }

    private fun wasPropertyOriginallyOccupied() =
        journeyDataService.getJourneyDataFromSession().getOriginalIsOccupied(
            stepGroupId,
            UpdateJourney.getOriginalJourneyDataKey(journeyDataService),
        )!!

    private fun occupancyNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getIsOccupiedUpdateIfPresent(stepGroupId)!!) {
            Pair(numberOfHouseholdsStepId, null)
        } else {
            Pair(checkOccupancyAnswersStepId, null)
        }

    private fun getLatestNumberOfHouseholds() =
        journeyDataService
            .getJourneyDataFromSession()
            .getLatestNumberOfHouseholds(stepGroupId, UpdateJourney.getOriginalJourneyDataKey(journeyDataService))

    companion object {
        fun getOccupancyStepIdFor(stepGroupId: UpdatePropertyDetailsGroupIdentifier) =
            when (stepGroupId) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.UpdateOccupancy
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.UpdateHouseholdsOccupancy
                // UpdatePropertyDetailsGroupIdentifier.NumberOfPeople or default for any non-occupancy sub-journey group ID
                else -> UpdatePropertyDetailsStepId.UpdatePeopleOccupancy
            }

        fun getNumberOfHouseholdsStepIdFor(stepGroupId: UpdatePropertyDetailsGroupIdentifier) =
            when (stepGroupId) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.UpdateOccupancyNumberOfHouseholds
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds
                UpdatePropertyDetailsGroupIdentifier.NumberOfPeople -> UpdatePropertyDetailsStepId.UpdatePeopleNumberOfHouseholds
                // UpdatePropertyDetailsGroupIdentifier.NumberOfPeople or default for any non-occupancy sub-journey group ID
                else -> UpdatePropertyDetailsStepId.UpdatePeopleNumberOfHouseholds
            }

        fun getNumberOfPeopleStepIdFor(stepGroupId: UpdatePropertyDetailsGroupIdentifier) =
            when (stepGroupId) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.UpdateOccupancyNumberOfPeople
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.UpdateHouseholdsNumberOfPeople
                UpdatePropertyDetailsGroupIdentifier.NumberOfPeople -> UpdatePropertyDetailsStepId.UpdateNumberOfPeople
                // UpdatePropertyDetailsGroupIdentifier.NumberOfPeople or default for any non-occupancy sub-journey group ID
                else -> UpdatePropertyDetailsStepId.UpdateNumberOfPeople
            }

        private fun getCheckOccupancyAnswersStepIdFor(stepGroupId: UpdatePropertyDetailsGroupIdentifier) =
            when (stepGroupId) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.CheckYourHouseholdsAnswers
                // UpdatePropertyDetailsGroupIdentifier.NumberOfPeople or default for any non-occupancy sub-journey group ID
                else -> UpdatePropertyDetailsStepId.CheckYourPeopleAnswers
            }
    }
}
