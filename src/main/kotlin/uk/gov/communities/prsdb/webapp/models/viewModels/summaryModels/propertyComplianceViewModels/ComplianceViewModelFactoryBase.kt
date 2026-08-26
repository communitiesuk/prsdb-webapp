package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class ComplianceViewModelFactoryBase(
    protected val messageSource: MessageSource,
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

            status == ComplianceCertStatus.PROVIDE_LATER && propertyCompliance.propertyOwnership.hasBeenOccupiedSinceRegistration -> {
                getProvideLaterWithDeadlineText(propertyCompliance.propertyOwnership.registrationDate)
            }

            status == ComplianceCertStatus.PROVIDE_LATER -> {
                // The property has been unoccupied at some point since registration, so we cannot know the deadline.
                // Show the provide-later message without a date, mirroring the tenancy/licensing details on the record.
                provideLaterNoDeadlineKey
            }

            else -> {
                missingCertOccupiedValue
            }
        }

    private fun getProvideLaterWithDeadlineText(registrationDate: LocalDate): String {
        // Occupied-at-registration properties anchor the 28-day deadline to their registration date.
        val deadline = registrationDate.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
        val formattedDate = deadline.format(DATE_FORMATTER)
        return messageSource.getMessageForKey(provideLaterWithDeadlineKey, arrayOf(formattedDate))
    }

    companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
