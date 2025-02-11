package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.time.Instant

@MappedSuperclass
abstract class ModifiableAuditableEntity :
    AuditableEntity(),
    Serializable {
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false)
    var lastModifiedDate: Instant? = null
        private set

    fun getMostRecentlyUpdated(): Instant = lastModifiedDate ?: createdDate
}
