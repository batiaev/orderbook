# Orderbook

test implementation of order book based on ws feed from coinbase

- CoinbaseClient on start open ws connection
- on connect send message to subscribe to specified pair
- on text message from ws coinbase client will put new event to disruptor
- disruptor has couple processors
  - to process order book to maintain current state
  - to log updates of order book to console
  - to cleanup events in ring buffer

## PoC limitations
- websocket connectivity management not implemented
  - currently assumed only happy path without logic of reconnection
- ping pong with server to validate healthcheck not implemented
- monitoring not added, but can be added as additional handler similar to logger
- order book has two implementations
  - sorted map - easy to use and understand solution
  - long arrays works better with caches but more complicated and current logic of orderbook updates is not efficient
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
