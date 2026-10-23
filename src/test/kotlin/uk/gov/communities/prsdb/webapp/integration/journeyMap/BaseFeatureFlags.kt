package uk.gov.communities.prsdb.webapp.integration.journeyMap

import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.UrlResource
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagConfigModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureReleaseConfigModel

data class BaseFeatureFlags(
    val featureFlags: List<FeatureFlagConfigModel>,
    val releases: List<FeatureReleaseConfigModel>,
) {
    companion object {
        // The first document of the main application.yml applies everywhere; the test resources' own copy shadows it on the classpath
        fun load(): BaseFeatureFlags {
            val mainApplicationYml = UrlResource(ClassPathResource("data-local.sql").url).createRelative("application.yml")
            val baseDocument =
                YamlPropertySourceLoader()
                    .load("application.yml", mainApplicationYml)
                    .first()
            val binder = Binder(ConfigurationPropertySources.from(baseDocument).filterNotNull())
            return BaseFeatureFlags(
                featureFlags =
                    binder
                        .bind("features.feature-flags", Bindable.listOf(FeatureFlagConfigModel::class.java))
                        .orElse(emptyList()) ?: emptyList(),
                releases =
                    binder
                        .bind("features.releases", Bindable.listOf(FeatureReleaseConfigModel::class.java))
                        .orElse(emptyList()) ?: emptyList(),
            )
        }
    }
}
