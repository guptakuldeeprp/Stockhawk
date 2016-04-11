package com.sam_chordas.android.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Calendar;

import yahoofinance.histquotes.HistoricalQuote;

/**
 * Created by kuldeep.gupta on 11-04-2016.
 */
public class ParcelableHistoricalQuote extends HistoricalQuote implements Parcelable{

    public ParcelableHistoricalQuote() {

    }

    protected ParcelableHistoricalQuote(Parcel in) {
        setSymbol(in.readString());
        setDate((Calendar) in.readValue(Calendar.class.getClassLoader()));
        setOpen((BigDecimal) in.readValue(BigDecimal.class.getClassLoader()));
        setLow((BigDecimal) in.readValue(BigDecimal.class.getClassLoader()));
        setHigh((BigDecimal) in.readValue(BigDecimal.class.getClassLoader()));
        setClose((BigDecimal) in.readValue(BigDecimal.class.getClassLoader()));
        setAdjClose((BigDecimal) in.readValue(BigDecimal.class.getClassLoader()));
        setVolume(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getSymbol());
        dest.writeValue(getDate());
        dest.writeValue(getOpen());
        dest.writeValue(getLow());
        dest.writeValue(getHigh());
        dest.writeValue(getClose());
        dest.writeValue(getAdjClose());
        dest.writeLong(getVolume());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<HistoricalQuote> CREATOR = new Parcelable.Creator<HistoricalQuote>() {
        @Override
        public HistoricalQuote createFromParcel(Parcel in) {
            return new ParcelableHistoricalQuote(in);
        }

        @Override
        public HistoricalQuote[] newArray(int size) {
            return new HistoricalQuote[size];
        }
    };
}
