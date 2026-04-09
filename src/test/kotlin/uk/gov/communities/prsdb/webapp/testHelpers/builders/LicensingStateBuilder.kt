package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoAdditionalLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HmoMandatoryLicenceFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel

interface LicensingStateBuilder<SelfType : LicensingStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withLicensingType(licensingType: LicensingType): SelfType {
        val licensingTypeFormModel =
            LicensingTypeFormModel().apply {
                this.licensingType = licensingType
            }
        withSubmittedValue("licensing-type", licensingTypeFormModel)
        return self()
    }

    fun withLicensing(
        licensingType: LicensingType,
        licenseNumber: String? = null,
    ): SelfType {
        withLicensingType(licensingType)
        when (licensingType) {
            LicensingType.SELECTIVE_LICENCE -> withLicenceNumber("selective-licence", licenseNumber)
            LicensingType.HMO_MANDATORY_LICENCE -> withLicenceNumber("hmo-mandatory-licence", licenseNumber)
            LicensingType.HMO_ADDITIONAL_LICENCE -> withLicenceNumber("hmo-additional-licence", licenseNumber)
            LicensingType.NO_LICENSING -> {}
        }
        return self()
    }

    fun withLicenceNumber(
        urlPathSegment: String,
        licenceNumber: String?,
    ): SelfType {
        val formModel =
            when (urlPathSegment) {
                "selective-licence" -> SelectiveLicenceFormModel().apply { this.licenceNumber = licenceNumber }
                "hmo-mandatory-licence" -> HmoMandatoryLicenceFormModel().apply { this.licenceNumber = licenceNumber }
                "hmo-additional-licence" -> HmoAdditionalLicenceFormModel().apply { this.licenceNumber = licenceNumber }
                else -> throw IllegalArgumentException("Unknown licence type: $urlPathSegment")
            }
        withSubmittedValue(urlPathSegment, formModel)
        return self()
    }
}
