package cn.com.wysha.hex_viewer.cache;

import java.util.*;
import java.util.function.Function;

public class LRUMapBytesCache {
    private final LRUMapCache<Long, byte[]> cache;

    private final int blockLength;

    /**
     *
     * @param blockLength 块大小
     * @param capacity 容量
     * @param loader 接收所需字节的地址,返回大小为{@code blockLength}的数组
     */
    public LRUMapBytesCache(int blockLength, int capacity, Function<Long, byte[]> loader) {
        this.blockLength = blockLength;
        cache = new LRUMapCache<>(capacity, (index) -> loader.apply(index*blockLength));
    }

    public synchronized byte[] get(long start, int len) {
        long startBlock = start / blockLength;
        long endBlock = (start + len - 1) / blockLength;

        byte[][] blocks = new byte[(int) (endBlock - startBlock + 1)][];
        for (long i = startBlock; i <= endBlock; i++) {
            blocks[(int) (i - startBlock)] = cache.get(i);
        }

        byte[] bytes = new byte[len];
        for (int i = 0, bytesIndex = 0; i < blocks.length; i++) {
            int srcPos = (int) ((start + bytesIndex) % blockLength);
            int copyLength = Math.min(blockLength - srcPos, len - bytesIndex);
            System.arraycopy(
                    blocks[i],
                    srcPos,
                    bytes,
                    bytesIndex,
                    copyLength
            );
            bytesIndex += copyLength;
        }

        return bytes;
    }

    public synchronized void remove(long start, int len) {
        long startBlock = start / blockLength;
        long endBlock = (start + len - 1) / blockLength;

        for (long i = startBlock; i <= endBlock; i++) {
            cache.remove(i);
        }
    }

    public synchronized void clear() {
        cache.clear();
    }
}
