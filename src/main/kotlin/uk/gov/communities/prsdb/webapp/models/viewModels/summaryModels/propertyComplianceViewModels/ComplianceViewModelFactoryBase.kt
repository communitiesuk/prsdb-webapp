package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class ComplianceViewModelFactoryBase(
    protected val messageSource: MessageSource,
    private val featureFlagManager: FeatureFlagManager,
) {
    protected abstract val provideLaterUnoccupiedKey: String
    protected abstract val provideLaterNoDeadlineKey: String
    protected abstract val provideLaterWithDeadlineKey: String
    protected abstract val missingCertOccupiedValue: String
    protected abstract val occupiedNoCertInsetKey: String

    protected abstract fun getStatus(propertyCompliance: PropertyCompliance): ComplianceCertStatus

    open fun getInsetTextKey(propertyCompliance: PropertyCompliance): String? =
        getCouncilWillSeeInsetKey(getStatus(propertyCompliance), propertyCompliance)

    protected fun getCouncilWillSeeInsetKey(
        status: ComplianceCertStatus,
        propertyCompliance: PropertyCompliance,
    ): String? =
        if (propertyCompliance.propertyOwnership.isOccupied &&
            status in ComplianceCertStatus.COUNCIL_WILL_SEE_STATUSES
        ) {
            occupiedNoCertInsetKey
        } else {
            null
        }

    protected fun getMissingCertValue(
        status: ComplianceCertStatus,
        propertyCompliance: PropertyCompliance,
    ): Any =
        when {
            !propertyCompliance.propertyOwnership.isOccupied -> {
                provideLaterUnoccupiedKey
            }

            status == ComplianceCertStatus.PROVIDE_LATER -> {
                getProvideLaterValue(propertyCompliance)
            }

            else -> {
                missingCertOccupiedValue
            }
        }

    private fun getProvideLaterValue(propertyCompliance: PropertyCompliance): Any {
        val propertyOwnership = propertyCompliance.propertyOwnership
        // TODO PDJB-939: when the flag is permanently on, delete the flag-off branch (and the injected
        //  featureFlagManager) so the deadline is always anchored to the registration date.
        return if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
            if (propertyOwnership.hasBeenOccupiedSinceRegistration) {
                getProvideLaterWithDeadlineText(propertyOwnership.registrationDate)
            } else {
                provideLaterNoDeadlineKey
            }
        } else {
            val lastOccupiedDate =
                propertyOwnership.lastOccupiedDate
                    ?: throw IllegalStateException("Cannot get provide-later-with-deadline text without an occupied date")
            getProvideLaterWithDeadlineText(lastOccupiedDate)
        }
    }

    private fun getProvideLaterWithDeadlineText(anchorDate: LocalDate): String {
        val deadline = anchorDate.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
        val formattedDate = deadline.format(DATE_FORMATTER)
        return messageSource.getMessageForKey(provideLaterWithDeadlineKey, arrayOf(formattedDate))
    }

    companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
