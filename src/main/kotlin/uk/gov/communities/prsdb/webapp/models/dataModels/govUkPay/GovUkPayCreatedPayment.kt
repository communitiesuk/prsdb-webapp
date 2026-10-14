package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

data class GovUkPayCreatedPayment(
    val payment: GovUkPayPayment,
    val nextUrl: String,
)
