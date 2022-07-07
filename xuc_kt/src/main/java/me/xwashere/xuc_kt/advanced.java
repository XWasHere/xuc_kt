package me.xwashere.xuc_kt;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class advanced {
    public static advanced _init;

    static {
        try {
            _init = new advanced();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean  _ready = false;
    public static Unsafe   _unsafe;

    public advanced() throws NoSuchFieldException, IllegalAccessException {
        if (!_ready) {
            Field the_unsafe = Unsafe.class.getDeclaredField("theUnsafe");
            the_unsafe.setAccessible(true);
            _unsafe = (Unsafe)the_unsafe.get(null);
            _ready = true;
        }
    }

    public static <T> T imalloc(Class<T> cls) throws InstantiationException {
        return (T)_unsafe.allocateInstance(cls);
    }

    public static long malloc(long size) {
        return _unsafe.allocateMemory(size);
    }

    public static long realloc(long addr, long size) {
        return _unsafe.reallocateMemory(addr, size);
    }

    public static void free(long addr) {
        _unsafe.freeMemory(addr);
    }

    public static long memcpy(long dst, long src, long n) {
        _unsafe.copyMemory(src, dst, n);
        return dst;
    }

    public static long memset(long addr, byte c, long n) {
        _unsafe.setMemory(addr, n, c);
        return addr;
    }

    public static long memmove(long dst, long src, long n) {
        long tb = malloc(n);
        memcpy(tb, src, n);
        memset(src, (byte)0, n);
        memcpy(dst, tb, n);
        memset(tb, (byte)0, n);
        free(tb);
        return dst;
    }

    public static long strlen(long s) {
        long l;
        for (l = 0; _unsafe.getByte(s + l) != 0; l++) {}
        return l;
    }

    public static long strcpy(long dst, long src) {
        long l;
        byte b;
        for (l = 0; (b = _unsafe.getByte(src + l)) != 0; l++) {
            _unsafe.putByte(dst + l, b);
        }
        _unsafe.putByte(dst + l + 1, (byte)0);
        return l;
    }

    public static long strdup(long src) {
        long ns = malloc(strlen(src) + 1);
        strcpy(ns, src);
        return ns;
    }
}
