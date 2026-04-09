package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import java.time.Instant
import kotlin.time.Duration.Companion.days

class CompleteByDateHelper {
    companion object {
        fun getIncompletePropertyCompleteByDateFromSavedJourneyState(savedJourneyState: SavedJourneyState): LocalDate =
            getIncompletePropertyCompleteByDateFromCreatedDate(savedJourneyState.createdDate)

        fun getIncompletePropertyCompleteByDateFromCreatedDate(createdDate: Instant): LocalDate =
            DateTimeHelper.getDateInUK(createdDate.toKotlinInstant().plus(28.days))
    }
}
