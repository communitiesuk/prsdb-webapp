package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

data class IncompletePropertyForReminderDataModel(
    val landlordEmail: String,
    val propertySingleLineAddress: String,
    val completeByDate: LocalDate,
    val savedJourneyState: SavedJourneyState,
)
