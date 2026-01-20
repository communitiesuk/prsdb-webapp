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

    @MapsId("landlordId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "landlord_id", nullable = false)
    lateinit var landlord: Landlord
        private set

    @MapsId("savedJourneyStateId")
    @ManyToOne(optional = false)
    @JoinColumn(name = "saved_journey_state_id", nullable = false)
    lateinit var savedJourneyState: SavedJourneyState
        private set

    constructor(landlord: Landlord, savedJourneyState: SavedJourneyState) : this() {
        this.landlord = landlord
        this.savedJourneyState = savedJourneyState
        this.id = LandlordIncompletePropertiesId(landlord.id, savedJourneyState.id)
    }
}

@Embeddable
data class LandlordIncompletePropertiesId(
    @Column(name = "landlord_id")
    var landlordId: Long = 0L,
    @Column(name = "saved_journey_state_id")
    var savedJourneyStateId: Long = 0L,
) : Serializable
