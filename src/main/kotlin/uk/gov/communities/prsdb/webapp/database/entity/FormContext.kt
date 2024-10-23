package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType

@Entity
class FormContext : AuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @Column(nullable = false)
    lateinit var journeyType: JourneyType
        private set

    @Column(columnDefinition = "TEXT", nullable = false)
    lateinit var context: String
        private set

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "subject_identifier",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_FORM_CONTEXT_1L_USER"),
    )
    lateinit var user: OneLoginUser
        private set
}
