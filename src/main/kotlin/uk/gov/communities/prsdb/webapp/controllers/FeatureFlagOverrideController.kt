package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.context.annotation.Conditional
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.FeatureFlagOverridesEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.FEATURE_FLAGS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideAction
import uk.gov.communities.prsdb.webapp.constants.enums.FeatureFlagOverrideChoice
import uk.gov.communities.prsdb.webapp.controllers.FeatureFlagOverrideController.Companion.FEATURE_FLAG_OVERRIDES_ROUTE
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagOverrides
import uk.gov.communities.prsdb.webapp.models.requestModels.FeatureFlagOverrideRequestModel
import uk.gov.communities.prsdb.webapp.models.viewModels.FeatureFlagOverrideViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.FeatureReleaseOverrideViewModel
import uk.gov.communities.prsdb.webapp.services.FeatureFlagOverrideService

@PrsdbController
@Conditional(FeatureFlagOverridesEnabled::class)
@RequestMapping(FEATURE_FLAG_OVERRIDES_ROUTE)
class FeatureFlagOverrideController(
    private val featureFlagConfig: FeatureFlagConfig,
    private val featureFlagManager: FeatureFlagManager,
    private val featureFlagOverrideService: FeatureFlagOverrideService,
) {
    @GetMapping
    fun getOverridesPage(model: Model): String {
        val overrides = featureFlagOverrideService.getOverrides()

        model.addAttribute("releases", buildReleaseViewModels(overrides))
        model.addAttribute("flags", buildFlagViewModels(overrides))
        model.addAttribute("choices", FeatureFlagOverrideChoice.entries)
        model.addAttribute("saveAction", FeatureFlagOverrideAction.SAVE)
        model.addAttribute("resetAction", FeatureFlagOverrideAction.RESET)

        return "featureFlagOverrides"
    }

    @PostMapping
    fun setOverrides(
        @ModelAttribute requestModel: FeatureFlagOverrideRequestModel,
    ): String {
        when (requestModel.action) {
            FeatureFlagOverrideAction.SAVE ->
                featureFlagOverrideService.setOverrides(
                    FeatureFlagOverrides(
                        flags = requestModel.flags.toOverrides(featureFlagConfig.featureFlags.map { it.name }),
                        releases = requestModel.releases.toOverrides(featureFlagConfig.releases.map { it.name }),
                    ),
                )

            FeatureFlagOverrideAction.RESET -> featureFlagOverrideService.clearOverrides()
        }

        return "redirect:$FEATURE_FLAG_OVERRIDES_ROUTE"
    }

    private fun Map<String, FeatureFlagOverrideChoice>.toOverrides(configuredNames: List<String>) =
        this
            .filterKeys { it in configuredNames }
            .mapNotNull { (name, choice) -> choice.toOverrideOrNull()?.let { name to it } }
            .toMap()

    private fun buildReleaseViewModels(overrides: FeatureFlagOverrides) =
        featureFlagConfig.releases.map { release ->
            FeatureReleaseOverrideViewModel(
                name = release.name,
                flagNames = featureFlagConfig.featureFlags.filter { it.release == release.name }.map { it.name },
                choice = FeatureFlagOverrideChoice.fromOverride(overrides.releases[release.name]),
            )
        }

    private fun buildFlagViewModels(overrides: FeatureFlagOverrides) =
        featureFlagConfig.featureFlags.map { flag ->
            FeatureFlagOverrideViewModel(
                name = flag.name,
                release = flag.release,
                isEnabledByDefault = featureFlagManager.checkConfiguredFeature(flag.name),
                choice = FeatureFlagOverrideChoice.fromOverride(overrides.flags[flag.name]),
                isSupersededByReleaseOverride = flag.release != null && overrides.releases.containsKey(flag.release),
            )
        }

    companion object {
        const val FEATURE_FLAG_OVERRIDES_ROUTE = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$FEATURE_FLAGS_PATH_SEGMENT"
    }
}
