package uk.gov.communities.prsdb.webapp.local.api

import uk.gov.communities.prsdb.webapp.constants.MAX_ADDRESSES
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.math.min

class MockOSPlacesAPIResponses {
    companion object {
        fun createResponseOfSize(
            size: Int,
            useEnglishAddresses: Boolean = true,
        ): String {
            val addresses =
                (1..min(size, MAX_ADDRESSES)).map {
                    AddressDataModel(
                        singleLineAddress = "$it, Example Road, EG",
                        uprn = "${it}123456".toLong(),
                        buildingNumber = it.toString(),
                    )
                }
            return createResponse(addresses, useEnglishAddresses)
        }

        fun createResponse(address: AddressDataModel) = createResponse(listOf(address))

        fun createResponse(
            addresses: List<AddressDataModel>,
            useEnglishAddresses: Boolean = true,
        ) = if (addresses.isEmpty()) {
            "{}"
        } else {
            addresses.joinToString(",", "{'results':[", "]}") {
                """
                    {'DPA':
                        {
                            'ADDRESS':'${it.singleLineAddress}',
                            'LOCAL_CUSTODIAN_CODE':'114',
                            'UPRN':'${it.uprn ?: 123456}',
                            'BUILDING_NUMBER':'${it.buildingNumber ?: "1"}',
                            'POSTCODE':'${it.postcode ?: "EG"}',
                            'COUNTRY_CODE':'${if (useEnglishAddresses) "E" else "W"}',
                        }
                    }
                """
            }
        }
    }
}
