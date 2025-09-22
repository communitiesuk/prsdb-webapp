package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Comment
import uk.gov.communities.prsdb.webapp.services.NgdAddressService.Companion.DATA_PACKAGE_VERSION_COMMENT_PREFIX

@Entity
@Comment(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
class NgdAddress() : ModifiableAuditableEntity() {
    @Id
    var uprn: Int = 0
        private set

    var organisationName: String? = null
        private set

    var subname: String? = null
        private set

    var name: String? = null
        private set

    var number: String? = null
        private set

    @Column(nullable = false)
    lateinit var streetName: String
        private set

    var locality: String? = null
        private set

    var townName: String? = null
        private set

    @Column(nullable = false)
    lateinit var postcode: String
        private set

    @Column(nullable = false)
    lateinit var fullAddress: String
        private set

    @Column(nullable = false)
    var localCustodianCode: Int = 0
        private set

    constructor(
        uprn: Int,
        organisationName: String?,
        subname: String?,
        name: String?,
        number: String?,
        streetName: String,
        locality: String?,
        townName: String?,
        postcode: String,
        fullAddress: String,
        localCustodianCode: Int,
    ) : this() {
        this.uprn = uprn
        this.organisationName = organisationName
        this.subname = subname
        this.name = name
        this.number = number
        this.streetName = streetName
        this.locality = locality
        this.townName = townName
        this.postcode = postcode
        this.fullAddress = fullAddress
        this.localCustodianCode = localCustodianCode
    }
}
