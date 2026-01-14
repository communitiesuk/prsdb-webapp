package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate

data class IncompletePropertiesForReminderDataModel(
    val landlordEmail: String,
    val propertySingleLineAddress: String,
    val completeByDate: LocalDate,
    val savedJourneyStateId: String,
)
