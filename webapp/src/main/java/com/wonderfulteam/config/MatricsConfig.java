package com.wonderfulteam.config;

import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;

/**
 * Created by Qixiang Zhou on 2019-11-05 16:32
 */

public class MatricsConfig {
    public static final StatsDClient statsd = new NonBlockingStatsDClient("csye6225-webapp", "localhost", 8125);
}