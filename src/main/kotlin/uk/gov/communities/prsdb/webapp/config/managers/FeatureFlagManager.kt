package uk.gov.communities.prsdb.webapp.config.managers

import org.ff4j.FF4j
import org.ff4j.core.Feature
import org.ff4j.property.PropertyDate
import uk.gov.communities.prsdb.webapp.models.dataModels.FeatureFlagModel
import java.time.ZoneId
import java.util.Date

class FeatureFlagManager : FF4j() {
    fun initializeFeatureFlags(featureFlags: List<FeatureFlagModel>) {
        featureFlags.forEach { flag ->
            initializeFeatureFlag(flag)
        }
    }

    private fun initializeFeatureFlag(flag: FeatureFlagModel) {
        val feature = Feature(flag.name, flag.enabled)
        feature.addProperty(PropertyDate("expiryDate", getDateFromLocalDate(flag.expiryDate)))

        this.createFeature(feature)
    }

    private fun getDateFromLocalDate(localDate: java.time.LocalDate): Date =
        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
}
