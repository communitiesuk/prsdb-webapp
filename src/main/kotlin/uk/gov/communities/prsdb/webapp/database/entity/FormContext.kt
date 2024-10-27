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
class FormContext(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val journeyType: JourneyType,
    @Column(columnDefinition = "TEXT", nullable = false)
    var context: String,
    @ManyToOne(optional = false)
    @JoinColumn(
        name = "subject_identifier",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_FORM_CONTEXT_1L_USER"),
    )
    val user: OneLoginUser,
) : AuditableEntity()
