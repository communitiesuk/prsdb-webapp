package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate

data class IncompletePropertiesDataModel(
    val journeyId: String,
    val completeByDate: LocalDate,
    val singleLineAddress: String,
)
