package nhom8.example.quizz.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;

    private T data;
    private PaginationDto pagination;


    private String error;
    private Object details;

    public ApiResponse() {}

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = true;
        res.message = message;
        res.data = data;
        return res;
    }

    public static <T> ApiResponse<T> success(String message, T data, PaginationDto pagination) {
        ApiResponse<T> res = success(message, data);
        res.pagination = pagination;
        return res;
    }

    public static ApiResponse<Object> error(String message, String errorCode, Object details) {
        ApiResponse<Object> res = new ApiResponse<>();
        res.success = false;
        res.message = message;
        res.error = errorCode;
        res.details = details;
        return res;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDto pagination) {
        this.pagination = pagination;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}

