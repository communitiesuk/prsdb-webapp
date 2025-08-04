package uk.gov.communities.prsdb.webapp.local.api

import uk.gov.communities.prsdb.webapp.constants.MAX_ADDRESSES
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import kotlin.math.min

class MockOSPlacesAPIResponses {
    companion object {
        fun createResponseOfSize(size: Int): String {
            val addresses =
                (1..min(size, MAX_ADDRESSES)).map {
                    AddressDataModel(
                        singleLineAddress = "$it, Example Road, EG",
                        uprn = "${it}123456".toLong(),
                        buildingNumber = it.toString(),
                    )
                }
            return createResponse(addresses)
        }

        fun createResponse(address: AddressDataModel) = createResponse(listOf(address))

        fun createResponse(addresses: List<AddressDataModel>) =
            if (addresses.isEmpty()) {
                "{}"
            } else {
                addresses.joinToString(",", "{'results':[", "]}") { address ->
                    """
                    {'DPA':
                        {
                            'ADDRESS':'${address.singleLineAddress}',
                            'LOCAL_CUSTODIAN_CODE':'114',
                            'UPRN':'${address.uprn ?: 123456}',
                            'BUILDING_NUMBER':'${address.buildingNumber ?: "1"}',
                            'POSTCODE':'${address.postcode ?: "EG"}',
                            'COUNTRY_CODE':'E'
                        }
                    }
                """
                }
            }
    }
}
