package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "uc_userJourney", columnNames = ["journey_id", "subject_identifier"])])
class SavedJourneyState() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var journeyId: String

    @Column(columnDefinition = "TEXT", nullable = false)
    lateinit var serializedState: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false)
    lateinit var user: OneLoginUser
        private set

    constructor(serializedState: String, user: OneLoginUser, journeyId: String) : this() {
        this.serializedState = serializedState
        this.user = user
        this.journeyId = journeyId
    }

    constructor(user: OneLoginUser, journeyId: String) : this(serializedState = "{}", user, journeyId)
}
