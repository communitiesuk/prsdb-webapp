# Performance Testing

## Password hashing performance

To check how long passwords take to hash, log in as a system operator and call the `/system-operator/password-benchmark` endpoint.

You should receive a report like:

```
Argon2 benchmark: 50 hashes
iterations=2, memory=16384, parallelism=1
total: 2.788 s
average: 0.056 s
min: 0.046 s
max: 0.089 s
```

We should tune this to take around 1 second per hash, as per spring docs https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html.

You can pass query params to this endpoint to trial different hashing args, as well as how many hashes are completed.
