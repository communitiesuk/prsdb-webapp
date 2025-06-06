package uk.gov.communities.prsdb.webapp.forms.steps.factories

import uk.gov.communities.prsdb.webapp.forms.journeys.UpdateJourney
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PropertyRegistrationNumberOfPeoplePage
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsGroupIdentifier
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.GroupedUpdateJourneyExtensions.Companion.withBackUrlIfNotChangingAnswer
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyDetailsUpdateJourneyExtensions.Companion.getLatestNumberOfHouseholds
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class PropertyDetailsUpdateJourneyStepFactory(
    private val stepName: String,
    private val isChangingAnswer: Boolean,
    private val propertyDetailsPath: String,
    private val journeyDataService: JourneyDataService,
) {
    fun getOccupancyStepId() = getOccupancyStepIdFor(stepName)

    fun getNumberOfHouseholdsStepId() = getNumberOfHouseholdsStepIdFor(stepName)

    fun getNumberOfPeopleStepId() = getNumberOfPeopleStepIdFor(stepName)

    fun getCheckOccupancyAnswersStepId() = getCheckOccupancyAnswersStepIdFor(stepName)

    fun createNumberOfHouseholdsStep() =
        when (getStepGroupIdFor(stepName)) {
            UpdatePropertyDetailsGroupIdentifier.Occupancy ->
                createNumberOfHouseholdsStep(
                    fieldSetHeadingKey = "forms.numberOfHouseholds.fieldSetHeading",
                )
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds ->
                createNumberOfHouseholdsStep(
                    fieldSetHeadingKey = "forms.update.numberOfHouseholds.fieldSetHeading",
                    backUrl = propertyDetailsPath,
                )
            else ->
                createNumberOfHouseholdsStep(
                    fieldSetHeadingKey = "forms.update.numberOfHouseholds.fieldSetHeading",
                )
        }

    fun createNumberOfPeopleStep() =
        when (getStepGroupIdFor(stepName)) {
            UpdatePropertyDetailsGroupIdentifier.Occupancy ->
                createNumberOfPeopleStep(
                    fieldSetHeadingKey = "forms.numberOfPeople.fieldSetHeading",
                )
            UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds ->
                createNumberOfPeopleStep(
                    fieldSetHeadingKey = "forms.update.numberOfPeople.fieldSetHeading",
                )
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
        id = getNumberOfHouseholdsStepId(),
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
        nextAction = { _, _ -> Pair(getNumberOfPeopleStepId(), null) },
        saveAfterSubmit = false,
    )

    private fun createNumberOfPeopleStep(
        fieldSetHeadingKey: String,
        backUrl: String? = null,
    ) = Step(
        id = getNumberOfPeopleStepId(),
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
                latestNumberOfHouseholds = getLatestNumberOfHouseholds(),
            ),
        nextAction = { _, _ -> Pair(getCheckOccupancyAnswersStepId(), null) },
        saveAfterSubmit = false,
    )

    private fun getLatestNumberOfHouseholds() =
        journeyDataService
            .getJourneyDataFromSession()
            .getLatestNumberOfHouseholds(getNumberOfHouseholdsStepId(), UpdateJourney.getOriginalJourneyDataKey(journeyDataService))

    companion object {
        fun getStepGroupIdFor(stepName: String) =
            UpdatePropertyDetailsStepId.fromPathSegment(stepName)?.groupIdentifier
                ?: throw IllegalArgumentException("Step: $stepName does not correspond to a UpdatePropertyDetailsStepId group identifier")

        fun getOccupancyStepIdFor(stepName: String) =
            when (getStepGroupIdFor(stepName)) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.UpdateOccupancy
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.UpdateHouseholdsOccupancy
                else -> UpdatePropertyDetailsStepId.UpdatePeopleOccupancy
            }

        private fun getNumberOfHouseholdsStepIdFor(stepName: String) =
            when (getStepGroupIdFor(stepName)) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.UpdateOccupancyNumberOfHouseholds
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds
                else -> UpdatePropertyDetailsStepId.UpdatePeopleNumberOfHouseholds
            }

        private fun getNumberOfPeopleStepIdFor(stepName: String) =
            when (getStepGroupIdFor(stepName)) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.UpdateOccupancyNumberOfPeople
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.UpdateHouseholdsNumberOfPeople
                else -> UpdatePropertyDetailsStepId.UpdateNumberOfPeople
            }

        private fun getCheckOccupancyAnswersStepIdFor(stepName: String) =
            when (getStepGroupIdFor(stepName)) {
                UpdatePropertyDetailsGroupIdentifier.Occupancy -> UpdatePropertyDetailsStepId.CheckYourOccupancyAnswers
                UpdatePropertyDetailsGroupIdentifier.NumberOfHouseholds -> UpdatePropertyDetailsStepId.CheckYourHouseholdsAnswers
                else -> UpdatePropertyDetailsStepId.CheckYourPeopleAnswers
            }
    }
}
