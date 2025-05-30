package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
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

    @Column(name = "gas_safety_cert_s3_key")
    var gasSafetyCertS3Key: String? = null

    var gasSafetyCertIssueDate: LocalDate? = null

    var gasSafetyCertEngineerNum: String? = null

    var gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null

    var gasSafetyCertExemptionOtherReason: String? = null

    @Column(name = "eicr_s3_key")
    var eicrS3Key: String? = null

    var eicrIssueDate: LocalDate? = null

    var eicrExemptionReason: EicrExemptionReason? = null

    var eicrExemptionOtherReason: String? = null

    var epcUrl: String? = null

    var epcExpiryDate: LocalDate? = null

    var epcEnergyRating: String? = null

    var epcExemptionReason: EpcExemptionReason? = null

    var epcMeesExemptionReason: MeesExemptionReason? = null

    var hasFireSafetyDeclaration: Boolean = false

    val gasSafetyCertExpiryDate: LocalDate?
        get() = gasSafetyCertIssueDate?.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong())

    val eicrExpiryDate: LocalDate?
        get() = eicrIssueDate?.plusYears(EICR_VALIDITY_YEARS.toLong())

    constructor(
        propertyOwnership: PropertyOwnership,
        hasFireSafetyDeclaration: Boolean,
        gasSafetyCertS3Key: String? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyCertEngineerNum: String? = null,
        gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null,
        gasSafetyCertExemptionOtherReason: String? = null,
        eicrS3Key: String? = null,
        eicrIssueDate: LocalDate? = null,
        eicrExemptionReason: EicrExemptionReason? = null,
        eicrExemptionOtherReason: String? = null,
        epcUrl: String? = null,
        epcExpiryDate: LocalDate? = null,
        epcEnergyRating: String? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        epcMeesExemptionReason: MeesExemptionReason? = null,
    ) : this() {
        this.propertyOwnership = propertyOwnership
        this.gasSafetyCertS3Key = gasSafetyCertS3Key
        this.gasSafetyCertIssueDate = gasSafetyCertIssueDate
        this.gasSafetyCertEngineerNum = gasSafetyCertEngineerNum
        this.gasSafetyCertExemptionReason = gasSafetyCertExemptionReason
        this.gasSafetyCertExemptionOtherReason = gasSafetyCertExemptionOtherReason
        this.eicrS3Key = eicrS3Key
        this.eicrIssueDate = eicrIssueDate
        this.eicrExemptionReason = eicrExemptionReason
        this.eicrExemptionOtherReason = eicrExemptionOtherReason
        this.epcUrl = epcUrl
        this.epcExpiryDate = epcExpiryDate
        this.epcEnergyRating = epcEnergyRating
        this.epcExemptionReason = epcExemptionReason
        this.epcMeesExemptionReason = epcMeesExemptionReason
        this.hasFireSafetyDeclaration = hasFireSafetyDeclaration
    }
}
