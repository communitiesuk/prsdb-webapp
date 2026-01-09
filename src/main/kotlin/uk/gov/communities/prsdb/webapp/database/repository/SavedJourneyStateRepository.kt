package uk.gov.communities.prsdb.webapp.database.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

@Suppress("ktlint:standard:function-naming")
interface SavedJourneyStateRepository : JpaRepository<SavedJourneyState, Long> {
    fun findByJourneyIdAndUser_Id(
        journeyId: String,
        principalName: String,
    ): SavedJourneyState?

    fun existsByJourneyIdAndUser_Id(
        journeyId: String,
        principalName: String,
    ): Boolean

    fun findByIdAndUser_Id(
        id: Long,
        principalName: String,
    ): SavedJourneyState?

    @Transactional
    fun deleteByJourneyIdAndUser_Id(
        journeyId: String,
        principalName: String,
    )
}
