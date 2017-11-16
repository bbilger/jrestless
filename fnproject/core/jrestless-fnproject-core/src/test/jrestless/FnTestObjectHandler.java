package com.fnproject.fn.jrestless;


import com.fnproject.fn.api.InputEvent;

public class FnTestObjectHandler extends FnRequestHandler {

    public WrappedOutput handleTestRequest(InputEvent inputEvent){
        return inputEvent.consumeBody((inputStream) -> {
            FnRequestHandler.WrappedInput wrappedInput = new FnRequestHandler.WrappedInput(inputEvent, inputStream);
            return this.delegateRequest(wrappedInput);
        });
    }
}
