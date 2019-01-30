package end;

/**
 * 中国象棋状态个数计算器
 * <p>
 * 一次放置一种棋子
 * <p>
 * 极其复杂的编码,极其复杂的程序
 * <p>
 * 编码解码复杂度小于90*14
 * <p>
 * 此类是编码器和解码器的基础
 * <p>
 * TODO:把变量放到类成员里面，防止大量传参
 */
public class Counter {
static NdArray cmap = new NdArray(-1, 91, 11);

int c(int n, int k) {
    if (cmap.get(n, k) == -1) {
        int s = 1;
        for (int i = 1; i <= k; i++) {
            s = s * (n + 1 - i) / i;
        }
        cmap.put(s, n, k);
    }
    return cmap.get(n, k);
}

int getJiang(int rShi, int bShi, int rXiang, int bXiang, int rZu, int bZu, int jvmapao) {
    int[] posCount = {5, 1, 3};//5个士位,1个相位,3个普通位置
    int s = 0;
    for (int rJiang = 0; rJiang < posCount.length; rJiang++) {
        for (int bJiang = 0; bJiang < posCount.length; bJiang++) {
            s += posCount[rJiang] * posCount[bJiang] * getShi(rJiang, bJiang, rShi, bShi, rXiang, bXiang, rZu, bZu, jvmapao);
        }
    }
    return s;
}

static NdArray shiMap = new NdArray(-1, 3, 3, 3, 3, 3, 3, 6, 6, 3, 3, 3, 3, 3, 3);

int getShi(int rJiang, int bJiang, int rShi, int bShi,
           int rXiang, int bXiang, int rZu, int bZu, int jvmapao) {
    if (shiMap.get(rJiang, bJiang, rShi, bShi, rXiang, bXiang, rZu, bZu, jvmapao) == -1) {
        int rShiPos = rJiang == 0 ? 4 : 5;
        int bShiPos = bJiang == 0 ? 4 : 5;
        int s = c(rShiPos, rShi) * c(bShiPos, bShi) * getXiang(rJiang, bJiang, 45 - rShi - 1, 45 - bShi - 1, rXiang, bXiang, rZu, bZu, jvmapao);
        shiMap.put(s, rJiang, bJiang, rShi, bShi, rXiang, bXiang, rZu, bZu, jvmapao);
    }
    return shiMap.get(rJiang, bJiang, rShi, bShi, rXiang, bXiang, rZu, bZu, jvmapao);
}

static NdArray xiangMap = new NdArray(-1, 3, 3, 46, 46, 3, 3, 6, 6, 13);


int getXiang(int rJiang, int bJiang, int rSpace, int bSpace,
             int rXiang, int bXiang, int rZu, int bZu, int jvmapao) {
    if (xiangMap.get(rJiang, bJiang, rSpace, bSpace, rXiang, bXiang, rZu, bZu, jvmapao) == -1) {
        int rXiangPos = rXiang == 1 ? 6 : 7, bXiangPos = bXiang == 1 ? 6 : 7;
        int s = 0;
        for (int rXiangZu = 0; rXiangZu <= rXiang; rXiangZu++) {
            for (int bXiangZu = 0; bXiangZu <= bXiang; bXiangZu++) {
                s += c(2, rXiangZu) * c(rXiangPos, rXiang - rXiangZu)
                        * c(2, bXiangZu) * c(bXiangPos, bXiang - bXiangZu)
                        * getZu(rXiangZu, bXiangZu, rSpace - rXiang, bSpace - bXiang, rZu, bZu, jvmapao);
            }
        }
        xiangMap.put(s, rJiang, bJiang, rSpace, bSpace, rXiang, bXiang, rZu, bZu, jvmapao);
    }
    return xiangMap.get(rJiang, bJiang, rSpace, bSpace, rXiang, bXiang, rZu, bZu, jvmapao);
}

static NdArray placeZuMap = new NdArray(3, 6);

int placeZu(int rXiangZu, int zu) {
    if (placeZuMap.get(rXiangZu, zu) == -1) {
        int s = 0;
        for (int i = 0; i < Math.min(zu, rXiangZu); i++) {
            s += c(5 - rXiangZu, zu - i) * (1 << (zu - i));
        }
        placeZuMap.put(s, rXiangZu, zu);
    }
    return placeZuMap.get(rXiangZu, zu);
}

static NdArray zuMap = new NdArray(-1, 3, 3, 46, 46, 6, 6, 13);

int getZu(int rXiangZu, int bXiangZu, int rSpace, int bSpace, int rZu, int bZu, int jvmapao) {
    if (zuMap.get(rXiangZu, bXiangZu, rSpace, bSpace, rZu, bZu, jvmapao) == -1) {
        int s = 0;
        for (int rZuRiver = 0; rZuRiver <= rZu; rZu++) {
            for (int bZuRiver = 0; bZuRiver <= bZu; bZu++) {
                s += placeZu(rXiangZu, rZu - rZuRiver) * placeZu(bXiangZu, bZu - bZuRiver)
                        * c(rSpace - (rZu - rZuRiver), bZuRiver) * c(bSpace - (bZu - bZuRiver), rZuRiver) * getJvmapao(rSpace + bSpace - rZu - bZu, jvmapao);
            }
        }
        zuMap.put(s, rXiangZu, bXiangZu, rSpace, bSpace, rZu, bZu, jvmapao);
    }
    return zuMap.get(rXiangZu, bXiangZu, rSpace, bSpace, rZu, bZu, jvmapao);
}

static NdArray jvmapaoMap = new NdArray(-1, 90, 13);

int getJvmapao(int space, int jvmapao) {
    if (jvmapaoMap.get(space, jvmapao) == -1) {
        int s = 0;

    }
    return jvmapaoMap.get(space, jvmapao);
}
}
