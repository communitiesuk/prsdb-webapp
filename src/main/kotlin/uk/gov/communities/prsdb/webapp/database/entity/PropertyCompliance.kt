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
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
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

    var electricalSafetyExpiryDate: LocalDate? = null

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

    val gasSafetyCertExpiryDate: LocalDate?
        get() = gasSafetyCertIssueDate?.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())

    val isGasSafetyCertExpired: Boolean?
        get() = gasSafetyCertExpiryDate?.let { !it.isAfter(LocalDate.now()) }

    val isGasSafetyCertMissing: Boolean
        get() = gasSafetyCertIssueDate == null

    val isElectricalSafetyExpired: Boolean?
        get() = electricalSafetyExpiryDate?.let { !it.isAfter(LocalDate.now()) }

    val isElectricalSafetyMissing: Boolean
        get() = electricalSafetyExpiryDate == null

    val isEpcExpired: Boolean?
        get() = epcExpiryDate?.isBefore(LocalDate.now())

    val isEpcNonCompliantDueToExpiry: Boolean
        get() = isEpcExpired == true && tenancyStartedBeforeEpcExpiry != true

    val isEpcRatingLow: Boolean?
        get() {
            val rating = epcEnergyRating?.uppercase() ?: return null
            return if (rating in EPC_ACCEPTABLE_RATING_RANGE) {
                false
            } else {
                epcMeesExemptionReason == null
            }
        }

    val isEpcMissing: Boolean
        get() = (epcUrl == null && !hasEpcExemption) || (isEpcRatingLow == true && epcMeesExemptionReason == null)

    var hasGasSupply: Boolean? = null

    var gasSafetyCertProvideLater: Boolean? = null

    var electricalSafetyCertProvideLater: Boolean? = null

    var epcProvideLater: Boolean? = null

    constructor(
        propertyOwnership: PropertyOwnership,
        gasSafetyCertIssueDate: LocalDate? = null,
        hasGasSupply: Boolean? = null,
        gasSafetyFileUploads: MutableList<FileUpload> = mutableListOf(),
        electricalSafetyExpiryDate: LocalDate? = null,
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
        this.electricalSafetyExpiryDate = electricalSafetyExpiryDate
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
