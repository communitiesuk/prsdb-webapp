package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import kotlin.time.Duration.Companion.days

class CompleteByDateHelper {
    companion object {
        fun getIncompletePropertyCompleteByDate(savedJourneyState: SavedJourneyState): LocalDate =
            DateTimeHelper.getDateInUK(savedJourneyState.createdDate.toKotlinInstant().plus(28.days))
    }
}
