package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class EpcDataModel(
    val certificateNumber: String,
    val singleLineAddress: String,
    val energyRating: String,
    // TODO PRSD-1132 - design looks like it has a certifcate date but this might not be returned, is the expiryDate ok and does the design need modifying?
    val expiryDate: LocalDate,
    val latestCertificateNumberForThisProperty: String? = null,
) {
    fun isLatestCertificateForThisProperty() = certificateNumber == latestCertificateNumberForThisProperty

    companion object {
        fun formatCertificateNumber(certificateNumber: String): String {
            val certNumberNoHyphens = certificateNumber.replace("-", "")
            require(certNumberNoHyphens.all { it.isDigit() }) { "Input must contain only digits and hyphens" }
            require(certNumberNoHyphens.length == 20) { "Input must contain exactly 20 digits" }

            return certNumberNoHyphens
                .chunked(4)
                .joinToString("-")
        }
    }
}
