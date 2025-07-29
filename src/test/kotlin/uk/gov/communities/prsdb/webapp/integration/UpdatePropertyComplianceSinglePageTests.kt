package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdatePropertyComplianceSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Nested
    inner class UpdateGasSafetyStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
            updateGasSafetyPage.form.submit()
            assertThat(
                updateGasSafetyPage.form.getErrorMessage(),
            ).containsText("Select whether you want to add a new certificate or exemption")
        }
    }

    @Nested
    inner class UpdateEicrStep {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val updateEicrPage = navigator.goToPropertyComplianceUpdateUpdateEicrPage(PROPERTY_OWNERSHIP_ID)
            updateEicrPage.form.submit()
            assertThat(updateEicrPage.form.getErrorMessage())
                .containsText("Select whether you want to add a new EICR or exemption")
        }
    }

    @Nested
    inner class UpdateEpcStep {
        // TODO PRSD-1312 - remove @Disabled when Gas Safety completion links to the rest of the journey
        @Disabled
        @Test
        fun `Submitting with no value entered returns an error`() {
            val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(PROPERTY_OWNERSHIP_ID)
            updateEpcPage.form.submit()
            assertThat(updateEpcPage.form.getErrorMessage())
                .containsText("Select whether you want to add a new certificate or exemption")
        }
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 12L
    }
}
