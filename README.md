# In-Memory Order Book (Kotlin + Vert.x)

This project implements a simplified **in-memory order book** with order matching, using **Kotlin** and **Vert.x**.  
It exposes REST APIs for submitting limit orders, retrieving order books, and viewing recent trades.  
JWT authentication is supported.

---

## 📂 Project Structure


```
src
└── main
    └── kotlin
        └── com
            └── resvara
                ├── model # Data models (Order, Trade, enums)
                ├── service # Order book logic
                ├── api # REST API (routes, DTOs)
                └── Application.kt # Entry point
    └── test
        └── kotlin
            └── com
                └── resvara
                    ├── service # Unit tests for order book logic
                    └── api # Unit tests for REST API
```


---

## 🚀 Running the Application

1. Clone this repo.
2. Run with Gradle:

```bash
./gradlew run
```

The HTTP server will start on http://localhost:8080.

🔑 Authentication

Before calling protected endpoints, request a JWT:
```
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"test","password":"password"}'
```

Response:
```
{"token":"<jwt-token>"}
```


Use the token in subsequent requests:
```
-H "Authorization: Bearer <jwt-token>"
```

📡 API Endpoints
1. Get Order Book

#### GET /:currencyPair/orderbook

Example:
```
curl -H "Authorization: Bearer <token>" \
http://localhost:8080/BTCZAR/orderbook
```
Response:
```
{
"asks": [
 {
  "id":"...",
  "side":"SELL",
  "price":500000,
  "quantity":0.2,
  "currencyPair":"BTCZAR"
  }
 ],
"bids": [
{
 "id":"...",
  "side":"BUY",
  "price":490000,
  "quantity":0.5,
  "currencyPair":"BTCZAR"
  }
 ]
}
```


2. Submit Limit Order

#### POST /v1/orders/limit

Payload:
```
{
 "side": "BUY",
 "price": 500000,
 "quantity": 0.1,
 "currencyPair": "BTCZAR"
}
```


Response (list of trades executed immediately):
```
[
  {
  "side":"BUY",
  "price":500000,
  "quantity":0.1,
  "currencyPair":"BTCZAR",
  "lastChange":1691234567890
  }
]
```

If unmatched, order is stored in the order book.

3. Recent Trades

#### GET /:currencyPair/tradehistory

Example:
```
curl -H "Authorization: Bearer <token>" \
http://localhost:8080/BTCZAR/tradehistory
```


Response:
```
[
 {
  "side":"SELL",
  "price":495000,
  "quantity":0.05,
  "currencyPair":"BTCZAR",
  "lastChange":1691234567890
 }
]
```

🏗️ Architecture

![Architecture Diagram](docs/architecture.png)


- Vert.x Router handles requests

- JWTAuth protects endpoints

- OrderBookService manages matching + trades

- In-Memory Store holds bids, asks, trades

