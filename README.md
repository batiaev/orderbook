# Orderbook

![](https://img.shields.io/github/workflow/status/batiaev/orderbook/Java%20CI%20with%20Gradle)
![](https://img.shields.io/badge/coverage-94%25-green)

Order book implementation with ws feed from coinbase

```java
var orderBook = OrderBook.basedOn(LONG_MAP)
    .withGroupingBy(0.01)
    .withDepth(5)
    .withLoggingFrequency(100)
    .subscribedOn(coinbaseClient)
    .start(withEvent("level2", "ETH-USD"));
```

- CoinbaseClient on start open ws connection
- on connect send message to subscribe to specified pair
- on text message from ws coinbase client will put new event to disruptor
- disruptor has couple processors
  - depth limiter to avoid storing unnecessary levels
  - grouping handler to provider required granularity of price levels
  - to process order book to maintain current order book state
  - to log updates of order book to console on each tick or with required frequency e.g. ones per 100 tick
  - to clean up events in ring buffer (optional)

## PoC limitations
- Rest api with wide open cors implemented in dummy format just to have better visibility of order book updates (in additional to console logger) and to be able to play with different currencies/grouping. Check more at repo for UI https://github.com/batiaev/orderbook-ui
- websocket connectivity management not implemented
  - currently assumed only happy path without logic of reconnection
- ping pong with server to validate healthcheck not implemented
- monitoring not added, but can be added as additional handler similar to logger
- update event not optimal, required to rewrite to long arrays for bids and asks updates
- order book implementations
  - LongsMap: well-balanced implementation (mechanical sympathy to keep array in cache and easy to read code implementation), hppc primitive structure 
  - TreeMap: basic, not mem efficient, but easy to understand, no extra dependencies, stored full order book
  - ArrayList: mem optimised, cpu intensive(reordering of order book on each update), fixed size = min(100, requiredDepth) array always sorted to proper order

## Build

```groovy
./gradlew clean build
```

## Run
### From console
Default (product=ETH-USD depth=10 )
```bash
java -jar ./build/libs/orderbook-0.0.1-SNAPSHOT-all.jar 
```
Different implementations
```bash
java -jar ./build/libs/orderbook-0.0.1-SNAPSHOT-all.jar ETH-USD 5 treemap
```
```bash
java -jar ./build/libs/orderbook-0.0.1-SNAPSHOT-all.jar ETH-USD 5 array
```
```bash
java -jar ./build/libs/orderbook-0.0.1-SNAPSHOT-all.jar ETH-USD 5 longmap
```
Custom product and depth
```bash
java -jar ./build/libs/orderbook-0.0.1-SNAPSHOT-all.jar BTC-USD 3
```
### Docker
```bash
docker-compose up
```
### From IDEA
Run `OrderBookApp` configuration 

## Output
### Rest API
[http://localhost:4567/orderbook/ETH-USD?depth=3]()
```json
[
  {
    "side": "SELL",
    "priceLevel": 1600.62,
    "size": 6.67094188
  },
  {
    "side": "SELL",
    "priceLevel": 1600.52,
    "size": 0.06294962
  },
  {
    "side": "SELL",
    "priceLevel": 1600.50,
    "size": 0.36294962
  },
  {
    "side": "BUY",
    "priceLevel": 1600.17,
    "size": 0.24962437
  },
  {
    "side": "BUY",
    "priceLevel": 1600.14,
    "size": 2.10000000
  },
  {
    "side": "BUY",
    "priceLevel": 1600.09,
    "size": 0.59447000
  }
]
```

### Console
```csv
SIDE    PRICE      SIZE
SELL  1603.32   9.44306272
SELL  1603.28   0.96906302
SELL  1603.18   0.41006322
SELL  1603.10   1.46044000
SELL  1602.92   1.66718000
SELL  1602.86   1.46044000
SELL  1602.82   1.66718000
SELL  1602.75   1.80000000
SELL  1602.59  25.33627671
SELL  1602.58   4.41134484
 BUY  1602.17   0.30000000
 BUY  1602.16   2.03481079
 BUY  1602.13   0.67211880
 BUY  1602.06   0.93098272
 BUY  1602.05   0.62595088
 BUY  1601.99   0.50000000
 BUY  1601.97   0.87534464
 BUY  1601.96   0.99247573
 BUY  1601.95   1.34078645
 BUY  1601.75   1.66718000
```
## Author

Anton Batiaev
