package com.boilerplate.boot.common.seeder;

import java.util.List;

public interface Seeder<T> {
    void seed(List<T> data);
    String resourcePath();
    Class<T> recordType();
}
