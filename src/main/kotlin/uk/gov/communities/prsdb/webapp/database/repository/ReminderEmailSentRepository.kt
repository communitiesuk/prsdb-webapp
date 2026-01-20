package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.constants.enums.RemindableEntityType
import uk.gov.communities.prsdb.webapp.database.entity.ReminderEmailSent

interface ReminderEmailSentRepository : JpaRepository<ReminderEmailSent, Long> {
    fun findByEntityTypeAndEntityIdIn(
        entityType: RemindableEntityType,
        entityIds: List<Long>,
    ): List<ReminderEmailSent>
}
