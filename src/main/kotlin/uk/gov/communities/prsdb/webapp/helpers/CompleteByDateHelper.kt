package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import java.time.Instant

class CompleteByDateHelper {
    companion object {
        fun getIncompletePropertyCompleteByDateFromSavedJourneyState(savedJourneyState: SavedJourneyState): LocalDate =
            getIncompletePropertyCompleteByDateFromCreatedDate(savedJourneyState.createdDate)

        fun getIncompletePropertyCompleteByDateFromCreatedDate(createdDate: Instant): LocalDate =
            DateTimeHelper.get28DaysFromDate(DateTimeHelper.getDateInUK(createdDate.toKotlinInstant()))
    }
}
