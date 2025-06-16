package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class CheckAnswersFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "_",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isFilteredJourneyDataUnchanged",
            ),
        ],
    )
    var submittedFilteredJourneyData: String = "{}"

    var storedJourneyData: JourneyData = emptyMap()

    fun isFilteredJourneyDataUnchanged(): Boolean {
        val submittedFilteredJourneyDataWithStringValues = deserializeJourneyData(submittedFilteredJourneyData)
        return submittedFilteredJourneyDataWithStringValues.all { it.value == storedJourneyData[it.key].toString() }
    }

    companion object {
        fun serializeJourneyData(journeyData: JourneyData): String {
            val journeyDataWithStringValues = journeyData.mapValues { (_, value) -> value.toString() }
            return Json.encodeToString(journeyDataWithStringValues)
        }

        private fun deserializeJourneyData(serializedJourneyData: String): Map<String, String> =
            Json.decodeFromString(serializedJourneyData)
    }
}
