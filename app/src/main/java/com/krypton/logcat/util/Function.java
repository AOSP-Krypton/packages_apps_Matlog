package com.krypton.logcat.util;

public interface Function<E, T> {

    T apply(E input);
}
