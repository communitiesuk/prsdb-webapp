package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpStatus
import uk.gov.communities.prsdb.webapp.constants.FAILOVER_TEST_ENDPOINTS
import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideChoice
import uk.gov.communities.prsdb.webapp.controllers.FailoverTestController.Companion.ERROR_501_URL_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.FeatureFlagOverridesPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFeatureFlagConfig
import kotlin.test.Test

class FeatureFlagOverridesSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    private lateinit var configuredFlags: List<FeatureFlagConfigModel>
    private lateinit var configuredReleases: List<FeatureReleaseConfigModel>

    @BeforeEach
    fun addTestFlagsToConfiguration() {
        configuredFlags = featureFlagConfig.featureFlags
        configuredReleases = featureFlagConfig.releases

        // Added to the configured flags rather than replacing them, as the rest of the application relies on those existing
        featureFlagConfig.featureFlags = configuredFlags.map { it.inTestReleaseIfObservable() } + testFlags
        featureFlagConfig.releases = configuredReleases + testReleases
        FeatureFlagConfigUpdater.resetToConfiguration(
            featureFlagManager,
            featureFlagConfig.featureFlags,
            featureFlagConfig.releases,
        )
    }

    @AfterEach
    fun restoreConfiguration() {
        featureFlagConfig.featureFlags = configuredFlags
        featureFlagConfig.releases = configuredReleases
    }

    @Test
    fun `every configured flag and release is listed with no override selected`() {
        val overridesPage = navigator.goToFeatureFlagOverrides()

        assertThat(overridesPage.heading).isVisible()
        featureFlagConfig.featureFlags.forEach {
            assertEquals(FeatureFlagOverrideChoice.DEFAULT.name, overridesPage.flagRadios(it.name).selectedValue)
        }
        featureFlagConfig.releases.forEach {
            assertEquals(FeatureFlagOverrideChoice.DEFAULT.name, overridesPage.releaseRadios(it.name).selectedValue)
        }
    }

    @Test
    fun `the save and reset buttons are shown together in a single button group`() {
        val overridesPage = navigator.goToFeatureFlagOverrides()

        assertThat(overridesPage.buttonGroup).hasCount(1)
        assertThat(overridesPage.buttonGroup.locator("button")).hasCount(2)
    }

    @Test
    fun `a flag's hint describes its configured default and which release it belongs to`() {
        val overridesPage = navigator.goToFeatureFlagOverrides()

        assertThat(overridesPage.flagHint(ENABLED_FLAG)).hasText("Default is on, not in a release")
        assertThat(overridesPage.flagHint(DISABLED_FLAG)).hasText("Default is off, not in a release")
        assertThat(overridesPage.flagHint(FLAG_IN_RELEASE)).hasText("Default is on, in release $RELEASE")
    }

    @Test
    fun `a flag's hint keeps describing the default once the flag has been overridden`() {
        val overridesPage = overrideFlag(ENABLED_FLAG, FeatureFlagOverrideChoice.OFF)

        assertEquals(FeatureFlagOverrideChoice.OFF.name, overridesPage.flagRadios(ENABLED_FLAG).selectedValue)
        assertThat(overridesPage.flagHint(ENABLED_FLAG)).hasText("Default is on, not in a release")
    }

    @Test
    fun `overriding a flag off makes an endpoint that requires it unavailable`() {
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), failoverEndpointStatus())

        overrideFlag(FAILOVER_TEST_ENDPOINTS, FeatureFlagOverrideChoice.OFF)

        assertEquals(HttpStatus.NOT_FOUND.value(), failoverEndpointStatus())
    }

    @Test
    fun `a release override takes precedence over a flag override`() {
        var overridesPage = navigator.goToFeatureFlagOverrides()
        overridesPage.flagRadios(FAILOVER_TEST_ENDPOINTS).selectValue(FeatureFlagOverrideChoice.ON)
        overridesPage.releaseRadios(RELEASE).selectValue(FeatureFlagOverrideChoice.OFF)
        overridesPage.saveButton.clickAndWait()

        overridesPage = assertPageIs(overridesPage.page, FeatureFlagOverridesPage::class)
        assertEquals(FeatureFlagOverrideChoice.ON.name, overridesPage.flagRadios(FAILOVER_TEST_ENDPOINTS).selectedValue)
        assertEquals(HttpStatus.NOT_FOUND.value(), failoverEndpointStatus())
    }

    @Test
    fun `overriding a flag leaves it unchanged for anyone without that override`() {
        overrideFlag(ENABLED_FLAG, FeatureFlagOverrideChoice.OFF)

        assertTrue(featureFlagManager.checkFeature(ENABLED_FLAG))
    }

    @Test
    fun `resetting overrides restores the configured values`() {
        var overridesPage = overrideFlag(FAILOVER_TEST_ENDPOINTS, FeatureFlagOverrideChoice.OFF)
        overridesPage.resetButton.clickAndWait()

        overridesPage = assertPageIs(overridesPage.page, FeatureFlagOverridesPage::class)
        assertEquals(FeatureFlagOverrideChoice.DEFAULT.name, overridesPage.flagRadios(FAILOVER_TEST_ENDPOINTS).selectedValue)
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), failoverEndpointStatus())
    }

    private fun failoverEndpointStatus() = navigator.navigate(ERROR_501_URL_ROUTE)?.status()

    private fun overrideFlag(
        flagName: String,
        choice: FeatureFlagOverrideChoice,
    ): FeatureFlagOverridesPage {
        val overridesPage = navigator.goToFeatureFlagOverrides()
        overridesPage.flagRadios(flagName).selectValue(choice)
        overridesPage.saveButton.clickAndWait()

        return assertPageIs(overridesPage.page, FeatureFlagOverridesPage::class)
    }

    // Puts the one flag with an endpoint behind it into a release, so that release precedence can be observed
    private fun FeatureFlagConfigModel.inTestReleaseIfObservable() = if (name == FAILOVER_TEST_ENDPOINTS) copy(release = RELEASE) else this

    companion object {
        private const val ENABLED_FLAG = "test-enabled-flag"
        private const val DISABLED_FLAG = "test-disabled-flag"
        private const val FLAG_IN_RELEASE = "test-flag-in-release"
        private const val RELEASE = "test-release"

        private val testFlags =
            listOf(
                MockFeatureFlagConfig.createFeatureFlagConfigModel(name = ENABLED_FLAG, enabled = true),
                MockFeatureFlagConfig.createFeatureFlagConfigModel(name = DISABLED_FLAG, enabled = false),
                MockFeatureFlagConfig.createFeatureFlagConfigModel(name = FLAG_IN_RELEASE, enabled = true, release = RELEASE),
            )

        private val testReleases = listOf(MockFeatureFlagConfig.createFeatureReleaseConfigModel(name = RELEASE, enabled = true))
    }
}
