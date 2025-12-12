package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

@Suppress("ktlint:standard:function-naming")
interface SavedJourneyStateRepository : JpaRepository<SavedJourneyState, Long> {
    fun findByJourneyIdAndUser_Id(
        journeyId: String,
        principalName: String,
    ): SavedJourneyState?

    fun findByIdAndUser_Id(
        id: Long,
        principalName: String,
    ): SavedJourneyState?

    fun deleteByJourneyIdAndUser_Id(
        journeyId: String,
        principalName: String,
    )
}
