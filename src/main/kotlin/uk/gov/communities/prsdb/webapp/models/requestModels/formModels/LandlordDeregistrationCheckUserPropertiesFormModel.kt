package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

class LandlordDeregistrationCheckUserPropertiesFormModel : FormModel {
    var userHasRegisteredProperties: Boolean? = null

    companion object {
        const val USER_HAS_REGISTERED_PROPERTIES_JOURNEY_DATA_KEY = "userHasRegisteredProperties"
    }
}
