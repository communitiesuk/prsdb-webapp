package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import uk.gov.communities.prsdb.webapp.constants.enums.RemindableEntityType
import java.io.Serializable
import java.time.Instant

@Entity
class ReminderEmailSent() : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    lateinit var lastReminderEmailSentDate: Instant
        private set

    @Column(nullable = false)
    lateinit var entityType: RemindableEntityType

    @Column(nullable = false)
    var entityId: Long = 0

    constructor(
        lastEmailSentDate: Instant,
        entityType: RemindableEntityType,
        entityId: Long,
    ) : this() {
        this.lastReminderEmailSentDate = lastEmailSentDate
        this.entityType = entityType
        this.entityId = entityId
    }
}
