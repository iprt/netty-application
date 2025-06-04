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
 * LogoutReq
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class LogoutReq {
    private String username;

    public static DataBody create(String username) {
        return DataBody.builder()
                .dataType(DataType.LOGOUT.getCode())
                .json(JSON.toJSONString(LogoutReq.builder().username(username).build()))
                .build();
    }

}
