package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType

@Entity
class License(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : ModifiableAuditableEntity() {
    // This only ever holds an actual licensed type (e.g. SELECTIVE_LICENCE, HMO_MANDATORY_LICENCE,
    // HMO_ADDITIONAL_LICENCE). A License is never created for NO_LICENSING or PROVIDE_LATER, so this must not be
    // used to determine a property's overall licensing state. Use PropertyOwnership.licenseType for that.
    @Column(nullable = false)
    lateinit var licenseType: LicensingType

    @Column(nullable = false)
    lateinit var licenseNumber: String

    constructor(licenseType: LicensingType, licenseNumber: String) : this() {
        this.licenseType = licenseType
        this.licenseNumber = licenseNumber
    }
}
