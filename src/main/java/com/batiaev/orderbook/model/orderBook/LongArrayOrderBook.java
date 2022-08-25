package com.batiaev.orderbook.model.orderBook;

import com.batiaev.orderbook.model.Side;

import java.util.Arrays;

import static com.batiaev.orderbook.model.Side.SELL;

public class LongArrayOrderBook {
    private final long[][] bids;
    private final long[][] asks;

    public LongArrayOrderBook(long[][] bids, long[][] asks) {
        this.bids = bids;
        this.asks = asks;
    }

    public void update(Side side, long price, long size) {
        if (side.equals(SELL)) {
            long[] cur = new long[]{price, size};
            for (int i = 0; i < asks.length; i++) { //TODO replace O(n) to binary search O(logN)
                if (price < asks[i][0]) {
                    long[] tmp = new long[]{asks[i][0], asks[i][1]};
                    asks[i][0] = cur[0];
                    asks[i][1] = cur[1];
                    cur[0] = tmp[0];
                    cur[1] = tmp[1];
                } else if (price == asks[i][0]) {
                    asks[i][1] += size;
                    break;
                }
            }
            if (asks[0][0] <= bids[0][0]) {
                int idx1 = 0;
                int idx2 = 0;
                while (idx2 < bids.length - 1) {
                    if (bids[idx2][0] < asks[0][0]) {
                        bids[idx1][0] = bids[idx2][0];
                        bids[idx1][1] = bids[idx2][1];
                        idx1++;
                    }
                    idx2++;
                }
                while (idx1 < bids.length - 1) {
                    bids[idx1][0] = 0;
                    bids[idx1][1] = 0;
                    idx1++;
                }
            }
        } else {
            long[] cur = new long[]{price, size};
            for (int i = 0; i < bids.length; i++) { //TODO replace O(n) to binary search O(logN)
                if (price > bids[i][0]) {
                    long[] tmp = new long[]{bids[i][0], bids[i][1]};
                    bids[i][0] = cur[0];
                    bids[i][1] = cur[1];
                    cur[0] = tmp[0];
                    cur[1] = tmp[1];
                } else if (price == bids[i][0]) {
                    bids[i][1] += size;
                    break;
                }
            }
            if (bids[0][0] >= asks[0][0]) {
                int idx1 = 0;
                int idx2 = 0;
                while (idx2 < asks.length - 1) {
                    if (bids[0][0] >= asks[idx2][0]) {
                        asks[idx1][0] = asks[idx2][0];
                        asks[idx1][1] = asks[idx2][1];
                        idx1++;
                    }
                    idx2++;
                }
                while (idx1 < bids.length - 1) {
                    bids[idx1][0] = 0;
                    bids[idx1][1] = 0;
                    idx1++;
                }
            }
        }
    }

    public long[][] getBids() {
        return bids.clone();
    }

    public long[][] getAsks() {
        return asks.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongArrayOrderBook that = (LongArrayOrderBook) o;
        return Arrays.equals(bids, that.bids) && Arrays.equals(asks, that.asks);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(bids);
        result = 31 * result + Arrays.hashCode(asks);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(System.lineSeparator() + "SIZE PRICE").append(System.lineSeparator());
        for (int i = asks.length - 1; i >= 0; i--) {
            builder.append(String.format("%6d %6d", asks[i][1], asks[i][0])).append(System.lineSeparator());
        }
        builder.append("------------------").append(System.lineSeparator());
        for (long[] bid : bids) {
            builder.append(String.format("%6d %6d", bid[1], bid[0])).append(System.lineSeparator());
        }
        return builder.append(System.lineSeparator()).toString();
    }
}
