package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Entity
class Address() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long = 0

    @Column(unique = true)
    var uprn: Long? = null
        private set

    @Column(nullable = false)
    lateinit var singleLineAddress: String
        private set

    var organisation: String? = null
        private set

    var subBuilding: String? = null
        private set

    var buildingName: String? = null
        private set

    var buildingNumber: String? = null
        private set

    var streetName: String? = null
        private set

    var locality: String? = null
        private set

    var townName: String? = null
        private set

    var postcode: String? = null
        private set

    @ManyToOne
    @JoinColumn(foreignKey = ForeignKey(name = "FK_ADDRESS_LA"))
    var localAuthority: LocalAuthority? = null
        private set

    constructor(addressDataModel: AddressDataModel, localAuthority: LocalAuthority? = null) : this() {
        this.uprn = addressDataModel.uprn
        this.singleLineAddress = addressDataModel.singleLineAddress
        this.organisation = addressDataModel.organisation
        this.subBuilding = addressDataModel.subBuilding
        this.buildingName = addressDataModel.buildingName
        this.buildingNumber = addressDataModel.buildingNumber
        this.streetName = addressDataModel.streetName
        this.locality = addressDataModel.locality
        this.townName = addressDataModel.townName
        this.postcode = addressDataModel.postcode
        this.townName = addressDataModel.townName
        this.postcode = addressDataModel.postcode
        this.localAuthority = localAuthority
    }
}
