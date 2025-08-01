package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "uniqueS3ObjectConstraint", columnNames = ["objectKey", "eTag", "versionId"])])
class FileUpload() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    lateinit var status: FileUploadStatus

    lateinit var objectKey: String
        private set

    var eTag: String? = null
        private set

    var versionId: String? = null
        private set

    lateinit var extension: String
        private set

    constructor(status: FileUploadStatus, objectKey: String, extension: String) : this() {
        this.status = status
        this.objectKey = objectKey
        this.extension = extension
    }

    constructor(status: FileUploadStatus, objectKey: String, extension: String, eTag: String, versionId: String?) : this() {
        this.status = status
        this.objectKey = objectKey
        this.eTag = eTag
        this.versionId = versionId
        this.extension = extension
    }
}
