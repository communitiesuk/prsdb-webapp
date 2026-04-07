package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import kotlinx.serialization.Serializable

@Serializable
data class CertificateUpload(
    val fileUploadId: Long,
    val fileName: String,
)
