package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter

data class BillsIncludedDataModel(
    val allBillsIncludedList: List<String>,
) {
    companion object {
        fun fromFormData(
            billsIncluded: MutableList<String?>,
            customBillsIncluded: String,
        ): BillsIncludedDataModel =
            BillsIncludedDataModel(
                allBillsIncludedList = getAllBillsIncludedList(billsIncluded, customBillsIncluded),
            )

        fun getAllBillsIncludedList(
            billsIncluded: MutableList<String?>,
            customBillsIncluded: String,
        ): List<String> {
            val billsIncludedList = getMessageKeysForStandardBillsIncluded(billsIncluded)
            if (billsIncluded.contains(BillsIncluded.SOMETHING_ELSE.toString()) && customBillsIncluded.isNotEmpty()) {
                billsIncludedList += customBillsIncluded
            } else {
                billsIncludedList.removeLast()
            }
            return billsIncludedList.filterNotNull()
        }

        fun getMessageKeysForStandardBillsIncluded(billsIncluded: MutableList<String?>): MutableList<String?> {
            val billsIncludedList: MutableList<String?> = mutableListOf()
            billsIncluded.forEach { bill ->
                bill?.let {
                    if (BillsIncluded.valueOf(bill) != BillsIncluded.SOMETHING_ELSE) {
                        billsIncludedList.add(
                            MessageKeyConverter.convert(
                                BillsIncluded.valueOf(bill),
                            ),
                        )
                        billsIncludedList.add(", ")
                    }
                }
            }
            return billsIncludedList
        }
    }
}
