package uk.gov.communities.prsdb.webapp.services

import org.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

@Service
class EpcLookupService(
    private val epcRegisterClient: EpcRegisterClient,
) {
    fun getEpcByCertificateNumber(certificateNumber: String): EpcDataModel? {
        val formattedCertificateNumber = EpcDataModel.formatCertificateNumber(certificateNumber)
        val response = epcRegisterClient.getByRrn(formattedCertificateNumber)
        val jsonResponse = JSONObject(response)

        if (jsonResponse.has("errors")) {
            val errorCode = getErrorCode(jsonResponse)
            if (errorCode == "NOT_FOUND") {
                return null
            }
            if (errorCode == "INVALID_REQUEST" || errorCode == "BAD_REQUEST") {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, getErrorMessage(jsonResponse))
            }
        }

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
            expiryDate = DateTimeHelper.getDateInUKFromDateString(epcData.getString("expiryDate")),
            latestCertificateNumberForThisProperty = epcData.getString("latestEpcRrnForAddress"),
        )
    }

    companion object {
        fun getErrorCode(jsonObject: JSONObject): String? {
            val errors = jsonObject.getJSONArray("errors")
            return if (errors.count() == 1) {
                errors.getJSONObject(0).getString("code")
            } else {
                null
            }
        }

        fun getErrorMessage(jsonObject: JSONObject): String? {
            val errors = jsonObject.getJSONArray("errors")
            return if (errors.count() == 1) {
                errors.getJSONObject(0).getString("title")
            } else {
                null
            }
        }
    }
}
