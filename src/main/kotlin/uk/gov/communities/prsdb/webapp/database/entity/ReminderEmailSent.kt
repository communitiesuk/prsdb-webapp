package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
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

    constructor(
        lastReminderEmailSentDate: Instant,
    ) : this() {
        this.lastReminderEmailSentDate = lastReminderEmailSentDate
    }
}
