package com.jrestless.fnproject;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fnproject.fn.api.Headers;
import com.fnproject.fn.api.InputEvent;
import com.fnproject.fn.api.QueryParameters;
import com.fnproject.fn.runtime.QueryParametersImpl;
import com.fnproject.fn.runtime.ReadOnceInputEvent;

public class DefaultInputEvent {
    private String domainWithScheme = "http://www.example.com";
    private String appName = "myApp";
    private String route = "/route";
    private String requestUrl = domainWithScheme + "/r/route";
    private String method = "GET";
    private InputStream body = new ByteArrayInputStream(new byte[]{});
    private Headers headers = Headers.emptyHeaders();
    private QueryParameters parameters = new QueryParametersImpl();

    public DefaultInputEvent(){
    }

    public DefaultInputEvent setReqUrlAndRoute(String reqUrl, String route){
        requestUrl = reqUrl;
        this.route = route;
        return this;
    }

    public DefaultInputEvent setMethod(String method){
        this.method = method;
        return this;
    }

    public DefaultInputEvent setAppName(String name){
        appName = name;
        return this;
    }

    public DefaultInputEvent setBody(InputStream body){
        this.body = body;
        return this;
    }

    public DefaultInputEvent setHeaders(Headers headers){
        this.headers = headers;
        return this;
    }

    public DefaultInputEvent setHeaders(Map<String,String> headers){
        this.headers = Headers.fromMap(headers);
        return this;
    }

    public DefaultInputEvent setQueryParameters(Map<String, List<String>> params){
        parameters = new QueryParametersImpl(params);
        return this;
    }

    public InputEvent getInputEvent(){
        return new ReadOnceInputEvent(appName, route, requestUrl, method, body, headers, parameters);
    }
}
