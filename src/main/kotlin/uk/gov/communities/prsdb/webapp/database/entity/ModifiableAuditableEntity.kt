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
    @Column
    var lastModifiedDate: Instant = createdDate
        private set
}
