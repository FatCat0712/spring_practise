package vn.tayjava.dto.response;

public class ResponseFailure<T> extends ResponseData<T> {
    public ResponseFailure(int status, String message) {
        super(status, message);
    }
}
