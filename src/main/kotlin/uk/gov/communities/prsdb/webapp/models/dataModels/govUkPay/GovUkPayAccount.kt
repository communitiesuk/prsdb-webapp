package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

// Each account is a separate GOV.UK Pay service with its own API key; a payment can only be read or changed through its own account
enum class GovUkPayAccount {
    ONLINE,
    MOTO,
}
