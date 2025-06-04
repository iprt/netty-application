package io.intellij.netty.example.dispatch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * HeartBeat
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class HeartBeat {
    private Date time;
    private String id;
    private long seq;
}
