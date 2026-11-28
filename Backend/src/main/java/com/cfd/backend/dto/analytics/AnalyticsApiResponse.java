
package com.cfd.backend.dto.analytics;

public class AnalyticsApiResponse<T> {

    private String status;

    private T data;

    public AnalyticsApiResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
