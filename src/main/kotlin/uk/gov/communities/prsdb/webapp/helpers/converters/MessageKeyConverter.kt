package uk.gov.communities.prsdb.webapp.helpers.converters

import uk.gov.communities.prsdb.webapp.constants.enums.BillsIncluded
import uk.gov.communities.prsdb.webapp.constants.enums.ComplianceCertStatus
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency

class MessageKeyConverter {
    companion object {
        fun convert(boolean: Boolean): String =
            when (boolean) {
                true -> "commonText.yes"
                false -> "commonText.no"
            }

        fun convert(enum: Enum<*>): String =
            when (enum) {
                is PropertyType -> convertPropertyType(enum)

                is OwnershipType -> convertOwnershipType(enum)

                is LicensingType -> convertLicensingType(enum)

                is FurnishedStatus -> convertFurnishedStatus(enum)

                is RentFrequency -> convertRentFrequency(enum)

                is BillsIncluded -> convertBillsIncluded(enum)

                is GasSafetyExemptionReason -> convertGasSafetyExemptionReason(enum)

                is EicrExemptionReason -> convertEicrExemptionReason(enum)

                is EpcExemptionReason -> convertEpcExemptionReason(enum)

                is MeesExemptionReason -> convertMeesExemptionReason(enum)

                is ComplianceCertStatus -> convertComplianceCertStatus(enum)

                else -> throw NotImplementedError(
                    "Was not able to convert Enum as ${this::class.simpleName} does not have a conversion for ${enum::class.simpleName}",
                )
            }

        private fun convertBillsIncluded(billsIncluded: BillsIncluded): String =
            when (billsIncluded) {
                BillsIncluded.GAS -> "forms.billsIncluded.checkbox.gas"
                BillsIncluded.ELECTRICITY -> "forms.billsIncluded.checkbox.electricity"
                BillsIncluded.WATER -> "forms.billsIncluded.checkbox.water"
                BillsIncluded.COUNCIL_TAX -> "forms.billsIncluded.checkbox.councilTax"
                BillsIncluded.CONTENTS_INSURANCE -> "forms.billsIncluded.checkbox.contentsInsurance"
                BillsIncluded.BROADBAND -> "forms.billsIncluded.checkbox.broadband"
                BillsIncluded.TV_LICENCE -> "forms.billsIncluded.checkbox.tvLicence"
                BillsIncluded.CABLE_OR_SATELLITE_TV -> "forms.billsIncluded.checkbox.cableSatelliteTV"
                BillsIncluded.GARDENING -> "forms.billsIncluded.checkbox.gardening"
                BillsIncluded.CLEANER_FOR_COMMUNAL_AREAS -> "forms.billsIncluded.checkbox.communalAreasCleaner"
                BillsIncluded.SOMETHING_ELSE -> ""
            }

        private fun convertRentFrequency(rentFrequency: RentFrequency): String =
            when (rentFrequency) {
                RentFrequency.WEEKLY -> "forms.rentFrequency.radios.option.weekly.label"
                RentFrequency.FOUR_WEEKLY -> "forms.rentFrequency.radios.option.fourWeekly.label"
                RentFrequency.MONTHLY -> "forms.rentFrequency.radios.option.monthly.label"
                RentFrequency.OTHER -> ""
            }

        private fun convertFurnishedStatus(furnishedStatus: FurnishedStatus): String =
            when (furnishedStatus) {
                FurnishedStatus.FURNISHED -> "forms.furnishedStatus.radios.options.furnished.label"
                FurnishedStatus.PART_FURNISHED -> "forms.furnishedStatus.radios.options.partFurnished.label"
                FurnishedStatus.UNFURNISHED -> "forms.furnishedStatus.radios.options.unfurnished.label"
            }

        private fun convertLicensingType(licensingType: LicensingType): String =
            when (licensingType) {
                LicensingType.SELECTIVE_LICENCE -> "forms.licensingType.radios.option.selectiveLicence.label"
                LicensingType.HMO_MANDATORY_LICENCE -> "forms.licensingType.radios.option.hmoMandatory.label"
                LicensingType.HMO_ADDITIONAL_LICENCE -> "forms.licensingType.radios.option.hmoAdditional.label"
                LicensingType.NO_LICENSING -> "forms.checkPropertyAnswers.propertyDetails.noLicensing"
            }

        private fun convertOwnershipType(ownershipType: OwnershipType): String =
            when (ownershipType) {
                OwnershipType.FREEHOLD -> "forms.ownershipType.radios.option.freehold.label"
                OwnershipType.LEASEHOLD -> "forms.ownershipType.radios.option.leasehold.label"
                OwnershipType.SHARE_OF_FREEHOLD -> "forms.ownershipType.radios.option.shareOfFreehold.label"
                OwnershipType.COMMONHOLD -> "forms.ownershipType.radios.option.commonhold.label"
            }

        private fun convertPropertyType(propertyType: PropertyType): String =
            when (propertyType) {
                PropertyType.OTHER -> "forms.propertyType.radios.option.other.label"
                PropertyType.DETACHED_HOUSE -> "forms.propertyType.radios.option.detachedHouse.label"
                PropertyType.SEMI_DETACHED_HOUSE -> "forms.propertyType.radios.option.semiDetachedHouse.label"
                PropertyType.TERRACED_HOUSE -> "forms.propertyType.radios.option.terracedHouse.label"
                PropertyType.FLAT -> "forms.propertyType.radios.option.flat.label"
            }

        private fun convertGasSafetyExemptionReason(gasSafetyExemptionReason: GasSafetyExemptionReason): String =
            when (gasSafetyExemptionReason) {
                GasSafetyExemptionReason.NO_GAS_SUPPLY -> "forms.gasSafetyExemptionReason.radios.noGas.label"
                GasSafetyExemptionReason.LONG_LEASE -> "forms.gasSafetyExemptionReason.radios.longLease.label"
                GasSafetyExemptionReason.OTHER -> "commonText.other"
            }

        private fun convertEicrExemptionReason(eicrExemptionReason: EicrExemptionReason): String =
            when (eicrExemptionReason) {
                EicrExemptionReason.LONG_LEASE -> "forms.eicrExemptionReason.radios.longLease.label"
                EicrExemptionReason.OTHER -> "commonText.other"
            }

        private fun convertEpcExemptionReason(epcExemptionReason: EpcExemptionReason): String =
            when (epcExemptionReason) {
                EpcExemptionReason.ANNUAL_USE_LESS_THAN_4_MONTHS -> {
                    "forms.epcExemptionReason.radios.annualUseLessThan4Months.label"
                }

                EpcExemptionReason.ANNUAL_ENERGY_CONSUMPTION_LESS_THAN_25_PERCENT -> {
                    "forms.epcExemptionReason.radios.annualEnergyConsumptionLessThan25Percent.label"
                }

                EpcExemptionReason.TEMPORARY_BUILDING -> {
                    "forms.epcExemptionReason.radios.temporaryBuilding.label"
                }

                EpcExemptionReason.STANDALONE_SMALL_BUILDING -> {
                    "forms.epcExemptionReason.radios.standaloneSmallBuilding.label"
                }

                EpcExemptionReason.DUE_FOR_DEMOLITION -> {
                    "forms.epcExemptionReason.radios.dueForDemolition.label"
                }
            }

        private fun convertMeesExemptionReason(meesExemptionReason: MeesExemptionReason): String =
            when (meesExemptionReason) {
                MeesExemptionReason.HIGH_COST -> "forms.meesExemptionReason.radios.highCost.label"
                MeesExemptionReason.ALL_IMPROVEMENTS_MADE -> "forms.meesExemptionReason.radios.allImprovementsMade.label"
                MeesExemptionReason.WALL_INSULATION -> "forms.meesExemptionReason.radios.wallInsulation.label"
                MeesExemptionReason.THIRD_PARTY_CONSENT -> "forms.meesExemptionReason.radios.thirdPartyConsent.label"
                MeesExemptionReason.PROPERTY_DEVALUATION -> "forms.meesExemptionReason.radios.propertyDevaluation.label"
                MeesExemptionReason.NEW_LANDLORD -> "forms.meesExemptionReason.radios.newLandlord.label"
            }

        private fun convertComplianceCertStatus(complianceCertStatus: ComplianceCertStatus): String =
            when (complianceCertStatus) {
                ComplianceCertStatus.NOT_STARTED -> "complianceActions.status.notStarted"
                ComplianceCertStatus.ADDED -> "complianceActions.status.added"
                ComplianceCertStatus.NOT_ADDED -> "complianceActions.status.notAdded"
                ComplianceCertStatus.EXPIRED -> "complianceActions.status.expired"
            }
    }
}
