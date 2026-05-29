package uk.gov.communities.prsdb.webapp.database.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import java.math.BigDecimal
import java.time.LocalDate

@Embeddable
class TenancyDetails() {
    @Column(nullable = false)
    var currentNumHouseholds: Int = 0

    @Column(nullable = false)
    var currentNumTenants: Int = 0

    var lastOccupiedDate: LocalDate? = null

    var furnishedStatus: FurnishedStatus? = null

    var rentFrequency: RentFrequency? = null

    var customRentFrequency: String? = null

    @Column(precision = 9, scale = 2)
    var rentAmount: BigDecimal? = null

    var billsIncludedList: String? = null

    var customBillsIncluded: String? = null

    val isOccupied: Boolean
        get() = currentNumTenants > 0

    val rentIncludesBills: Boolean
        get() = billsIncludedList != null

    constructor(
        currentNumHouseholds: Int,
        currentNumTenants: Int,
        lastOccupiedDate: LocalDate? = null,
        furnishedStatus: FurnishedStatus? = null,
        rentFrequency: RentFrequency? = null,
        customRentFrequency: String? = null,
        rentAmount: BigDecimal? = null,
        billsIncludedList: String? = null,
        customBillsIncluded: String? = null,
    ) : this() {
        this.currentNumHouseholds = currentNumHouseholds
        this.currentNumTenants = currentNumTenants
        this.lastOccupiedDate = lastOccupiedDate
        this.furnishedStatus = furnishedStatus
        this.rentFrequency = rentFrequency
        this.customRentFrequency = customRentFrequency
        this.rentAmount = rentAmount
        this.billsIncludedList = billsIncludedList
        this.customBillsIncluded = customBillsIncluded
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TenancyDetails) return false
        return currentNumHouseholds == other.currentNumHouseholds &&
            currentNumTenants == other.currentNumTenants &&
            lastOccupiedDate == other.lastOccupiedDate &&
            furnishedStatus == other.furnishedStatus &&
            rentFrequency == other.rentFrequency &&
            customRentFrequency == other.customRentFrequency &&
            (rentAmount?.compareTo(other.rentAmount) == 0 || rentAmount == other.rentAmount) &&
            billsIncludedList == other.billsIncludedList &&
            customBillsIncluded == other.customBillsIncluded
    }

    override fun hashCode(): Int {
        var result = currentNumHouseholds
        result = 31 * result + currentNumTenants
        result = 31 * result + (lastOccupiedDate?.hashCode() ?: 0)
        result = 31 * result + (furnishedStatus?.hashCode() ?: 0)
        result = 31 * result + (rentFrequency?.hashCode() ?: 0)
        result = 31 * result + (customRentFrequency?.hashCode() ?: 0)
        result = 31 * result + (rentAmount?.hashCode() ?: 0)
        result = 31 * result + (billsIncludedList?.hashCode() ?: 0)
        result = 31 * result + (customBillsIncluded?.hashCode() ?: 0)
        return result
    }
}
