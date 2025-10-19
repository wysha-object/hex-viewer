package cn.com.wysha.hex_viewer.service;

import java.io.File;
import java.util.Objects;

public interface FileService {
    void setFile(File file);

    File getFile();

    void open(File file);

    default void open() {
        File file =  getFile();
        Objects.requireNonNull(file);
        open(file);
    }

    boolean isOpen();

    void close();

    boolean isOutOfRange(long address);

    long getFileLength();

    byte[] getBytes(long address, int length);

    void saveBytes(long address, byte[] data);
}
