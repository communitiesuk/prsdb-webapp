package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.ff4j.exception.PropertyNotFoundException
import org.junit.jupiter.api.Test
import java.util.Date

class FeatureFlagExpiryDateTests : FeatureFlagTest() {
    @Test
    fun `all configured features have expiry dates in the future`() {
        featureFlagManager.features.forEach({ (name, feature) ->
            try {
                val expiryDate = feature.getProperty<Date>("expiryDate").value
                assert(expiryDate.after(Date())) { "Feature $name has an expiry date in the past: $expiryDate" }
            } catch (e: PropertyNotFoundException) {
                assert(false) { "Feature $name does not have an expiry date property" }
            }
        })
    }
}
