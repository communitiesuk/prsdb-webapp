# Compliance Certifcates on PRSDB

The compliance flow on PRSDB has a number of branching paths.
This document will break down, for the 3 certificate types, what properties the certificate can have, and how we combine these properties together to display on the site.

# Certificate state

Once the compliance steps has been completed, the certificate can be in one of 4 states.
- `ADDED`: Certificate is valid to be used for the property.
- `PROVIDE_LATER`: The user has opted to provide their certificate later, by pressing the 'Provide later' button at the start of the flow.
- `EXPIRED`: The certificate was valid but is now expired.
- `HAS_FAULTS`: The certificate has an issue and cannot be used for this property. For example, the EPC has a low rating (and no exemption), the property doesn't have an EPC (and no exemption). This takes precedence over expired.

The possible states for a certificate are:

## Gas

| State                                                    | If occupied   | If unoccupied    |
|----------------------------------------------------------|---------------|------------------|
| Has gas, provide it later                                | PROVIDE_LATER | PROVIDE_LATER    |
| Has gas, has certificate, not expired                    | VALID         | VALID            |
| Has gas, has certificate, it's expired                   | EXPIRED       | EXPIRED          |
| Has gas, no certificate                                  | HAS_FAULTS    | PROVIDE_LATER[1] |
| No gas                                                   | VALID         | VALID            |

[1]: If a cert has faults but the property is unoccupied, we treat as just provide later

## Electric

| State                                       | If occupied   | If unoccupied |
|---------------------------------------------|---------------|---------------|
| Provide it later                            | PROVIDE_LATER | PROVIDE_LATER |
| Has certificate, not expired                | VALID         | VALID         |
| Has certificate, it's expired               | EXPIRED[1]    | EXPIRED       |
| No certificate                              | HAS_FAULTS    | PROVIDE_LATER |

[1]: Note that for gas & electric, when checking your records there is no functional difference between whether the certificate was expired when you uploaded it or later expired.

## EPC

| State                                                                       | If occupied   | If unoccupied |
|-----------------------------------------------------------------------------|---------------|---------------|
| Provide it later                                                            | PROVIDE_LATER | PROVIDE_LATER |
| Has EPC, not expired, high rating                                           | VALID         | VALID         |
| Has EPC, not expired, low rating, has exemption                             | VALID         | VALID         |
| Has EPC, not expired, low rating, no exemption                              | HAS_FAULTS    | PROVIDE_LATER |
| Has EPC, expired, EPC in date when tenancy began, high rating               | EXPIRED[2]    | EXPIRED[1]    |
| Has EPC, expired, EPC in date when tenancy began, low rating, has exemption | EXPIRED[2]    | EXPIRED[1]    |
| Has EPC, expired, EPC in date when tenancy began, low rating, no exemption  | HAS_FAULTS    | EXPIRED[1]    |
| Has EPC, expired, EPC not in date when tenancy began                        | EXPIRED       | EXPIRED[1]    |
| No EPC, it is required                                                      | HAS_FAULTS    | PROVIDE_LATER |
| No EPC, not required                                                        | VALID         | VALID         |

[1]: In the case of EPC and expired for unoccupied properties we do not check the EPC rating or ask whether it has an exemption, so in all cases we class as expired.
We prefer to only show 'HAS_FAULTS' if we know for certain that the EPC is not valid.
For these cases, consider the state as 'Has EPC, expired'.
[2]: We show 'Expired' for these technically valid certificates since we can't reliably tell when the landlord re-lets the property.
See PDJB-979 for when we look to improve this.

In gas & electric we will have files for certificates that went on to expire but we do not keep certificates that expired on upload.
EPC always keeps a record of the certificate if it was uploaded while expired.
Having the certificate files or not does not matter for compliance, just the expiry/issue date of the certificate.

# Compliance action

We consider that a certificate needs action if any of these are true:
- It is expired
- It has faults and the property is occupied
- It is provide later and the property is occupied