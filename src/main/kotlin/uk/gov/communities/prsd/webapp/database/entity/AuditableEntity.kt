package uk.gov.communities.prsd.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.springframework.data.annotation.CreatedDate
import java.io.Serializable
import java.util.Date

@MappedSuperclass
abstract class AuditableEntity : Serializable {
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    var createdDate: Date? = null
        private set

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(insertable = false)
    var lastModifiedDate: Date? = null
        private set
}
