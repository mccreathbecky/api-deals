# api-deals

This is a basic demo API that supports:
- querying for all active deals at a time of day
- calculating the peak window for most deals available


## Dependencies
- Java 21
- Contract API Deals: https://github.com/mccreathbecky/contract-api-deals

## Run Locally
Ensure you have locally installed `contract-api-deals`. 

Clone the project
```bash
  git clone https://github.com/mccreathbecky/api-deals.git
```
Go to the project directory
```bash
  cd api-deals
```
Compile the code
```bash
  mvn clean compile package -U
```
Start the server, specifying the environment
```bash
mvn spring-boot:run -Dactive.spring.profiles=<your-env-here e.g. local> -U
```

Running Tests
```bash
  mvn clean verify test
```

Test API Endpoints
```bash
curl --location 'http://localhost:8080/api-deals/v1/active?timeOfDay=17%3A00' \
--header 'x-tracking-id: my-tracking-id-123' \
--header 'x-api-key: DUMMY_VALUE'

curl --location 'http://localhost:8080/api-deals/v1/peak' \
--header 'x-tracking-id: my-tracking-id-123' \
--header 'x-api-key: DUMMY_VALUE'
```

## Pending Enhancements
Given more time, the following would be implemented:
- API Key security validation (including encrypted secure key properties)
- A database/API resource implementation rather than a mock for a static file
- Proper logging using tracking ID with appropriate error/warn/info/debug levels
- More detailed unit tests with different inputs testing the peak and active deals logic, including more exhaustive null checking
- Circuit Breakers and Timeouts
- Full JavaDocs on every function
- More consistency on error messages e.g. error code formats