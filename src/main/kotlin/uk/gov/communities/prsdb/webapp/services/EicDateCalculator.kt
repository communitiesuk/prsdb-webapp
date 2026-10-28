package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.WebServerOnly
import uk.gov.communities.prsdb.webapp.constants.EIC_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import java.time.LocalDate

// The single place that knows how an EIC's dates map to storage. Implementations use @Service rather than
// @PrsdbWebService so FF4J can read their bean names without creating every bean when this is flipped to
// issue-date-backed storage.
interface EicDateCalculator {
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
@Service("eic-date-calculator-flag-off")
class ExpiryDateBackedEicDateCalculator : EicDateCalculator {
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
