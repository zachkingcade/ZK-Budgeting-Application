package zachkingcade.dev.ledger.adapter.in.web.dto;

public class ApiResponse<T> {
    private String statusMessage;
    private MetaData metaData;
    private T data;

    public ApiResponse(String statusMessage, MetaData metaData, T data) {
        this.statusMessage = statusMessage;
        this.metaData = metaData;
        this.data = data;
    }

    public ApiResponse() {
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
