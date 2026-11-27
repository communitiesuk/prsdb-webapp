package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

data class GovUkPayCreatedMotoPayment(
    val payment: GovUkPayPayment,
    val oneTimeToken: String,
)
