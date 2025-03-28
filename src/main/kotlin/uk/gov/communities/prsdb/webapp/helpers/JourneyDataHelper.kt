package uk.gov.communities.prsdb.webapp.helpers

import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalAuthorityFormModel
import java.time.LocalDate
import kotlin.reflect.full.memberProperties

open class JourneyDataHelper {
    companion object {
        fun getLookupAddressHouseNameOrNumberAndPostcode(
            journeyData: JourneyData,
            lookupAddressPathSegment: String,
        ): Pair<String, String>? {
            val houseNameOrNumber =
                getFieldStringValue(journeyData, lookupAddressPathSegment, LookupAddressFormModel::class.memberProperties.first().name)
                    ?: return null

            val postcode =
                getFieldStringValue(journeyData, lookupAddressPathSegment, LookupAddressFormModel::class.memberProperties.last().name)
                    ?: return null

            return Pair(houseNameOrNumber, postcode)
        }

        fun getManualAddress(
            journeyData: JourneyData,
            manualAddressPathSegment: String,
            selectLocalAuthorityPathSegment: String? = null,
        ): AddressDataModel? {
            val addressLineOne =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    ManualAddressFormModel::class.memberProperties.first().name,
                ) ?: return null

            val townOrCity =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    ManualAddressFormModel::class.memberProperties.last().name,
                ) ?: return null

            val postcode =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    ManualAddressFormModel::class.memberProperties.elementAt(3).name,
                ) ?: return null

            val addressLineTwo =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    ManualAddressFormModel::class.memberProperties.elementAt(1).name,
                )

            val county =
                getFieldStringValue(
                    journeyData,
                    manualAddressPathSegment,
                    ManualAddressFormModel::class.memberProperties.elementAt(2).name,
                )

            val localAuthorityId =
                selectLocalAuthorityPathSegment?.let {
                    getFieldIntegerValue(
                        journeyData,
                        it,
                        SelectLocalAuthorityFormModel::class.memberProperties.first().name,
                    ) ?: return null
                }

            return AddressDataModel.fromManualAddressData(
                addressLineOne,
                townOrCity,
                postcode,
                addressLineTwo,
                county,
                localAuthorityId,
            )
        }

        fun getStringValueByKey(
            journeyData: JourneyData,
            key: String,
        ): String? = journeyData[key]?.toString()

        fun getPageData(
            journeyData: JourneyData,
            pageName: String,
            subPageNumber: Int? = null,
        ): PageData? {
            var pageData = objectToStringKeyedMap(journeyData[pageName])
            if (subPageNumber != null && pageData != null) {
                pageData = objectToStringKeyedMap(pageData[subPageNumber.toString()])
            }
            return pageData
        }

        fun getFieldStringValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): String? {
            val pageData = getPageData(journeyData, urlPathSegment, subPageNumber)
            return pageData?.get(fieldName)?.toString()
        }

        fun getFieldIntegerValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): Int? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString.toInt()
        }

        fun getFieldLocalDateValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): LocalDate? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString.let { LocalDate.parse(fieldAsString) }
        }

        fun getFieldBooleanValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): Boolean? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString == "true"
        }

        inline fun <reified E : Enum<E>> getFieldEnumValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): E? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return enumValueOf<E>(fieldAsString)
        }
    }
}
