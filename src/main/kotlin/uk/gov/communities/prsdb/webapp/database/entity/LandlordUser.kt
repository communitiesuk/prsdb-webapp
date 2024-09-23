package uk.gov.communities.prsdb.webapp.database.entity

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
class LandlordUser : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @Column(nullable = false)
    lateinit var phoneNumber: String
        private set

    @Column(nullable = false)
    lateinit var dateOfBirth: Date
        private set

    @OneToOne(optional = false)
    @JoinColumn(name = "subject_identifier", nullable = false, foreignKey = ForeignKey(name = "FK_LANDLORD_1L_USER"))
    lateinit var baseUser: OneLoginUser
        private set
}
