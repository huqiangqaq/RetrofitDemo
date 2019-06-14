package com.example.huqiang.retrofitdemo;

/**
 * Created by 83916 on 2018/1/1.
 */

/**
 * 如果后台返回的格式比较规范，可以采取定义一个基类的方式来定义实体类，这样可以通过ApiException来对结果进行预处理
 * 后台返回的数据不一定一致，这里只是演示
 * @param <T>
 */
public class BaseResult<T> {
    private int code;
    private String message;
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
