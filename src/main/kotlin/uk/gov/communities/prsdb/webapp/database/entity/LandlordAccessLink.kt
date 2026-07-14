package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "landlord_access_link",
    uniqueConstraints = [UniqueConstraint(columnNames = ["landlord_id", "subject_identifier"])],
)
class LandlordAccessLink() : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(optional = false)
    @JoinColumn(name = "landlord_id", nullable = false)
    lateinit var landlord: Landlord

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false)
    lateinit var baseUser: PrsdbUser

    constructor(landlord: Landlord, baseUser: PrsdbUser) : this() {
        this.landlord = landlord
        this.baseUser = baseUser
    }
}
