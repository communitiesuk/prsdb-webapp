package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException

@Entity
class VirusScanCallback() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false)
    lateinit var encodedCallbackData: String

    @ManyToOne(optional = false)
    @JoinColumn(name = "file_upload_id", nullable = false, unique = false)
    lateinit var fileUpload: FileUpload
        private set

    constructor(upload: FileUpload, encodedCallbackData: String) : this() {
        this.fileUpload = upload
        this.encodedCallbackData = encodedCallbackData
    }

    companion object {
        fun List<VirusScanCallback>.extractFileUpload(): FileUpload {
            if (isEmpty()) {
                throw PrsdbWebException("No virus scan callbacks provided to extract file upload from.")
            }
            return this.fold(first().fileUpload) { upload: FileUpload, callback: VirusScanCallback ->
                if (upload.id != callback.fileUpload.id) {
                    throw PrsdbWebException("Inconsistent file uploads for callbacks: ${upload.id} and ${callback.fileUpload.id}")
                } else {
                    upload
                }
            }
        }
    }
}
