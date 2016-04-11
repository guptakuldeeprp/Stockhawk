package com.sam_chordas.android.stockhawk.data;

/**
 * Created by kuldeep.gupta on 29-03-2016.
 */
public class StockDetails {

    private String symbol;
    private String bidPrice;
    private String percChange;
    private String change;
    private int isUp;

    public StockDetails() {

    }

    public StockDetails(String symbol, String bidPrice, String percChange, String change, int isUp) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.percChange = percChange;
        this.change = change;
        this.isUp = isUp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(String bidPrice) {
        this.bidPrice = bidPrice;
    }

    public String getPercChange() {
        return percChange;
    }

    public void setPercChange(String percChange) {
        this.percChange = percChange;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public int getIsUp() {
        return isUp;
    }

    public void setIsUp(int isUp) {
        this.isUp = isUp;
    }
}
