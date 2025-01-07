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
    val id: Long? = null,
) : ModifiableAuditableEntity() {
    @Column(nullable = false)
    lateinit var licenseType: LicensingType
        private set

    @Column(nullable = false)
    lateinit var licenseNumber: String
        private set

    constructor(licenseType: LicensingType, licenseNumber: String) : this() {
        this.licenseType = licenseType
        this.licenseNumber = licenseNumber
    }
}
