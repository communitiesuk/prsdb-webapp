package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

open class JourneyDataHelper {
    companion object {
        fun getManualAddress(
            journeyDataService: JourneyDataService,
            journeyData: JourneyData,
            manualAddressPathSegment: String,
        ): AddressDataModel? {
            val addressLineOne =
                journeyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "addressLineOne",
                ) ?: return null

            val townOrCity =
                journeyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "townOrCity",
                ) ?: return null

            val postcode =
                journeyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "postcode",
                ) ?: return null

            val addressLineTwo =
                journeyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "addressLineTwo",
                )

            val county =
                journeyDataService.getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    "county",
                )

            return AddressDataModel.fromManualAddressData(
                addressLineOne,
                townOrCity,
                postcode,
                addressLineTwo,
                county,
            )
        }
    }
}
