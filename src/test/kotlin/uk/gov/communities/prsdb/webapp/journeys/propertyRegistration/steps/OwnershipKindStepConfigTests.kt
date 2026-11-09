package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxButtonViewModel

@ExtendWith(MockitoExtension::class)
class OwnershipKindStepConfigTests {
    @Mock
    lateinit var mockState: JourneyState

    @Test
    fun `getStepSpecificContent returns the heading, hint and freehold and leasehold checkboxes with hints`() {
        val content = OwnershipKindStepConfig().getStepSpecificContent(mockState)

        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.ownershipKind.fieldSetHeading",
                "fieldSetHint" to "registerProperty.ownershipKind.fieldSetHint",
                "checkboxOptions" to
                    listOf(
                        CheckboxButtonViewModel(
                            value = OwnershipKindOption.FREEHOLD,
                            labelMsgKey = "registerProperty.ownershipKind.checkbox.freehold.label",
                            hintMsgKey = "registerProperty.ownershipKind.checkbox.freehold.hint",
                        ),
                        CheckboxButtonViewModel(
                            value = OwnershipKindOption.LEASEHOLD,
                            labelMsgKey = "registerProperty.ownershipKind.checkbox.leasehold.label",
                            hintMsgKey = "registerProperty.ownershipKind.checkbox.leasehold.hint",
                        ),
                    ),
            ),
            content,
        )
    }
}
