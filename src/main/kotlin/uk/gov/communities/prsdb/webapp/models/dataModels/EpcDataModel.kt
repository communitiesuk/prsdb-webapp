package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.json.JSONObject
import uk.gov.communities.prsdb.webapp.constants.VIEW_EPC_CERTIFICATE_BASE_URL
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.util.Locale

@Serializable
data class EpcDataModel(
    val certificateNumber: String,
    val singleLineAddress: String,
    val energyRating: String,
    val expiryDate: LocalDate,
    val latestCertificateNumberForThisProperty: String? = null,
) {
    val energyRatingUppercase: String
        get() = energyRating.uppercase(Locale.getDefault())

    val expiryDateAsString: String
        get() = DateTimeHelper.formatLocalDate(expiryDate)

    fun isLatestCertificateForThisProperty() = certificateNumber == latestCertificateNumberForThisProperty

    fun getEpcCertificateUrl() = "${VIEW_EPC_CERTIFICATE_BASE_URL}/${parseCertificateNumberOrNull(certificateNumber)}"

    fun isPastExpiryDate(): Boolean = expiryDate < DateTimeHelper().getCurrentDateInUK()

    fun isEnergyRatingEOrBetter(): Boolean = energyRatingUppercase in "A".."E"

    companion object {
        fun parseCertificateNumberOrNull(certificateNumber: String): String? {
            val certNumberNoHyphens = certificateNumber.replace("-", "")
            if (!(certNumberNoHyphens.all { it.isDigit() }) || certNumberNoHyphens.length != 20) {
                // Certificate number should be of the form XXXX-XXXX-XXXX-XXXX-XXXX
                return null
            }

            return certNumberNoHyphens
                .chunked(4)
                .joinToString("-")
        }

        fun fromJsonObject(jsonResponse: JSONObject): EpcDataModel {
            val epcData = jsonResponse.getJSONObject("data")
            val epcDataAddress = epcData.getJSONObject("address")
            val singleLineAddress =
                AddressDataModel.manualAddressDataToSingleLineAddress(
                    addressLineOne = epcDataAddress.getString("addressLine1"),
                    townOrCity = epcDataAddress.getString("town"),
                    postcode = epcDataAddress.getString("postcode"),
                    addressLineTwo = epcDataAddress.optString("addressLine2"),
                )

            return EpcDataModel(
                certificateNumber = epcData.getString("epcRrn"),
                singleLineAddress = singleLineAddress,
                energyRating = epcData.getString("currentEnergyEfficiencyBand"),
                expiryDate = DateTimeHelper.getDateInUK(epcData.getString("expiryDate")),
                latestCertificateNumberForThisProperty = epcData.getString("latestEpcRrnForAddress"),
            )
        }
    }
}
