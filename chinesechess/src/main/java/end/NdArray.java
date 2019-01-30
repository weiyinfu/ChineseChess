package end;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 高维数组,类似Numpy封装
 * 本来想使用nd4j,但是这个库有点麻烦,依赖比较复杂
 */
public class NdArray {
int a[];
int[] shape;

NdArray(int initValue, int... shape) {
    this.shape = shape;
    int sz = Arrays.stream(shape).reduce(1, (s, x) -> s * x);
    a = new int[sz];
    Arrays.fill(a, initValue);
}

int getIndex(int[] index) {
    if (index.length != shape.length)
        throw new RuntimeException("invalid index " + Arrays.stream(index).mapToObj(x -> x + "").collect(Collectors.joining(",")) + " with shape " + Arrays.stream(shape).mapToObj(x -> x + "").collect(Collectors.joining(",")));
    int s = 0;
    int w = 1;
    for (int i = index.length - 1; i >= 0; i--) {
        s += index[i] * w;
        w *= shape[i];
    }
    return s;
}

int get(int... index) {
    return a[getIndex(index)];
}

void put(int value, int... index) {
    a[getIndex(index)] = value;
}

@Override
public String toString() {
    return Arrays.stream(a).mapToObj(x -> x + "").collect(Collectors.joining(","));
}

public static void main(String[] args) {
    NdArray a = new NdArray(-1, 3, 3, 3);
    System.out.println(a);
    a.put(5, 1, 2, 1);
    System.out.println(a);
    System.out.println(a.get(1, 2, 1));
}
}
