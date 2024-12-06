package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@Entity
class Address() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @Column(unique = true)
    var uprn: Long? = null
        private set

    @Column(nullable = false)
    lateinit var singleLineAddress: String
        private set

    lateinit var organisation: String
        private set

    lateinit var subBuilding: String
        private set

    lateinit var buildingName: String
        private set

    lateinit var buildingNumber: String
        private set

    lateinit var streetName: String
        private set

    lateinit var locality: String
        private set

    lateinit var townName: String
        private set

    lateinit var postcode: String
        private set

    lateinit var custodianCode: String
        private set

    constructor(addressDataModel: AddressDataModel) : this() {
        this.uprn = addressDataModel.uprn
        this.singleLineAddress = addressDataModel.singleLineAddress
        if (addressDataModel.organisation != null) this.organisation = addressDataModel.organisation
        if (addressDataModel.subBuilding != null) this.subBuilding = addressDataModel.subBuilding
        if (addressDataModel.buildingName != null) this.buildingName = addressDataModel.buildingName
        if (addressDataModel.buildingNumber != null) this.buildingNumber = addressDataModel.buildingNumber
        if (addressDataModel.streetName != null) this.streetName = addressDataModel.streetName
        if (addressDataModel.locality != null) this.locality = addressDataModel.locality
        if (addressDataModel.townName != null) this.townName = addressDataModel.townName
        if (addressDataModel.postcode != null) this.postcode = addressDataModel.postcode
        if (addressDataModel.townName != null) this.townName = addressDataModel.townName
        if (addressDataModel.postcode != null) this.postcode = addressDataModel.postcode
        if (addressDataModel.custodianCode != null) this.custodianCode = addressDataModel.custodianCode
    }
}
