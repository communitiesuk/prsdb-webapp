package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import java.time.LocalDate

@Entity
class PropertyCompliance : ModifiableAuditableEntity() {
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

    var gasSafetyCertExpiryDate: LocalDate? = null
        private set

    var gasSafetyCertEngineerNum: String? = null

    var gasSafetyCertExemptionReason: GasSafetyExemptionReason? = null

    var gasSafetyCertExemptionOtherReason: String? = null

    @Column(name = "eicr_s3_key")
    var eicrS3Key: String? = null

    var eicrIssueDate: LocalDate? = null

    var eicrExpiryDate: LocalDate? = null
        private set

    var eicrExemptionReason: EicrExemptionReason? = null

    var eicrExemptionOtherReason: String? = null

    var epcUrl: String? = null

    var epcExpiryDate: LocalDate? = null

    var epcEnergyRating: String? = null

    var epcExemptionReason: EicrExemptionReason? = null

    var epcMeesExemptionReason: MeesExemptionReason? = null

    var hasFireSafetyDeclaration: Boolean = false
}
