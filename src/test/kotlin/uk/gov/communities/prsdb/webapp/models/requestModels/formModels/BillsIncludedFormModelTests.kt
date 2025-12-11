package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded

class BillsIncludedFormModelTests {
    lateinit var billsIncludedFormModel: BillsIncludedFormModel

    @BeforeEach
    fun setup() {
        billsIncludedFormModel = BillsIncludedFormModel()
    }

    @Nested
    inner class NotAllFalseTests {
        @Test
        fun `returns false when billsIncluded is empty`() {
            billsIncludedFormModel.billsIncluded = mutableListOf()
            assertFalse(billsIncludedFormModel.notAllFalse())
        }

        @Test
        fun `returns true when billsIncluded is not empty`() {
            billsIncludedFormModel.billsIncluded = mutableListOf(BillsIncluded.WATER.toString())
            assertTrue(billsIncludedFormModel.notAllFalse())
        }
    }

    @Nested
    inner class IsCustomBillsIncludedValidNotBlankTests {
        @Test
        fun `returns false when something else is selected and customBillsIncluded is blank`() {
            billsIncludedFormModel.billsIncluded = mutableListOf(BillsIncluded.SOMETHING_ELSE.toString())
            billsIncludedFormModel.customBillsIncluded = ""
            assertFalse(billsIncludedFormModel.isCustomBillsIncludedValidNotBlank())
        }

        @Test
        fun `returns true when something else is selected and customBillsIncluded is not blank`() {
            billsIncludedFormModel.billsIncluded = mutableListOf(BillsIncluded.SOMETHING_ELSE.toString())
            billsIncludedFormModel.customBillsIncluded = "Internet"
            assertTrue(billsIncludedFormModel.isCustomBillsIncludedValidNotBlank())
        }

        @Test
        fun `returns true when something else is not selected`() {
            billsIncludedFormModel.billsIncluded = mutableListOf(BillsIncluded.WATER.toString())
            billsIncludedFormModel.customBillsIncluded = ""
            assertTrue(billsIncludedFormModel.isCustomBillsIncludedValidNotBlank())
        }
    }

    @Nested
    inner class IsCustomBillsIncludedNotTooLongTests {
        @Test
        fun `returns false when something else is selected and customBillsIncluded exceeds max length`() {
            billsIncludedFormModel.billsIncluded = mutableListOf(BillsIncluded.SOMETHING_ELSE.toString())
            billsIncludedFormModel.customBillsIncluded = "A".repeat(300)
            assertFalse(billsIncludedFormModel.isCustomBillsIncludedNotTooLong())
        }

        @Test
        fun `returns true when something else is selected and customBillsIncluded is within max length`() {
            billsIncludedFormModel.billsIncluded = mutableListOf(BillsIncluded.SOMETHING_ELSE.toString())
            billsIncludedFormModel.customBillsIncluded = "Internet"
            assertTrue(billsIncludedFormModel.isCustomBillsIncludedNotTooLong())
        }

        @Test
        fun `returns true when something else is not selected`() {
            billsIncludedFormModel.billsIncluded = mutableListOf(BillsIncluded.WATER.toString())
            billsIncludedFormModel.customBillsIncluded = "A".repeat(300)
            assertTrue(billsIncludedFormModel.isCustomBillsIncludedNotTooLong())
        }
    }
}
