package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindOption

class OwnershipKindFormModelTests {
    @Test
    fun `isSelectionValid returns false when nothing is selected`() {
        val formModel = OwnershipKindFormModel().apply { ownershipKinds = mutableListOf(null, null) }

        assertFalse(formModel.isSelectionValid())
    }

    @Test
    fun `isSelectionValid returns false when a selected value is not a valid option`() {
        val formModel =
            OwnershipKindFormModel().apply {
                ownershipKinds = mutableListOf(OwnershipKindOption.FREEHOLD.name, "COMMONHOLD")
            }

        assertFalse(formModel.isSelectionValid())
    }

    @ParameterizedTest
    @MethodSource("validSelectionValues")
    fun `isSelectionValid returns true for a valid selection`(selected: List<String?>) {
        val formModel = OwnershipKindFormModel().apply { ownershipKinds = selected.toMutableList() }

        assertTrue(formModel.isSelectionValid())
    }

    @ParameterizedTest
    @MethodSource("validSelections")
    fun `toOwnershipKind maps the selected options to the ownership kind`(
        selected: List<String?>,
        expected: OwnershipKind,
    ) {
        val formModel = OwnershipKindFormModel().apply { ownershipKinds = selected.toMutableList() }

        assertEquals(expected, formModel.toOwnershipKind())
    }

    @Test
    fun `toOwnershipKind returns null when nothing is selected`() {
        assertNull(OwnershipKindFormModel().toOwnershipKind())
    }

    companion object {
        @JvmStatic
        fun validSelections() =
            listOf(
                Arguments.of(listOf(OwnershipKindOption.FREEHOLD.name, null), OwnershipKind.FREEHOLD_ONLY),
                Arguments.of(listOf(null, OwnershipKindOption.LEASEHOLD.name), OwnershipKind.LEASEHOLD_ONLY),
                Arguments.of(
                    listOf(OwnershipKindOption.FREEHOLD.name, OwnershipKindOption.LEASEHOLD.name),
                    OwnershipKind.FREEHOLD_AND_LEASEHOLD,
                ),
            )

        @JvmStatic
        fun validSelectionValues() = validSelections().map { Arguments.of(it.get()[0]) }
    }
}
