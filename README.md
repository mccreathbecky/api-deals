# api-deals

This is a basic demo API that supports:
- querying for all active deals at a time of day
- calculating the peak window for most deals available


## Dependencies
- Contract API Deals: https://github.com/mccreathbecky/contract-api-deals

## Run Locally
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
Start the server, specifying the 3 injected variables for security tokens
```bash
mvn spring-boot:run

OR

APP_CONFIG_SECURITY_APIKEY_TOKEN=<your-value-here e.g. abc123> \
mvn spring-boot:run -Dspring-boot.run.profiles=<your-env-here e.g. local> -U
```

Running Tests
```bash
  mvn clean verify test
```