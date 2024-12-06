package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Address : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @Column(unique = true)
    var uprn: Long? = null
        private set

    @Column(nullable = false)
    lateinit var singleLineAddress: String
        private set

    lateinit var organisation: String
        private set

    lateinit var subBuilding: String
        private set

    lateinit var buildingName: String
        private set

    lateinit var buildingNumber: String
        private set

    lateinit var streetName: String
        private set

    lateinit var locality: String
        private set

    lateinit var townName: String
        private set

    lateinit var postcode: String
        private set

    lateinit var custodianCode: String
        private set
}
