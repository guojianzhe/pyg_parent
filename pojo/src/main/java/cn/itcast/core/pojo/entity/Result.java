package cn.itcast.core.pojo.entity;

import java.io.Serializable;

/**
 * 实现自定义封装类,封装了返回的正确或者错误的信息
 */
public class Result implements Serializable{
    //true 操作成功, false 操作失败
    private boolean success;
    //成功的信息或者错误的信息
    private String message;
    public Result(boolean success, String message) {
        super();
        this.success = success;
        this.message = message;
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
}
