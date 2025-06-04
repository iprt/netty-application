package io.intellij.netty.example.dispatch.model.msg;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.example.dispatch.model.DataBody;
import io.intellij.netty.example.dispatch.model.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Response
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class Response {
    private int code;
    private String msg;

    public static DataBody create(int code, String msg) {
        return DataBody.builder()
                .dataType(DataType.RESPONSE.getCode())
                .json(JSON.toJSONString(Response.builder().code(code).msg(msg).build()))
                .build();
    }

}
