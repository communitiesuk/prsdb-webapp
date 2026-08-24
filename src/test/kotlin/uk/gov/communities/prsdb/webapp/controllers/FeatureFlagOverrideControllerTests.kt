package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.model
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.view
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.MANAGE_USERS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideAction
import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideChoice
import uk.gov.communities.prsdb.webapp.controllers.FeatureFlagOverrideController.Companion.FEATURE_FLAG_OVERRIDES_ROUTE
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagOverrides
import uk.gov.communities.prsdb.webapp.models.requestModels.FeatureFlagOverrideRequestModel
import uk.gov.communities.prsdb.webapp.services.FeatureFlagOverrideService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFeatureFlagConfig

@WebMvcTest(FeatureFlagOverrideController::class)
@TestPropertySource(properties = ["features.overrides-enabled=true"])
class FeatureFlagOverrideControllerTests(
    @Autowired val context: WebApplicationContext,
) : ControllerTest(context) {
    @MockitoBean
    lateinit var featureFlagOverrideService: FeatureFlagOverrideService

    @MockitoBean
    lateinit var featureFlagManager: FeatureFlagManager

    @MockitoBean
    lateinit var featureFlagConfig: FeatureFlagConfig

    private fun configureFlags(
        flagName: String = CONFIGURED_FLAG,
        releaseName: String? = null,
        overrides: FeatureFlagOverrides = FeatureFlagOverrides(),
    ) {
        whenever(featureFlagOverrideService.getOverrides()).thenReturn(overrides)
        whenever(featureFlagConfig.featureFlags)
            .thenReturn(listOf(MockFeatureFlagConfig.createFeatureFlagConfigModel(name = flagName, release = releaseName)))
        whenever(featureFlagConfig.releases)
            .thenReturn(releaseName?.let { listOf(MockFeatureFlagConfig.createFeatureReleaseConfigModel(name = it)) } ?: emptyList())
    }

    @Test
    fun `the page renders the configured flags and releases without requiring authentication`() {
        configureFlags(releaseName = CONFIGURED_RELEASE)

        mvc
            .perform(get(FEATURE_FLAG_OVERRIDES_ROUTE))
            .andExpect(status().isOk)
            .andExpect(view().name("featureFlagOverrides"))
            .andExpect(model().attributeExists("flags", "releases", "choices", "saveAction", "resetAction"))
    }

    @Test
    fun `submitting overrides stores them and redirects back to the page`() {
        configureFlags()

        mvc
            .perform(
                post(FEATURE_FLAG_OVERRIDES_ROUTE)
                    .param(flagParameter(CONFIGURED_FLAG), FeatureFlagOverrideChoice.ON.name)
                    .with(csrf()),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(FEATURE_FLAG_OVERRIDES_ROUTE))

        val captor = argumentCaptor<FeatureFlagOverrides>()
        verify(featureFlagOverrideService).setOverrides(captor.capture())
        assertEquals(mapOf(CONFIGURED_FLAG to true), captor.firstValue.flags)
    }

    @Test
    fun `submitting a choice of default does not store an override for that flag`() {
        configureFlags()

        mvc
            .perform(
                post(FEATURE_FLAG_OVERRIDES_ROUTE)
                    .param(flagParameter(CONFIGURED_FLAG), FeatureFlagOverrideChoice.DEFAULT.name)
                    .with(csrf()),
            ).andExpect(status().is3xxRedirection)

        val captor = argumentCaptor<FeatureFlagOverrides>()
        verify(featureFlagOverrideService).setOverrides(captor.capture())
        assertEquals(emptyMap<String, Boolean>(), captor.firstValue.flags)
    }

    @Test
    fun `submitting a flag that is not configured is ignored`() {
        configureFlags()

        mvc
            .perform(
                post(FEATURE_FLAG_OVERRIDES_ROUTE)
                    .param(flagParameter("an-unconfigured-flag"), FeatureFlagOverrideChoice.ON.name)
                    .with(csrf()),
            ).andExpect(status().is3xxRedirection)

        val captor = argumentCaptor<FeatureFlagOverrides>()
        verify(featureFlagOverrideService).setOverrides(captor.capture())
        assertEquals(emptyMap<String, Boolean>(), captor.firstValue.flags)
    }

    @Test
    fun `resetting clears the overrides and redirects back to the page`() {
        mvc
            .perform(
                post(FEATURE_FLAG_OVERRIDES_ROUTE)
                    .param(ACTION_PARAMETER, FeatureFlagOverrideAction.RESET.name)
                    .with(csrf()),
            ).andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(FEATURE_FLAG_OVERRIDES_ROUTE))

        verify(featureFlagOverrideService).clearOverrides()
    }

    @Test
    fun `permitting the overrides route does not permit other system operator routes`() {
        mvc
            .perform(get("$FEATURE_FLAG_OVERRIDES_ROUTE/$MANAGE_USERS_PATH_SEGMENT"))
            .andExpect(status().is3xxRedirection)
    }

    companion object {
        private const val CONFIGURED_FLAG = "a-flag"
        private const val CONFIGURED_RELEASE = "a-release"
        private val ACTION_PARAMETER = FeatureFlagOverrideRequestModel::action.name

        private fun flagParameter(flagName: String) = "${FeatureFlagOverrideRequestModel::flags.name}[$flagName]"
    }
}
