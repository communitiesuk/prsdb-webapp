package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter

data class BillsIncludedDataModel(
    val billsIncluded: MutableList<String?>,
    val customBillsIncluded: String?,
) {
    val commaSeparatedBills: String
        get() = billsIncluded?.filterNotNull()?.map { MessageKeyConverter.convert(BillsIncluded.valueOf(it!!)) }?.joinToString(", ") ?: ""

    companion object {
        fun fromFormData(
            billsIncluded: MutableList<String?>,
            customBillsIncluded: String?,
        ): BillsIncludedDataModel = BillsIncludedDataModel(billsIncluded, customBillsIncluded)
    }
}
