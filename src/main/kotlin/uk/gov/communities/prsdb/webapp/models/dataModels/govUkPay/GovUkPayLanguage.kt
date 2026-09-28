package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

import com.fasterxml.jackson.annotation.JsonValue

enum class GovUkPayLanguage(
    @get:JsonValue val value: String,
) {
    EN("en"),
    CY("cy"),
}
