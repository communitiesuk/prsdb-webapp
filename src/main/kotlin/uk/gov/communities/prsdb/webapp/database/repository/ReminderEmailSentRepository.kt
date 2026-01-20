package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState

interface ReminderEmailSentRepository : JpaRepository<ReminderEmailSent, Long> {
    @Suppress("ktlint:standard:function-naming")
    fun findBySavedJourneyStateIn(savedJourneyStates: List<SavedJourneyState>): List<ReminderEmailSent>
}
