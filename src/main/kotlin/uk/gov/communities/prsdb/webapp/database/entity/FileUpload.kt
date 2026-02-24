package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "uc_fileupload_s3_object", columnNames = ["objectKey", "eTag", "versionId"])])
class FileUpload() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var status: FileUploadStatus

    @Column(nullable = false)
    lateinit var objectKey: String
        private set

    @Column(nullable = false)
    lateinit var eTag: String

    var versionId: String? = null

    @Column(nullable = false)
    lateinit var extension: String
        private set

    constructor(status: FileUploadStatus, objectKey: String, extension: String, eTag: String, versionId: String?) : this() {
        this.status = status
        this.objectKey = objectKey
        this.eTag = eTag
        this.versionId = versionId
        this.extension = extension
    }
}
