package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity(name = "landlord_with_listed_property_count")
class LandlordWithListedPropertyCount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val landlordId: Long = 0,
) {
    @OneToOne(optional = false)
    @JoinColumn(name = "landlord_id", referencedColumnName = "id")
    lateinit var landlord: Landlord
        private set

    var listedPropertyCount: Int = 0
}
