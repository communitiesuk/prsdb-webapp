package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.journeys.FormData

class CheckAnswersFormModel : FormModel {
    companion object {
        fun serializeJourneyData(journeyData: FormData): String {
            val journeyDataWithStringValues = journeyData.mapValues { (_, value) -> value.toString() }
            return Json.encodeToString(journeyDataWithStringValues)
        }
    }
}
