package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Comment
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader.Companion.DATA_PACKAGE_VERSION_COMMENT_PREFIX

@Entity
@Comment(DATA_PACKAGE_VERSION_COMMENT_PREFIX)
class Address() : ModifiableAuditableEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(unique = true)
    var uprn: Long? = null
        private set

    @Column(nullable = false, length = SINGLE_LINE_ADDRESS_LENGTH)
    lateinit var singleLineAddress: String
        private set

    var organisation: String? = null
        private set

    @Column(length = 500)
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

    @Column(nullable = false)
    var postcode: String? = null
        private set

    @ManyToOne
    @JoinColumn(name = "local_council_id")
    var localCouncil: LocalCouncil? = null
        private set

    @Column(nullable = false)
    var isActive: Boolean = true
        private set

    constructor(addressDataModel: AddressDataModel, localCouncil: LocalCouncil? = null) : this() {
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
        this.localCouncil = localCouncil
    }

    fun getSelectedAddress(): String = if (uprn == null) MANUAL_ADDRESS_CHOSEN else singleLineAddress

    fun toMultiLineAddress(): String =
        if (hasAddressComponents()) {
            buildMultiLineAddressFromComponents()
        } else {
            buildMultiLineAddressFromSingleLine(singleLineAddress)
        }

    private fun hasAddressComponents(): Boolean = streetName != null || buildingName != null || buildingNumber != null

    private fun buildMultiLineAddressFromComponents(): String =
        listOfNotNull(
            organisation,
            subBuilding,
            buildingName,
            listOfNotNull(buildingNumber, streetName).joinToString(" ").ifBlank { null },
            locality,
            townName,
            postcode,
        ).joinToString("\n")

    companion object {
        const val SINGLE_LINE_ADDRESS_LENGTH = 1000

        private val HOUSE_NUMBER_REGEX = Regex("^\\d+[A-Za-z]?$")
        private val UK_POSTCODE_REGEX = Regex("^[A-Z]{1,2}\\d[A-Z\\d]? ?\\d[A-Z]{2}$", RegexOption.IGNORE_CASE)

        private fun buildMultiLineAddressFromSingleLine(singleLineAddress: String): String {
            val parts = singleLineAddress.split(", ")
            val merged = mutableListOf<String>()
            var i = 0
            while (i < parts.size) {
                val current = parts[i]
                val next = parts.getOrNull(i + 1)
                if (current.matches(HOUSE_NUMBER_REGEX) && next != null && !next.matches(UK_POSTCODE_REGEX)) {
                    merged.add("$current $next")
                    i += 2
                } else {
                    merged.add(current)
                    i += 1
                }
            }
            return merged.joinToString("\n")
        }
    }
}
