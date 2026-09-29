package com.shop.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器，保证所有异常都返回统一 Result 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.error(Result.CODE_BAD_REQUEST, message);
    }

    /**
     * 请求的路径或静态资源不存在：返回 404，避免被兜底处理器误报为 500
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(NoResourceFoundException e) {
        return Result.error(404, "请求的资源不存在");
    }

    /**
     * 请求体或参数格式错误：返回 400，避免被兜底处理器误报为 500
     */
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBadRequest(Exception e) {
        return Result.error(Result.CODE_BAD_REQUEST, "请求参数格式错误");
    }

    /**
     * 请求方法不支持：例如对只有 GET 的接口发送 POST，返回 405 而不是 500
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "请求方法不支持：" + e.getMethod() + "，请检查 Apifox 中的请求方法（GET / POST / PUT / DELETE）");
    }

    /**
     * 请求体媒体类型不支持：例如接口要求 application/json，却用了 form 表单，返回 415
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Void> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return Result.error(415, "请求体格式不支持，请在 Apifox 的 Body 中选择 JSON，并设置 Content-Type 为 application/json");
    }

    /**
     * 上传文件超过大小限制：返回明确提示，避免前端只显示网络异常。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.error(413, "图片大小不能超过20MB，请压缩图片或重新选择较小的图片");
    }
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(Result.CODE_ERROR, "系统繁忙，请稍后重试");
    }
}

