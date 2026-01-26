package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.BillsIncludedFormModel
import kotlin.test.assertEquals

class BillsIncludedDataModelTests {
    val formModel = BillsIncludedFormModel()
    private val customBillsIncluded = "Lawnmowing services"

    @BeforeEach
    fun setup() {
        formModel.billsIncluded =
            mutableListOf(BillsIncluded.GAS.toString(), BillsIncluded.CONTENTS_INSURANCE.toString())
        formModel.customBillsIncluded = customBillsIncluded
    }

    @Test
    fun `fromFormData correctly builds DataModel from FormModel`() {
        formModel.billsIncluded.add(BillsIncluded.SOMETHING_ELSE.toString())

        val dataModel = BillsIncludedDataModel.fromFormData(formModel)

        assertEquals(
            dataModel.standardBillsIncludedString,
            "${BillsIncluded.GAS},${BillsIncluded.CONTENTS_INSURANCE},${BillsIncluded.SOMETHING_ELSE}",
        )
        assertEquals(
            dataModel.standardBillsIncludedListAsEnums,
            listOf(BillsIncluded.GAS, BillsIncluded.CONTENTS_INSURANCE, BillsIncluded.SOMETHING_ELSE),
        )
        assertEquals(dataModel.customBillsIncluded, customBillsIncluded)
    }

    @Test
    fun `fromFormData sets customBillsIncluded to null if SOMETHING_ELSE is not selected`() {
        val dataModel = BillsIncludedDataModel.fromFormData(formModel)

        assertNull(dataModel.customBillsIncluded)
    }
}
