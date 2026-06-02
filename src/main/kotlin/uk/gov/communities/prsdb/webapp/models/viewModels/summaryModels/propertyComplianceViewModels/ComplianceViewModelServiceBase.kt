package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

abstract class ComplianceViewModelServiceBase(
    protected val messageSource: MessageSource,
) {
    protected abstract val provideLaterUnoccupiedKey: String
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
    ): Any {
        val isOccupied = propertyCompliance.propertyOwnership.isOccupied

        return when {
            !isOccupied -> provideLaterUnoccupiedKey
            status == ComplianceCertStatus.PROVIDE_LATER ->
                getProvideLaterWithDeadlineText(propertyCompliance.propertyOwnership.lastOccupiedDate)
            else -> missingCertOccupiedValue
        }
    }

    protected fun getProvideLaterWithDeadlineText(lastOccupiedDate: LocalDate?): String {
        val deadline =
            lastOccupiedDate?.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                ?: throw IllegalStateException("Cannot get provide-later-with-deadline text without an occupied date")
        val formattedDate = deadline.format(DATE_FORMATTER)
        return messageSource.getMessageForKey(provideLaterWithDeadlineKey, arrayOf(formattedDate))
    }

    companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
