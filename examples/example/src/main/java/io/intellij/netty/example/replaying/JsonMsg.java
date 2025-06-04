package io.intellij.netty.example.replaying;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * JsonMsg
 *
 * @author tech@intellij.io
 * @since 2025-03-02
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class JsonMsg {
    private boolean valid;
    private String content;
}
