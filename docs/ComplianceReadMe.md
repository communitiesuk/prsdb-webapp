# Compliance Certifcates on PRSDB

The compliance flow on PRSDB has a number of branching paths.
This document will break down, for the 3 certificate types, what properties the certificate can have, and how we combine these properties together to display on the site.

# Certificate state

Once the compliance journey has been completed, the certificate can be in one of 4 states.
- `ADDED`: Certificate is valid to be used for the property.
- `PROVIDE_LATER`: The user has opted to provide their certificate later, by pressing the 'Provide later' button at the start of the flow.
- `EXPIRED`: The certificate was valid but is now expired.
- `HAS_FAULTS`: The certificate has an issue and cannot be used for this property. This takes precedence over expired.

The possible states for a certificate are:

## Gas

| State                                                    | If occupied   | If unoccupied |
|----------------------------------------------------------|---------------|---------------|
| Has gas, provide it later                                | PROVIDE_LATER | PROVIDE_LATER |
| Has gas, has certificate, not expired                    | VALID         | VALID         |
| Has gas, has certificate, it's expired                   | EXPIRED       | EXPIRED       |
| Has gas, no certificate                                  | HAS_FAULTS    | HAS_FAULTS    |
| No gas                                                   | VALID         | VALID         |

## Electric

| State                                       | If occupied   | If unoccupied |
|---------------------------------------------|---------------|---------------|
| Provide it later                            | PROVIDE_LATER | PROVIDE_LATER |
| Has certificate, not expired                | VALID         | VALID         |
| Has certificate, it's expired               | EXPIRED       | EXPIRED       |
| No certificate                              | HAS_FAULTS    | HAS_FAULTS    |

## EPC

| State                                                                       | If occupied   | If unoccupied |
|-----------------------------------------------------------------------------|---------------|---------------|
| Provide it later                                                            | PROVIDE_LATER | PROVIDE_LATER |
| Has EPC, not expired, high rating                                           | VALID         | VALID         |
| Has EPC, not expired, low rating, has exemption                             | VALID         | VALID         |
| Has EPC, not expired, low rating, no exemption                              | HAS_FAULTS    | HAS_FAULTS    |
| Has EPC, expired, EPC in date when tenancy began, high rating               | EXPIRED[2]    | EXPIRED[1]    |
| Has EPC, expired, EPC in date when tenancy began, low rating, has exemption | EXPIRED       | EXPIRED[1]    |
| Has EPC, expired, EPC in date when tenancy began, low rating, no exemption  | HAS_FAULTS    | EXPIRED[1][2] |
| Has EPC, expired, EPC not in date when tenancy began                        | EXPIRED       | EXPIRED[1]    |
| No EPC, it is required                                                      | HAS_FAULTS    | HAS_FAULTS    |
| No EPC, not required                                                        | VALID         | VALID         |

[1]: In the case of EPC and expired for unoccupied properties we do not check the EPC rating or ask whether it has an exemption, so in all cases we class as expired.
We prefer to only show 'HAS_FAULTS' if we know for certain that the EPC is not valid.

[2]: Note that when checking your records there is no functional difference between whether the certificate was expired when you uploaded it or later expired.
In gas & electric we will have files for certificates that went on to expire but we do not keep certificates that expired on upload.
EPC always keeps a record of the certificate if it was uploaded while expired.
Having the certificate files or not does not matter for compliance, just the expiry/issue date of the certificate.

# Compliance action

We consider that a certificate needs action if any of these are true:
- It is expired
- It has faults and the property is occupied
- It is provide later and the property is occupied