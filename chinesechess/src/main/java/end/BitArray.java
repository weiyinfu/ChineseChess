package end;

import java.util.Arrays;

/**
 * 每个局面只需要2bit来表示：胜1负-1和0
 * 实现工具类
 * 每个元素至多跨越两个int
 */
public class BitArray {
int perSize;
int[] a;
int count;

BitArray(int perSize, int count) {
    if (perSize > 8) throw new RuntimeException("perSize too big");
    this.perSize = perSize;
    this.count = count;
    a = new int[(int) Math.ceil(count * perSize / 32.0)];
}

int mask(int shift, int cnt) {
    return ((1 << cnt) - 1) << shift;
}

void set(int x, int y) {
    if (x > count) throw new RuntimeException("array index error " + x);
    int p = x * perSize;
    int index = p / 32, shift = p % 32;
    a[index] &= ~(mask(shift, Math.min(32 - shift, perSize)));
    a[index] |= (mask(shift, Math.min(32 - shift, perSize)) & (y << shift));
    if (shift + perSize > 32) {
        a[index + 1] &= ~(mask(0, shift + perSize - 32));
        a[index + 1] |= mask(0, shift + perSize - 32) & (y >> (32 - shift));
    }
}

int get(int x) {
    if (x > count) throw new RuntimeException("array index error " + x);
    int p = x * perSize;
    int index = p / 32, shift = p % 32;
    int value = 0;
    value |= (mask(shift, Math.min(32 - shift, perSize)) & a[index]) >>> shift;
    if (shift + perSize > 32) {
        value |= (mask(0, shift + perSize - 32) & (a[index + 1])) << (32 - shift);
    }
    return value;
}

void clear() {
    Arrays.fill(a, 0);
}

@Override
public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i : a) {
        for (int j = 0; j < 32; j++) {
            builder.append((i >> j) & 1);
        }
    }
    builder.append(" persize=" + perSize + " count=" + count);
    return builder.toString();
}
}
