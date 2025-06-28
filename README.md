## Scalable Tracking Number Generator API

### Overview
This project implements a scalable, efficient RESTful API for generating 
unique tracking numbers for parcels. Built with Spring Boot(3.5.3) and
Spring Framework, it is designed for high concurrency and horizontal 
scalability.

### Functional Features
- **GET /next-tracking-number** endpoint to generate unique tracking numbers
- Generated tracking numbers match the pattern: `^[A-Z0-9]{1,16}$`
- Ensures uniqueness across multiple requests and instances
  - **Uniqueness Guarantee:** The tracking number generation logic uses a monotonically increasing counter
  to guarantee uniqueness. The number is then encoded using a bijective method using a java library from
  https://sqids.org/. This ensures that each generated tracking number is unique. To make the counter globally available 
  to all server instances when the system scales horizontally, the
  counter is maintained in a distributed cache(Redis). To reduce network calls to redis,
  a local counter 
  is used to prefetch next tracking numbers in batches. 

### Non-functional Features
- Low latency in tracking number generation
- Handles high concurrency and is horizontally scalable
- Uses best practices in project structure and configuration

### API Specification
Interactive API documentation is available here
http://34.70.109.139/swagger-ui.html

#### API Endpoint
```
GET /next-tracking-number
```

#### Query Parameters
| Name                  | Type    | Description                                                      | Example                        |
|-----------------------|---------|------------------------------------------------------------------|--------------------------------|
| origin_country_id     | String  | Origin country code (ISO 3166-1 alpha-2)                         | MY                             |
| destination_country_id| String  | Destination country code (ISO 3166-1 alpha-2)                    | ID                             |
| weight                | String  | Order weight in kilograms (up to 3 decimal places)               | 1.234                          |
| created_at            | String  | Order creation timestamp (RFC 3339 format)                       | 2018-11-20T19:29:32+08:00      |
| customer_id           | String  | Customer UUID                                                    | de619854-b59b-425e-9db4-943979e1bd49 |
| customer_name         | String  | Customer name                                                    | RedBox Logistics               |
| customer_slug         | String  | Customer name in slug-case/kebab-case                            | redbox-logistics               |


#### Response Schema
| Field            | Type   | Description                                 |
|------------------|--------|---------------------------------------------|
| tracking_number  | String | The generated unique tracking number         |
| created_at       | String | Timestamp when the tracking number was generated (RFC 3339 format) |

#### Example Response
```
{
  "tracking_number": "A1B2C3D4E5F6G7H8",
  "created_at": "2025-06-28T12:34:56+00:00"
}
```

### Setup & Run
#### Prerequisites
- Java 21 
- Docker

#### Local Development
1. **Clone the repository:**
   ```sh
   git clone <your-repo-url>
   cd tracking-api-webflux-java
   ```
2. **Build the project:**
   ```sh
   ./gradlew build
   ```
3. **Spin up a docker container for Redis service.** 
   ```sh
   docker run -d --name redis -p 6379:6379 redis
   ```
4. **If you have a different redis configuration, Create a `.env` file in the root directory with the following content:**
   ```env
   REDIS_HOST=YOUR_REDIS_HOST
   REDIS_PORT=YOUR_REDIS_PORT
   ```
3. **Run the application:**
   ```sh
   ./gradlew bootRun
   ```
   The API documentation should be available at `http://localhost:8080/swagger-ui.html`.

#### Running using docker
To run the application in a Docker container, follow these steps:
1. **Build Docker image:**
   ```sh
   docker build -t tracking-api-webflux-java .
   ```
2. **Run Docker container:**
   ```sh
   docker run -p 8080:8080 tracking-api-webflux-java
   ```

### Testing
- Run all tests:
  ```sh
  ./gradlew test
  ```
- Test the API endpoint using `curl` or any HTTP client:
  ```sh
  curl "http://localhost:8080/next-tracking-number?origin_country_id=MY&destination_country_id=ID&weight=1.234&created_at=2018-11-20T19:29:32%2B08:00&customer_id=de619854-b59b-425e-9db4-943979e1bd49&customer_name=RedBox%20Logistics&customer_slug=redbox-logistics"
  ```

### Deployed Application
- **URL:** http://34.70.109.139/swagger-ui.html


