package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.journeys.FormData
import java.security.MessageDigest
import java.util.Base64

class CheckAnswersFormModel : FormModel {
    companion object {
        fun serializeJourneyDataHashes(journeyData: FormData): String {
            val journeyDataHashes = journeyData.mapValues { (_, value) -> sha256(value.toString()) }
            return Json.encodeToString(journeyDataHashes)
        }

        fun deserializeJourneyDataKeys(serializedJourneyData: String): Set<String> =
            Json.decodeFromString<Map<String, String>>(serializedJourneyData).keys

        private fun sha256(value: String): String =
            Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.toByteArray()))
    }
}
