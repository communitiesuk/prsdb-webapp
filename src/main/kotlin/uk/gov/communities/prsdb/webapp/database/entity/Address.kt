package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : ModifiableAuditableEntity() {
    @Column(nullable = false, unique = true)
    var uprn: Long = 0

    @Column(nullable = false)
    var singleLineAddress: String = ""

    @Column(nullable = false)
    var organisation: String = ""

    @Column(nullable = false)
    var subBuilding: String = ""

    @Column(nullable = false)
    var buildingName: String = ""

    @Column(nullable = false)
    var buildingNumber: String = ""

    @Column(nullable = false)
    var streetName: String = ""

    @Column(nullable = false)
    var locality: String = ""

    @Column(nullable = false)
    var townName: String = ""

    @Column(nullable = false)
    var postcode: String = ""

    // The data model just has the custodian code here, but does it make more sense to link it straight to the LA table?
    @OneToOne(optional = false)
    @JoinColumn(name = "local_authority_custodian_code", nullable = false, foreignKey = ForeignKey(name = "FK_ADDRESS_LOCAL_AUTHORITY"))
    lateinit var localAuthority: LocalAuthority
        private set
}
