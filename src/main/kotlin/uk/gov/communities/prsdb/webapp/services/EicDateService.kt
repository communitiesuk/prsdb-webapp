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

@Primary
@Conditional(WebServerOnly::class)
@Service("eic-date-service-flag-off")
class BeforePdjb1666EicDateService : EicDateService {
    override fun issueDateFrom(propertyCompliance: PropertyCompliance): LocalDate? =
        propertyCompliance.electricalSafetyExpiryDate?.minusYears(EIC_VALIDITY_YEARS.toLong())

    override fun expiryDateFrom(propertyCompliance: PropertyCompliance): LocalDate? = propertyCompliance.electricalSafetyExpiryDate

    override fun applyIssueDate(
        propertyCompliance: PropertyCompliance,
        issueDate: LocalDate?,
    ) {
        propertyCompliance.electricalSafetyExpiryDate = issueDate?.plusYears(EIC_VALIDITY_YEARS.toLong())
    }

    override fun applyExpiryDate(
        propertyCompliance: PropertyCompliance,
        expiryDate: LocalDate?,
    ) {
        propertyCompliance.electricalSafetyExpiryDate = expiryDate
    }
}

// TODO PDJB-1669: Replace delegation with issue-date-backed storage.
@Conditional(WebServerOnly::class)
@Service("eic-date-service-flag-on")
class AfterPdjb1666EicDateService : EicDateService by BeforePdjb1666EicDateService()
