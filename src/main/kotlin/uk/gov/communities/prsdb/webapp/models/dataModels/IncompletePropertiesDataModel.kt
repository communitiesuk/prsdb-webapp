package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate

data class IncompletePropertiesDataModel(
    val contextId: Long,
    val completeByDate: LocalDate,
    val singleLineAddress: String,
    val localAuthorityName: String,
)
