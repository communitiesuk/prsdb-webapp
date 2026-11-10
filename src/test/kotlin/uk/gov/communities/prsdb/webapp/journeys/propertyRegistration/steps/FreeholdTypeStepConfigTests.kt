package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@ExtendWith(MockitoExtension::class)
class FreeholdTypeStepConfigTests {
    @Mock
    lateinit var mockJourneyState: JourneyState

    @Test
    fun `getStepSpecificContent includes the heading, hint, freehold type options and submit button`() {
        val content = FreeholdTypeStepConfig().getStepSpecificContent(mockJourneyState)

        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.freeholdType.fieldSetHeading",
                "fieldSetHint" to "registerProperty.freeholdType.fieldSetHint",
                "radioOptions" to
                    listOf(
                        RadiosButtonViewModel(
                            value = FreeholdType.SOLE_OWNERSHIP,
                            labelMsgKey = "registerProperty.freeholdType.radios.option.soleOwnership.label",
                            hintMsgKey = "registerProperty.freeholdType.radios.option.soleOwnership.hint",
                        ),
                        RadiosButtonViewModel(
                            value = FreeholdType.JOINT_TENANTS,
                            labelMsgKey = "registerProperty.freeholdType.radios.option.jointTenants.label",
                            hintMsgKey = "registerProperty.freeholdType.radios.option.jointTenants.hint",
                        ),
                        RadiosButtonViewModel(
                            value = FreeholdType.TENANTS_IN_COMMON,
                            labelMsgKey = "registerProperty.freeholdType.radios.option.tenantsInCommon.label",
                            hintMsgKey = "registerProperty.freeholdType.radios.option.tenantsInCommon.hint",
                        ),
                        RadiosButtonViewModel(
                            value = FreeholdType.COMMONHOLD,
                            labelMsgKey = "registerProperty.freeholdType.radios.option.commonhold.label",
                            hintMsgKey = "registerProperty.freeholdType.radios.option.commonhold.hint",
                        ),
                    ),
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }
}
