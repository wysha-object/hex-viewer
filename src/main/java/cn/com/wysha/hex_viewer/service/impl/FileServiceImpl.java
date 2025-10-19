package cn.com.wysha.hex_viewer.service.impl;

import cn.com.wysha.hex_viewer.cache.LRUMapBytesCache;
import cn.com.wysha.hex_viewer.constant.FileServiceConstant;
import cn.com.wysha.hex_viewer.service.FileService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

@Component @Scope("prototype")
public class FileServiceImpl implements FileService {
    private Long totalLen;
    private File origin;
    private RandomAccessFile file;
    private LRUMapBytesCache cache;

    @Override
    public synchronized void setFile(File file) {
        this.origin = file;
    }

    @Override
    public synchronized File getFile() {
        return origin;
    }

    @Override
    public synchronized void open(File origin) {
        if (isOpen()) throw new IllegalStateException();
        this.origin = origin;

        try {
            file = new RandomAccessFile(origin, "rw");
            cache = new LRUMapBytesCache(
                    FileServiceConstant.BYTES_CACHE_BLOCK_SIZE,
                    FileServiceConstant.BYTES_CACHE_SIZE,
                    address -> read(address, FileServiceConstant.BYTES_CACHE_BLOCK_SIZE)
            );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized boolean isOpen() {
        if (file == null) return false;
        return file.getChannel().isOpen();
    }

    @Override
    public synchronized void close() {
        if (isOpen()) {
            try {
                cache.clear();
                file.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public synchronized boolean isOutOfRange(long address) {
        return (address < 0 || address >= getFileLength());
    }

    @Override
    public synchronized long getFileLength() {
        if (totalLen == null) {
            try {
                totalLen = file.length();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return totalLen;
    }

    private synchronized byte[] read(long address, int length) {
        if (address < 0) throw new IllegalArgumentException();
        long lastByteRequireAddress = address + length - 1;
        byte[] bytes = new byte[length];
        try {
            if (isOutOfRange(address) || isOutOfRange(lastByteRequireAddress)) {
                if (!(isOutOfRange(address))) {
                    lastByteRequireAddress = getFileLength() - 1;
                    file.seek(address);
                    file.readFully(bytes, 0, Math.toIntExact(lastByteRequireAddress - address + 1));
                }
            } else {
                file.seek(address);
                file.readFully(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    @Override
    public synchronized byte[] getBytes(long address, int length) {
        return cache.get(address, length);
    }
    private synchronized void write(long address, byte[] data) {
        try {
            file.seek(address);
            file.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public synchronized void saveBytes(long address, byte[] data) {
        write(address, data);
        cache.remove(address, data.length);
        totalLen = null;
    }
}
