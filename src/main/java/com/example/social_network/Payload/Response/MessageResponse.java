package com.example.social_network.Payload.Response;

import java.util.List;

public class MessageResponse {
    private int Status;
    private Object Object;
    private boolean isOk;
    private boolean isError;

    public MessageResponse(int success, String foundAllLicenses, boolean b, boolean b1, List<Object> object) {
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public java.lang.Object getObject() {
        return Object;
    }

    public void setObject(java.lang.Object object) {
        Object = object;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public MessageResponse(int status, java.lang.Object object, boolean isOk, boolean isError) {
        Status = status;
        Object = object;
        this.isOk = isOk;
        this.isError = isError;
    }
    public MessageResponse() {
    }
}
