package io.intellij.netty.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * NettySpringBootApplication
 *
 * @author tech@intellij.io
 */
@SpringBootApplication
public class NettySpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettySpringBootApplication.class, args);
    }

    @ControllerAdvice
    static class ExceptionConfig {

        @ExceptionHandler(Exception.class)
        @ResponseBody
        public ResponseEntity<Map<String, Object>> handleException(Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "code", 500,
                    "error", e.getMessage()));
        }
    }
}
