# API Gateway

Reactive API Gateway using Java 21, Spring Boot 4.1.2 and Spring Cloud 2025.1.4.

## Included

- Spring Cloud Gateway Server WebFlux
- Eureka discovery client
- Spring Cloud LoadBalancer (`lb://SERVICE-NAME`)
- Bucket4j token-bucket rate limiting
- Caffeine cache for per-client buckets and idle eviction
- Request logging
- Actuator and Prometheus endpoint


## Prerequisites

- JDK 21
- Maven 3.9+
- Eureka Server running at `http://localhost:8761`
- Existing downstream services registered with Eureka

## Run

```bash
mvn clean compile
mvn spring-boot:run
```

Gateway listens on `http://localhost:8080`.

## Configure routes

Edit `src/main/resources/application.yml`. The service name after `lb://` must match the application name registered in Eureka.

Example:

```yaml
- id: auth-service
  uri: lb://AUTH-SERVICE
  predicates:
    - Path=/api/auth/**
```

The gateway preserves the request path by default. Add `StripPrefix` only if your downstream service expects the prefix to be removed.

## Rate limiter defaults

- Bucket capacity: 100 tokens
- Refill: 100 tokens over 60 seconds, using greedy refill
- Client key: remote IP address
- Idle bucket expiration: 30 minutes
- Maximum cached client buckets: 100,000

Successful requests include `X-Rate-Limit-Remaining`.
Rejected requests return HTTP 429 and a `Retry-After` header.

Override values under `gateway.rate-limit` in `application.yml`.

## Endpoints

- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/actuator/metrics`
- `GET http://localhost:8080/actuator/prometheus`

Prometheus scraping must be configured separately.

## Important limitations

1. Buckets are stored in this gateway process. Multiple gateway replicas have independent counters; quotas are not globally shared.
2. The remote IP is used as the client key. Behind a reverse proxy, configure trusted proxy handling before relying on IP-based limits. Do not trust arbitrary forwarded-IP headers.
3. Caffeine eviction removes bucket state. If a bucket is evicted, that client can receive a fresh bucket before its normal refill period. Choose the maximum cache size based on expected client cardinality.
4. Eureka registration alone is not a complete health guarantee. Test instance failure and recovery.
5. This project does not validate JWTs. Your existing authentication service remains separate; secure the gateway and downstream services according to your existing authentication design.

## Test API

1..150 | ForEach-Object {
try {
$response = Invoke-WebRequest `
            -Uri "http://localhost:8080/api/posts" `
-Method GET `
-ErrorAction Stop

        $statusCode = [int]$response.StatusCode
    }
    catch {
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }
        else {
            $statusCode = "ERROR"
        }
    }

    Write-Host "Request $_ : $statusCode"
}                                                                                                              >>     try {                                                                                                            >>         $response = Invoke-WebRequest `                                                                              >>             -Uri "http://localhost:8080/api/posts" `                                                                 >>             -Method GET `                                                                                            >>             -ErrorAction Stop                                                                                        >>                                                                                                                      >>         $statusCode = [int]$response.StatusCode                                                                      >>     }                                                                                                                >>     catch {                                                                                                          >>         if ($_.Exception.Response) {                                                                                 >>             $statusCode = [int]$_.Exception.Response.StatusCode                                                      >>         }                                                                                                            >>         else {                                                                                                       >>             $statusCode = "ERROR"                                                                                    >>         }                                                                                                            >>     }                                                                                                                >>                                                                                                                      >>     Write-Host "Request $_ : $statusCode"                                                                            >> }  