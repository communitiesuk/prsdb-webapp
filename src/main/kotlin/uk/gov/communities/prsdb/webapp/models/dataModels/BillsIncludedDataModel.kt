package uk.gov.communities.prsdb.webapp.models.dataModels

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded

data class BillsIncludedDataModel(
    val allBillsIncludedList: List<Any>,
    val standardBillsIncludedList: String,
    val customBillsIncludedIfRequired: String?,
) {
    companion object {
        fun fromFormData(
            billsIncluded: MutableList<String?>,
            customBillsIncluded: String,
        ): BillsIncludedDataModel =
            BillsIncludedDataModel(
                allBillsIncludedList = getAllBillsIncludedList(billsIncluded, customBillsIncluded),
                standardBillsIncludedList = billsIncluded.filterNotNull().joinToString(separator = ","),
                customBillsIncludedIfRequired = getCustomBillsIncludedIfRequired(billsIncluded, customBillsIncluded),
            )

        fun getAllBillsIncludedList(
            billsIncluded: MutableList<String?>,
            customBillsIncluded: String,
        ): List<Any> {
            val billsIncludedList = getStandardBillsIncludedList(billsIncluded)
            if (shouldIncludeCustomBillsIncluded(billsIncluded, customBillsIncluded)) {
                billsIncludedList += customBillsIncluded
            } else {
                billsIncludedList.removeLast()
            }
            return billsIncludedList.filterNotNull()
        }

        fun getStandardBillsIncludedList(billsIncluded: MutableList<String?>): MutableList<Any?> {
            val billsIncludedList: MutableList<Any?> = mutableListOf()
            billsIncluded.forEach { bill ->
                bill?.let {
                    if (BillsIncluded.valueOf(bill) != BillsIncluded.SOMETHING_ELSE) {
                        billsIncludedList.add(BillsIncluded.valueOf(bill))
                        billsIncludedList.add(", ")
                    }
                }
            }
            return billsIncludedList
        }

        fun getCustomBillsIncludedIfRequired(
            billsIncluded: MutableList<String?>,
            customBillsIncluded: String,
        ): String? =
            if (shouldIncludeCustomBillsIncluded(billsIncluded, customBillsIncluded)) {
                customBillsIncluded
            } else {
                null
            }

        fun shouldIncludeCustomBillsIncluded(
            billsIncluded: MutableList<String?>,
            customBillsIncluded: String,
        ): Boolean = billsIncluded.contains(BillsIncluded.SOMETHING_ELSE.toString()) && customBillsIncluded.isNotEmpty()
    }
}
