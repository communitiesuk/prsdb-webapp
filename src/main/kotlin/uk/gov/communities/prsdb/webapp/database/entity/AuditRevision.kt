package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp
import uk.gov.communities.prsdb.webapp.database.audit.AuditRevisionListener
import java.time.Instant

@Entity
@RevisionEntity(AuditRevisionListener::class)
class AuditRevision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    val id: Long = 0

    @RevisionTimestamp
    @Column(nullable = false)
    lateinit var createdDate: Instant
        private set

    var userSubjectIdentifier: String? = null

    var lettingAgentAccessId: Long? = null

    var taskName: String? = null
}
