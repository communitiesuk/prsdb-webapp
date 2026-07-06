package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType

class OrgTypeFormModelTests {
    private lateinit var orgTypeFormModel: OrgTypeFormModel

    @BeforeEach
    fun setup() {
        orgTypeFormModel = OrgTypeFormModel()
    }

    @Test
    fun `isSelectionValid returns false when no types are selected`() {
        orgTypeFormModel.orgTypes = mutableListOf(null, null, null, null)
        assertFalse(orgTypeFormModel.isSelectionValid())
    }

    @Test
    fun `isSelectionValid returns true when a single type is selected`() {
        orgTypeFormModel.orgTypes = mutableListOf(OrgType.COMPANY.name, null, null, null)
        assertTrue(orgTypeFormModel.isSelectionValid())
    }

    @Test
    fun `isSelectionValid returns true when multiple types are selected`() {
        orgTypeFormModel.orgTypes = mutableListOf(OrgType.COMPANY.name, OrgType.CHARITY.name)
        assertTrue(orgTypeFormModel.isSelectionValid())
    }

    @Test
    fun `isSelectionValid returns true when only None is selected`() {
        orgTypeFormModel.orgTypes = mutableListOf(OrgType.NONE.name)
        assertTrue(orgTypeFormModel.isSelectionValid())
    }

    @Test
    fun `isSelectionValid returns false when None is selected with another type`() {
        orgTypeFormModel.orgTypes = mutableListOf(OrgType.COMPANY.name, OrgType.NONE.name)
        assertFalse(orgTypeFormModel.isSelectionValid())
    }
}
