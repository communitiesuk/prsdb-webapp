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
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
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

    @OneToOne(optional = true)
    @JoinColumn(name = "gas_safety_upload_id", nullable = true, unique = true)
    var gasSafetyFileUpload: FileUpload? = null

    @OneToMany()
    @JoinTable(name = "gas_safety_uploads")
    var gasSafetyFileUploads: MutableList<FileUpload> = mutableListOf()

    var gasSafetyCertIssueDate: LocalDate? = null

    var gasSafetyCertEngineerNum: String? = null

    var gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null

    var gasSafetyCertExemptionOtherReason: String? = null

    var hasGasSupply: Boolean? = null

    @OneToOne(optional = true)
    @JoinColumn(name = "eicr_upload_id", nullable = true, unique = true)
    var eicrFileUpload: FileUpload? = null

    @OneToMany()
    @JoinTable(name = "electrical_safety_uploads")
    var electricalSafetyFileUploads: MutableList<FileUpload> = mutableListOf()

    // TODO PDJB-766: Remove eicrIssueDate once the compliance update journey uses expiry date instead
    var eicrIssueDate: LocalDate? = null

    var eicrExemptionReason: EicrExemptionReason? = null

    var eicrExemptionOtherReason: String? = null

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

    val gasSafetyCertS3Key: String?
        get() = gasSafetyFileUpload?.objectKey

    val eicrS3Key: String?
        get() = eicrFileUpload?.objectKey

    val hasGasSafetyExemption: Boolean
        get() = gasSafetyCertExemptionReason != null

    val hasElectricalSafetyExemption: Boolean
        get() = eicrExemptionReason != null

    val hasEpcExemption: Boolean
        get() = epcExemptionReason != null

    val gasSafetyCertExpiryDate: LocalDate?
        get() = gasSafetyCertIssueDate?.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())

    val isGasSafetyCertExpired: Boolean?
        get() = gasSafetyCertExpiryDate?.let { !it.isAfter(LocalDate.now()) }

    val isGasSafetyCertMissing: Boolean
        get() = gasSafetyCertIssueDate == null && !hasGasSafetyExemption

    val isElectricalSafetyExpired: Boolean?
        get() = electricalSafetyExpiryDate?.let { !it.isAfter(LocalDate.now()) }

    val isElectricalSafetyMissing: Boolean
        get() = electricalSafetyExpiryDate == null && !hasElectricalSafetyExemption

    val isEpcExpired: Boolean?
        get() = epcExpiryDate?.isBefore(LocalDate.now())

    val isEpcNonCompliantDueToExpiry: Boolean?
        get() = if (isEpcExpired == true) tenancyStartedBeforeEpcExpiry != true else isEpcExpired

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

    constructor(
        propertyOwnership: PropertyOwnership,
        gasSafetyCertUpload: FileUpload? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyCertEngineerNum: String? = null,
        gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
        gasSafetyCertExemptionOtherReason: String? = null,
        eicrUpload: FileUpload? = null,
        eicrIssueDate: LocalDate? = null,
        electricalSafetyExpiryDate: LocalDate? = null,
        eicrExemptionReason: EicrExemptionReason? = null,
        eicrExemptionOtherReason: String? = null,
        epcUrl: String? = null,
        epcExpiryDate: LocalDate? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcEnergyRating: String? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
    ) : this() {
        this.propertyOwnership = propertyOwnership
        this.gasSafetyFileUpload = gasSafetyCertUpload
        this.gasSafetyCertIssueDate = gasSafetyCertIssueDate
        this.gasSafetyCertEngineerNum = gasSafetyCertEngineerNum
        this.gasSafetyCertExemptionReason = gasSafetyCertExemptionReason
        this.gasSafetyCertExemptionOtherReason = gasSafetyCertExemptionOtherReason
        this.eicrFileUpload = eicrUpload
        this.eicrIssueDate = eicrIssueDate
        this.electricalSafetyExpiryDate = electricalSafetyExpiryDate
        this.eicrExemptionReason = eicrExemptionReason
        this.eicrExemptionOtherReason = eicrExemptionOtherReason
        this.epcUrl = epcUrl
        this.epcExpiryDate = epcExpiryDate
        this.tenancyStartedBeforeEpcExpiry = tenancyStartedBeforeEpcExpiry
        this.epcEnergyRating = epcEnergyRating
        this.epcExemptionReason = epcExemptionReason
        this.epcMeesExemptionReason = epcMeesExemptionReason
    }
}
