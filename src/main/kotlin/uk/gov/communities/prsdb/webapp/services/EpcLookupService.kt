package uk.gov.communities.prsdb.webapp.services

import org.json.JSONObject
import org.springframework.stereotype.Service
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
            // TODO PRSD-1138 - parse errors
            return null
        }

        if (!jsonResponse.has("data")) {
            // TODO PRSD-1138 - throw error?
            return null
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

        // TODO PRSD-1138 - manually test this and write unit tests

        return EpcDataModel(
            certificateNumber = epcData.getString("epcRrn"),
            singleLineAddress = singleLineAddress,
            energyRating = epcData.getString("currentEnergyEfficiencyBand"),
            expiryDate = DateTimeHelper.getDateInUKFromDateString(epcData.getString("expiryDate")),
            latestCertificateNumberForThisProperty = epcData.getString("latestEpcRrnForAddress"),
        )
    }
}
