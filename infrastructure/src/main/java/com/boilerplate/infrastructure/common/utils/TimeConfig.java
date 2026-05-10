package com.boilerplate.infrastructure.common.utils;

import java.time.ZoneOffset;

public final class TimeConfig {
    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("-03:00");

    private TimeConfig() {}
}