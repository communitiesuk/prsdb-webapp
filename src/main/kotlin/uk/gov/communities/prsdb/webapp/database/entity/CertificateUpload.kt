package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.enums.FileCategory

@Entity
open class CertificateUpload() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    lateinit var category: FileCategory

    @ManyToOne
    @JoinColumn(name = "property_ownership_id")
    lateinit var propertyOwnership: PropertyOwnership
        private set

    @OneToOne
    @JoinColumn(name = "file_upload_id")
    lateinit var fileUpload: FileUpload
        private set

    constructor(upload: FileUpload, category: FileCategory, propertyOwnership: PropertyOwnership) : this() {
        this.category = category
        this.fileUpload = upload
        this.propertyOwnership = propertyOwnership
    }
}
