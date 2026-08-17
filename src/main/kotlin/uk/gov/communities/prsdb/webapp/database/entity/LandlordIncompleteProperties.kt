package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapsId
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.io.Serializable

@Entity
@Table(
    name = "landlord_incomplete_properties",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uc_landlord_incomplete_properties_saved_journey_state",
            columnNames = ["saved_journey_state_id"],
        ),
    ],
)
class LandlordIncompleteProperties() {
    @EmbeddedId
    lateinit var id: LandlordIncompletePropertiesId
        private set

    @MapsId("userId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: PrsdbUser
        private set

    @MapsId("savedJourneyStateId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "saved_journey_state_id", nullable = false)
    lateinit var savedJourneyState: SavedJourneyState
        private set

    constructor(user: PrsdbUser, savedJourneyState: SavedJourneyState) : this() {
        this.user = user
        this.savedJourneyState = savedJourneyState
        this.id = LandlordIncompletePropertiesId(user.id, savedJourneyState.id)
    }
}

@Embeddable
data class LandlordIncompletePropertiesId(
    @Column(name = "user_id")
    var userId: String = "",
    @Column(name = "saved_journey_state_id")
    var savedJourneyStateId: Long = 0L,
) : Serializable
