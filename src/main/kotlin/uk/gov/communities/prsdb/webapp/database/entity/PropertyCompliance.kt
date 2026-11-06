package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.CertificateExpiryHelper
import java.time.LocalDate

@Entity
class PropertyCompliance() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @OneToOne(optional = false)
    @JoinColumn(name = "property_ownership_id", nullable = false, unique = true)
    lateinit var propertyOwnership: PropertyOwnership
        private set

    @OneToMany()
    @JoinTable(name = "gas_safety_uploads")
    var gasSafetyFileUploads: MutableList<FileUpload> = mutableListOf()

    var gasSafetyCertIssueDate: LocalDate? = null

    @OneToMany()
    @JoinTable(name = "electrical_safety_uploads")
    var electricalSafetyFileUploads: MutableList<FileUpload> = mutableListOf()

    // Only used for EICRs (and legacy records with no cert type). Set through PropertyComplianceService,
    // which keeps eicrExpiryDate/eicIssueDate mutually exclusive.
    var eicrExpiryDate: LocalDate? = null

    // TODO PDJB-1679: Update this comment when the EIC_ISSUE_DATE flag is removed - EIC dates will be read from this column directly.
    // Access through PropertyComplianceService/EicDateService rather than directly; EIC expiry dates
    // are derived from this issue date. Only used for EICs.
    var eicIssueDate: LocalDate? = null

    var electricalCertType: CertificateType? = null

    var epcUrl: String? = null

    var epcExpiryDate: LocalDate? = null

    var tenancyStartedBeforeEpcExpiry: Boolean? = null

    var epcEnergyRating: String? = null

    var epcExemptionReason: EpcExemptionReason? = null

    var epcMeesExemptionReason: MeesExemptionReason? = null

    @Column(nullable = false)
    val hasFireSafetyDeclaration: Boolean = true

    @Column(nullable = false)
    val hasKeepPropertySafeDeclaration: Boolean = true

    @Column(nullable = false)
    val hasResponsibilityToTenantsDeclaration: Boolean = true

    val hasEpcExemption: Boolean
        get() = epcExemptionReason != null

    // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled and make this a val again (gasSafetyCertExpiryDate)
    fun getGasSafetyCertExpiryDate(isGas14MonthExpiryEnabled: Boolean): LocalDate? =
        gasSafetyCertIssueDate?.let { CertificateExpiryHelper.getGasExpiryDate(it, isGas14MonthExpiryEnabled) }

    // TODO: PDJB-1926: Remove isGas14MonthExpiryEnabled and make this a val again
    fun isGasSafetyCertExpired(isGas14MonthExpiryEnabled: Boolean): Boolean? =
        gasSafetyCertIssueDate?.let { CertificateExpiryHelper.isGasIssueDateExpired(it, isGas14MonthExpiryEnabled) }

    val isGasSafetyCertMissing: Boolean
        get() = gasSafetyCertIssueDate == null && hasGasSupply == true

    val isEpcExpired: Boolean?
        get() = epcExpiryDate?.let { CertificateExpiryHelper.isEpcExpiryDateExpired(it) }

    val isEpcRatingLow: Boolean?
        get() {
            val rating = epcEnergyRating?.uppercase() ?: return null
            return if (rating in EPC_ACCEPTABLE_RATING_RANGE) {
                false
            } else {
                epcMeesExemptionReason == null
            }
        }

    val epcHasFaults: Boolean
        get() =
            (!hasEpcUrl && !hasEpcExemption) ||
                (isEpcRatingLow == true && (isEpcExpired == false || tenancyStartedBeforeEpcExpiry == true))

    val hasEpcUrl: Boolean
        get() = epcUrl != null

    val isEpcExpiredAfterTenancyStart: Boolean
        get() = isEpcExpired == true && tenancyStartedBeforeEpcExpiry == false

    val didEpcBecomeExpired: Boolean
        get() = isEpcExpired == true && tenancyStartedBeforeEpcExpiry == null

    val isEpcValidDespiteExpiry: Boolean
        get() = tenancyStartedBeforeEpcExpiry == true && isEpcRatingLow != true

    var hasGasSupply: Boolean? = null

    var gasSafetyCertProvideLater: Boolean? = null

    var electricalSafetyCertProvideLater: Boolean? = null

    var epcProvideLater: Boolean? = null

    constructor(
        propertyOwnership: PropertyOwnership,
        gasSafetyCertIssueDate: LocalDate? = null,
        hasGasSupply: Boolean? = null,
        gasSafetyFileUploads: MutableList<FileUpload> = mutableListOf(),
        eicrExpiryDate: LocalDate? = null,
        eicIssueDate: LocalDate? = null,
        electricalCertType: CertificateType? = null,
        epcUrl: String? = null,
        epcExpiryDate: LocalDate? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcEnergyRating: String? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
        gasSafetyCertProvideLater: Boolean? = null,
        electricalSafetyCertProvideLater: Boolean? = null,
        epcProvideLater: Boolean? = null,
    ) : this() {
        this.propertyOwnership = propertyOwnership
        this.gasSafetyCertIssueDate = gasSafetyCertIssueDate
        this.hasGasSupply = hasGasSupply
        this.gasSafetyFileUploads = gasSafetyFileUploads
        this.eicrExpiryDate = eicrExpiryDate
        this.eicIssueDate = eicIssueDate
        this.electricalCertType = electricalCertType
        this.epcUrl = epcUrl
        this.epcExpiryDate = epcExpiryDate
        this.tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry
        this.epcEnergyRating = epcEnergyRating
        this.epcExemptionReason = epcExemptionReason
        this.epcMeesExemptionReason = epcMeesExemptionReason
        this.gasSafetyCertProvideLater = gasSafetyCertProvideLater
        this.electricalSafetyCertProvideLater = electricalSafetyCertProvideLater
        this.epcProvideLater = epcProvideLater
    }
}
