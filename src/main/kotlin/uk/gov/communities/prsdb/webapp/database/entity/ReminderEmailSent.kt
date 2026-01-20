package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.Check
import java.io.Serializable
import java.time.Instant

// It is expected that we will need to send reminder emails for entities such as PropertyCompliance as well as SavedJourneyState.
// We can add further columns and modify the check constraint to allow only one of SavedJourneyState or other entities to be non-null as required.
@Entity
@Check(constraints = "(saved_journey_state_id IS NOT NULL)")
class ReminderEmailSent() : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    lateinit var lastReminderEmailSentDate: Instant
        private set

    @OneToOne(optional = true)
    @JoinColumn(name = "saved_journey_state_id", nullable = true, unique = true)
    var savedJourneyState: SavedJourneyState? = null
        private set

    constructor(
        lastReminderEmailSentDate: Instant,
        savedJourneyState: SavedJourneyState,
    ) : this() {
        this.lastReminderEmailSentDate = lastReminderEmailSentDate
        this.savedJourneyState = savedJourneyState
    }
}
