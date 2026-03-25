package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.journeys.JourneyData

class CheckAnswersFormModel : FormModel {
    var storedJourneyData: JourneyData = emptyMap()

    companion object {
        fun serializeJourneyData(journeyData: JourneyData): String {
            val journeyDataWithStringValues = journeyData.mapValues { (_, value) -> value.toString() }
            return Json.encodeToString(journeyDataWithStringValues)
        }
    }
}
