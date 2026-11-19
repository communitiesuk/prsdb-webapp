package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.time.Instant

@MappedSuperclass
@Audited
abstract class ModifiableAuditableEntity :
    AuditableEntity(),
    Serializable {
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false)
    // Envers would copy insertable = false and always store null; audit_revision.created_date records when each change happened
    @NotAudited
    open var lastModifiedDate: Instant? = null
        protected set

    fun getMostRecentlyUpdated(): Instant = lastModifiedDate ?: createdDate
}
