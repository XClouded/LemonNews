package com.loopj.android.http;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

public class AsyncHttpRequestTask extends AsyncTask<Void, Void, Void> {
    
    private final AbstractHttpClient client;
    private final HttpContext context;
    private final HttpUriRequest request;
    private final AsyncHttpResponseHandler responseHandler;
    private boolean isBinaryRequest;
    private int executionCount;
    
    public AsyncHttpRequestTask(AbstractHttpClient client, HttpContext context, HttpUriRequest request, AsyncHttpResponseHandler responseHandler) {
        this.client = client;
        this.context = context;
        this.request = request;
        this.responseHandler = responseHandler;
        if(responseHandler instanceof BinaryHttpResponseHandler) {
            this.isBinaryRequest = true;
        }
    }
    
    @Override
    protected Void doInBackground(Void... params) {
        try {
            if(responseHandler != null){
                responseHandler.sendStartMessage();
            }

            makeRequestWithRetries();

            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
            }
        } catch (IOException e) {
            if(responseHandler != null) {
                responseHandler.sendFinishMessage();
                if(this.isBinaryRequest) {
                    responseHandler.sendFailureMessage(e, (byte[]) null);
                } else {
                    responseHandler.sendFailureMessage(e, (String) null);
                }
            }
        }
        return null;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    private void makeRequest() throws IOException {
        if(!Thread.currentThread().isInterrupted()) {
            HttpResponse response = client.execute(request, context);
            if(!Thread.currentThread().isInterrupted()) {
                if(responseHandler != null) {
                    responseHandler.sendResponseMessage(response);
                }
            } else{
                //TODO: should raise InterruptedException? this block is reached whenever the request is cancelled before its response is received
            }
        }
    }

    private void makeRequestWithRetries() throws ConnectException {
        // This is an additional layer of retry logic lifted from droid-fu
        // See: https://github.com/kaeppler/droid-fu/blob/master/src/main/java/com/github/droidfu/http/BetterHttpRequestBase.java
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = client.getHttpRequestRetryHandler();
        while (retry) {
            try {
                makeRequest();
                return;
        } catch (UnknownHostException e) {
            if(responseHandler != null) {
                responseHandler.sendFailureMessage(e, "can't resolve host");
        }
        return;
            } catch (IOException e) {
                cause = e;
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            } catch (NullPointerException e) {
                // there's a bug in HttpClient 4.0.x that on some occasions causes
                // DefaultRequestExecutor to throw an NPE, see
                // http://code.google.com/p/android/issues/detail?id=5255
                cause = new IOException("NPE in HttpClient" + e.getMessage());
                retry = retryHandler.retryRequest(cause, ++executionCount, context);
            }
        }

        // no retries left, crap out with exception
        ConnectException ex = new ConnectException();
        ex.initCause(cause);
        throw ex;
    }
}
