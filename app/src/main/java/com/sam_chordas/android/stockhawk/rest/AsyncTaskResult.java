package com.sam_chordas.android.stockhawk.rest;

/**
 * Created by kuldeep.gupta on 26-03-2016.
 */
public class AsyncTaskResult<T> {
    private T result;
    private Exception error;

    public T getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public AsyncTaskResult(T result, Exception error) {
        super();
        this.result = result;
        this.error = error;
    }

    public boolean isSucessful()
    {
        return this.error == null;
    }

    public AsyncTaskResult(T result) {
        this(result,null);
    }
}
