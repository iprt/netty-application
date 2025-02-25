/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.intellij.netty.server.socks.handler;

import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SocksServerUtils {

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch) {
        ChannelUtils.closeOnFlush(ch);
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch, String desc) {
        log.info("close on flush : {}", desc);
        ChannelUtils.closeOnFlush(ch);
    }

    private SocksServerUtils() {
    }
}
