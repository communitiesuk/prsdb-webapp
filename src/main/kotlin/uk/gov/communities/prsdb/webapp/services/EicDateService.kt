package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.WebServerOnly
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.constants.EIC_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import java.time.LocalDate

// TODO PDJB-1666: Remove the feature-flag toggle and flag-off implementation once EIC issue-date storage is released.
@PrsdbFlip(name = EIC_ISSUE_DATE, alterBean = "eic-date-service-flag-on")
interface EicDateService {
    fun issueDateFrom(propertyCompliance: PropertyCompliance): LocalDate?

    fun expiryDateFrom(propertyCompliance: PropertyCompliance): LocalDate?

    fun applyIssueDate(
        propertyCompliance: PropertyCompliance,
        issueDate: LocalDate?,
    )

    fun applyExpiryDate(
        propertyCompliance: PropertyCompliance,
        expiryDate: LocalDate?,
    )
}

// The database migration runs whatever the flag state, so both flag states share issue-date-backed storage.
// The flag only controls journey and UI behaviour.
@Primary
@Conditional(WebServerOnly::class)
@Service("eic-date-service-flag-off")
class BeforePdjb1666EicDateService : EicDateService by AfterPdjb1666EicDateService()

@Conditional(WebServerOnly::class)
@Service("eic-date-service-flag-on")
class AfterPdjb1666EicDateService : EicDateService {
    override fun issueDateFrom(propertyCompliance: PropertyCompliance): LocalDate? = propertyCompliance.eicIssueDate

    override fun expiryDateFrom(propertyCompliance: PropertyCompliance): LocalDate? =
        propertyCompliance.eicIssueDate?.plusYears(EIC_VALIDITY_YEARS.toLong())

    override fun applyIssueDate(
        propertyCompliance: PropertyCompliance,
        issueDate: LocalDate?,
    ) {
        propertyCompliance.eicIssueDate = issueDate
    }

    override fun applyExpiryDate(
        propertyCompliance: PropertyCompliance,
        expiryDate: LocalDate?,
    ) {
        propertyCompliance.eicIssueDate = expiryDate?.minusYears(EIC_VALIDITY_YEARS.toLong())
    }
}
