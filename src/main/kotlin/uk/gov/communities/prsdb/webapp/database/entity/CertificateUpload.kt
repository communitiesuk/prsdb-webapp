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
    open val id: Long = 0

    open lateinit var category: FileCategory

    @ManyToOne
    @JoinColumn(name = "property_ownership_id")
    open lateinit var propertyOwnership: PropertyOwnership
        protected set

    @OneToOne
    @JoinColumn(name = "file_upload_id")
    open lateinit var fileUpload: FileUpload
        protected set

    constructor(upload: FileUpload, category: FileCategory, propertyOwnership: PropertyOwnership) : this() {
        this.category = category
        this.fileUpload = upload
        this.propertyOwnership = propertyOwnership
    }
}
