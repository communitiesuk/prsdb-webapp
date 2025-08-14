package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.EPC_ACCEPTABLE_RATING_RANGE
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
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

    @OneToOne
    @JoinColumn(
        name = "property_ownership_id",
        nullable = false,
        foreignKey = ForeignKey(name = "FK_PROPERTY_COMPLIANCE_PROPERTY_OWNERSHIP"),
    )
    lateinit var propertyOwnership: PropertyOwnership
        private set

    @OneToOne
    @JoinColumn(
        name = "gas_safety_upload_id",
        nullable = true,
        foreignKey = ForeignKey(name = "FK_PROPERTY_COMPLIANCE_GAS_SAFETY_UPLOAD"),
    )
    var gasSafetyFileUpload: FileUpload? = null

    var gasSafetyCertIssueDate: LocalDate? = null

    var gasSafetyCertEngineerNum: String? = null

    var gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null

    var gasSafetyCertExemptionOtherReason: String? = null

    @OneToOne
    @JoinColumn(
        name = "eicr_id",
        nullable = true,
        foreignKey = ForeignKey(name = "FK_PROPERTY_COMPLIANCE_EICR_UPLOAD"),
    )
    var eicrFileUpload: FileUpload? = null

    var eicrIssueDate: LocalDate? = null

    var eicrExemptionReason: EicrExemptionReason? = null

    var eicrExemptionOtherReason: String? = null

    var epcUrl: String? = null

    var epcExpiryDate: LocalDate? = null

    var tenancyStartedBeforeEpcExpiry: Boolean? = null

    var epcEnergyRating: String? = null

    var epcExemptionReason: EpcExemptionReason? = null

    var epcMeesExemptionReason: MeesExemptionReason? = null

    val hasFireSafetyDeclaration: Boolean = true

    val hasKeepPropertySafeDeclaration: Boolean = true

    val hasResponsibilityToTenantsDeclaration: Boolean = true

    val gasSafetyCertS3Key: String?
        get() = gasSafetyFileUpload?.objectKey

    val eicrS3Key: String?
        get() = eicrFileUpload?.objectKey

    val hasGasSafetyExemption: Boolean
        get() = gasSafetyCertExemptionReason != null

    val hasEicrExemption: Boolean
        get() = eicrExemptionReason != null

    val hasEpcExemption: Boolean
        get() = epcExemptionReason != null

    val gasSafetyCertExpiryDate: LocalDate?
        get() = gasSafetyCertIssueDate?.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())

    val eicrExpiryDate: LocalDate?
        get() = eicrIssueDate?.plusYears(EICR_VALIDITY_YEARS.toLong())

    val isGasSafetyCertExpired: Boolean?
        get() = gasSafetyCertExpiryDate?.let { !it.isAfter(LocalDate.now()) }

    val isGasSafetyCertMissing: Boolean
        get() = gasSafetyCertIssueDate == null && !hasGasSafetyExemption

    val isEicrExpired: Boolean?
        get() = eicrExpiryDate?.let { !it.isAfter(LocalDate.now()) }

    val isEicrMissing: Boolean
        get() = eicrIssueDate == null && !hasEicrExemption

    val isEpcExpired: Boolean?
        get() {
            val isPastExpiryDate = epcExpiryDate?.isBefore(LocalDate.now()) ?: return null
            return if (!isPastExpiryDate) {
                false
            } else {
                tenancyStartedBeforeEpcExpiry?.not()
            }
        }

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
        get() = epcUrl == null && !hasEpcExemption

    constructor(
        propertyOwnership: PropertyOwnership,
        gasSafetyCertUpload: FileUpload? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyCertEngineerNum: String? = null,
        gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
        gasSafetyCertExemptionOtherReason: String? = null,
        eicrUpload: FileUpload? = null,
        eicrIssueDate: LocalDate? = null,
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
