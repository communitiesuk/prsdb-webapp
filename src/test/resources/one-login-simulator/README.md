# GOV.UK One Login simulator fixtures

These fixtures are synthetic and contain no personal data. They are submitted through the
official simulator's interactive-mode form by
`OneLoginSimulatorIntegrationTests`.

The JSON shapes and configured claims follow the simulator documentation for release
[`26.09.4`](https://github.com/govuk-one-login/simulator/tree/26.09.4):

- [Configuration](https://github.com/govuk-one-login/simulator/blob/26.09.4/docs/configuration.md)
- [Interactive mode](https://github.com/govuk-one-login/simulator/blob/26.09.4/docs/interactive-mode.md)
- [Default configuration](https://github.com/govuk-one-login/simulator/blob/26.09.4/docs/default-config-values.md)

The core-identity and address structures also follow the [GOV.UK One Login identity
claim guidance](https://docs.sign-in.service.gov.uk/integrate-with-integration-environment/prove-users-identity/).
Their values are only sufficient for the webapp's existing identity-claim parsing contract.

The simulator accepts any string as a return-code `code`. `"00"` is a synthetic value used
only to exercise the return-code claim requested by the webapp; this test does not interpret
or assert its meaning.
