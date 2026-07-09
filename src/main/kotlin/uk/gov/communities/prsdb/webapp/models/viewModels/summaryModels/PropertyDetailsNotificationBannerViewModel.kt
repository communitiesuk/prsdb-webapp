package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

/**
 * Notification banner shown on the property record for the "provide later" registration flow
 * (behind PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING). Banners are only shown for occupied
 * properties, in both the landlord and local council views.
 *
 * When a property has details in "provide later" AND an outstanding compliance issue, only the
 * combined banner is shown; the separate provide-later and compliance-cert banners are suppressed.
 */
class PropertyDetailsNotificationBannerViewModel private constructor(
    val variant: Variant,
) {
    enum class Variant {
        LICENSING,
        TENANCY,
        BOTH,
        COMBINED,
    }

    /** The combined banner replaces the compliance-cert banner, so that must be suppressed. */
    val suppressesComplianceBanner: Boolean = variant == Variant.COMBINED

    companion object {
        fun fromState(
            provideLaterEnabled: Boolean,
            isOccupied: Boolean,
            isLicensingProvideLater: Boolean,
            isTenancyProvideLater: Boolean,
            hasComplianceIssue: Boolean,
        ): PropertyDetailsNotificationBannerViewModel? {
            if (!provideLaterEnabled || !isOccupied) return null

            val hasPropertyProvideLater = isLicensingProvideLater || isTenancyProvideLater

            val variant =
                when {
                    hasPropertyProvideLater && hasComplianceIssue -> Variant.COMBINED
                    isLicensingProvideLater && isTenancyProvideLater -> Variant.BOTH
                    isLicensingProvideLater -> Variant.LICENSING
                    isTenancyProvideLater -> Variant.TENANCY
                    else -> return null
                }

            return PropertyDetailsNotificationBannerViewModel(variant)
        }
    }
}
