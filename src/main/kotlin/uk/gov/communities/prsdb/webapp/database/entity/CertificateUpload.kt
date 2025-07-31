package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo

@Entity
open class CertificateUpload() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    lateinit var category: PropertyFileNameInfo.FileCategory

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_ownership_id")
    lateinit var propertyOwnership: PropertyOwnership
        private set

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "file_upload_id")
    lateinit var fileUpload: FileUpload
        private set

    constructor(upload: FileUpload, category: PropertyFileNameInfo.FileCategory, propertyOwnership: PropertyOwnership) : this() {
        this.category = category
        this.fileUpload = upload
        this.propertyOwnership = propertyOwnership
    }
}
