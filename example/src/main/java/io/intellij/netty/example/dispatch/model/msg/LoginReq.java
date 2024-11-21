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
 * LoginReq
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class LoginReq {
    private String username;
    private String password;

    public DataBody toDataBody() {
        return DataBody.builder()
                .dataType(DataType.LOGIN.getCode())
                .json(JSON.toJSONString(this))
                .build();
    }

    public static DataBody create(String username, String password) {
        return DataBody.builder()
                .dataType(DataType.LOGIN.getCode())
                .json(JSON.toJSONString(LoginReq.builder().username(username).password(password).build()))
                .build();
    }

}
