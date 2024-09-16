package uk.gov.communities.prsd.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import java.util.Date

@Entity
class Landlord : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @Column(nullable = false)
    var phoneNumber: String? = null
        private set

    @Column(nullable = false)
    var dateOfBirth: Date? = null
        private set

    @OneToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false, foreignKey = ForeignKey(name = "FK_LANDLORD_1L_USER"))
    var baseUser: OneLoginUser? = null
}
