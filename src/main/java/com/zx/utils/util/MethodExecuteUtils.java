package com.zx.utils.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

/**
 * @author ZhaoXu
 * @date 2023/4/15 19:22
 */
@Slf4j
public class MethodExecuteUtils {
    @FunctionalInterface
    public interface LogMethodFunction<B> extends Serializable {
        /**
         * 执行方法
         *
         * @return
         */
        B execute();

        /**
         * 获取lambda方法
         *
         * @return
         * @throws Exception
         */
        default SerializedLambda getSerializedLambda() throws Exception {
            Method write = this.getClass().getDeclaredMethod("writeReplace");
            write.setAccessible(true);
            return (SerializedLambda) write.invoke(this);
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 执行rpc远程调用逻辑
     *
     * @param function
     * @param <B>      response
     * @return
     */
    public static <B> B logAround(LogMethodFunction<B> function) {
        SerializedLambda serializedLambda = null;
        try {
            serializedLambda = function.getSerializedLambda();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 类名
        String className = serializedLambda.getImplClass();
        String[] packageSplit = className.split("/");
        className = packageSplit[packageSplit.length - 1];

        // 方法名
        String methodName = serializedLambda.getImplMethodName();
        methodName = className + "." + methodName + " ";
        log.info(methodName + "execute.before, requestBody: {}", OBJECT_MAPPER.valueToTree(function));

        B responseBody = null;
        long startTime = System.currentTimeMillis();
        try {
            responseBody = function.execute();
        } catch (Throwable e) {
            log.error(methodName + "execute.error, exception: {}", OBJECT_MAPPER.valueToTree(e));
        } finally {
            log.info(methodName + "execute.after, responseBody: {}", OBJECT_MAPPER.valueToTree(responseBody));
            log.info(methodName + "execute.time: {}", System.currentTimeMillis() - startTime + " millisecond");
        }
        return responseBody;
    }
}
