package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.springframework.data.annotation.LastModifiedDate
import java.io.Serializable
import java.time.OffsetDateTime

@MappedSuperclass
abstract class ModifiableAuditableEntity :
    AuditableEntity(),
    Serializable {
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false)
    lateinit var lastModifiedDate: OffsetDateTime
        private set
}
