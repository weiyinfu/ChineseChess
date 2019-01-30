package end;

import ui.Chess;
import ui.Expand;
import ui.Node;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 中国象棋残局打表器
 */
public class TableGenerator {
static Path solvableFile = Paths.get("../python中国象棋/data/棋型及状态总数.txt");
static Path targetFolder = Paths.get(System.getProperty("user.home")).resolve("desktop/chineseChessTable");
//士相车马炮卒，老将肯定要有
static final int[] chessCount = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 6, 6};
final static int MAX_STATE = (int) (1e7 * 6);
static int chessWeight[];
final static int WIN = 1, LOSE = 3, PEACE = 0;

TableSolver solver = new TableSolver();
Encoder encoder = new Encoder();
Decoder decoder = new Decoder();

static {
    initChessWeight();
    //创建文件夹
    if (!Files.exists(targetFolder)) try {
        Files.createDirectory(targetFolder);
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private static void initChessWeight() {
    int[] chessName = {Chess.ADVISOR, Chess.BISHOP, Chess.ROOK, Chess.KNIGHT, Chess.CANNON, Chess.PAWN};
    int[] w = new int[chessCount.length];
    int s = 1;
    for (int i = 0; i < w.length; i++) {
        w[i] = s;
        s *= chessCount[i];
    }
    chessWeight = new int[16];
    //因为棋子有负数，所以加上8，统一变成整数
    for (int i = 0; i < chessName.length; i++) {
        int ch = chessName[i];
        chessWeight[ch + 8] = w[i * 2];
        chessWeight[-ch + 8] = w[i * 2 + 1];
    }
}

class Result {
    byte peace, win, lose;//三种儿子的个数
}

/**
 * 翻转棋型
 */
int[] reverseChessType(int[] chess) {
    int[] a = new int[chess.length];
    for (int i = 0; i < a.length; i++) {
        if ((i & 1) == 0) {
            a[i] = chess[i + 1];
        } else {
            a[i] = chess[i - 1];
        }
    }
    return a;
}


/**
 * 获取全部可解棋型
 * Point的x表示棋型，y表示状态总数
 */
Point[] getSolvable() throws IOException {
    List<String> lines = Files.readAllLines(solvableFile);
    int[] a = lines.stream().mapToInt(x -> {
        if (x.length() > 9) return Integer.MAX_VALUE;
        return Integer.parseInt(x);
    }).toArray();
    System.out.println("全部棋型种数" + a.length);
    int[] solvable = Arrays.stream(a).filter(x -> x < MAX_STATE).toArray();
    System.out.println("可解棋型种数" + solvable.length + " (局面个数<" + MAX_STATE + ")");
    long sumState = Arrays.stream(solvable).mapToLong(x -> (long) x).sum();
    System.out.println("可解棋型总共状态总数" + sumState);
    System.out.println("如果每个状态使用1B，那么需要空间" + sumState / 1024.0 / 1024.0 / 1024.0 + " G");
    System.out.println("如果每个状态使用2bit，那么需要空间" + sumState / 1024.0 / 1024.0 / 1024.0 / 4 + " G");
    int[] chessCount = new int[a.length];
    int[] chessTypeByCount = new int[33];
    for (int i = 0; i < a.length; i++) {
        chessCount[i] = Arrays.stream(decodeChessType(i)).sum();
        chessTypeByCount[chessCount[i]]++;
    }
    Point[] b = new Point[solvable.length];
    int bi = 0;
    for (int i = 0; i < a.length; i++) {
        if (a[i] < MAX_STATE) {
            b[bi++] = new Point(i, a[i]);
        }
    }
    int[] sovableChessTypeByCount = new int[33];
    for (int i = 0; i < b.length; i++) {
        sovableChessTypeByCount[chessCount[b[i].x]]++;
    }
    System.out.println("不包括双将,可解的局面占全部局面的个数");
    for (int i = 0; i < 33; i++) {
        if (sovableChessTypeByCount[i] != 0) {
            System.out.println("棋子数为" + i + "个,可解棋型" + sovableChessTypeByCount[i] + "/" + chessTypeByCount[i]);
        }
    }
    return b;
}

/**
 * 解析棋型
 */
int[] decodeChessType(int x) {
    int[] a = new int[chessCount.length];
    for (int i = a.length - 1; i >= 0; i--) {
        a[i] = x % chessCount[i];
        x /= chessCount[i];
    }
    return a;
}

int encodeChessType(int[] a) {
    int s = 0;
    int w = 1;
    for (int i = a.length - 1; i >= 0; i--) {
        s += a[i] * w;
        w *= chessCount[i];
    }
    return s;
}

//初始化各个局面的胜负和
class EatExpand extends Expand {
    Result result;
    int reverseChessType;

    void init(int reverseChessType) {
        result = new Result();
        this.reverseChessType = reverseChessType;
    }

    @Override
    public void move(Node root, int fx, int fy, int tx, int ty) {
        if (root.a[tx][ty] != 0) {
            //如果吃子
            //需要考虑憋死的情况
            if (root.a[tx][ty] == -Chess.KING) {//如果能够直接灭掉敌方老将，则直接判为胜利
                result.lose = 1;
                result.win = result.peace = 0;
            } else {
                byte eat = root.a[tx][ty];
                root.move(fx, fy, tx, ty);
                int son = encoder.encode(root);
                root.move(tx, ty, fx, fy);
                root.a[tx][ty] = eat;
                //求吃子之后的棋型编码
                int nextChessType = reverseChessType - chessWeight[eat + 8];
                int res = solver.query(nextChessType, son);
                if (res == LOSE) result.lose++;
                else if (res == PEACE) result.peace++;
                else result.win++;//1
            }
        } else {
            result.peace++;
        }
    }
}

class MoveExpand extends Expand {
    OneTimeQueue q;
    Result a[];
    int reverseChessType;
    int nowResult;

    /**
     * 查看到达当前局面的所有局面，需要逆向推出有哪些局面可以到达当前局面
     * 这个过程中一定没有吃子操作
     * 整个棋盘上除了马，别的棋子操作都是可逆的，所以只需要重写一下马的移动操作
     * 把马腿的位置改一下即可
     */
    int leg[][] = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
    int targetPos[][] = {{-2, 0}, {-1, -2}, {-2, 1}, {-1, 2}, {1, -2}, {2, -1}, {1, 2}, {2, 1}};

    @Override
    public void ma(Node root, int x, int y) {
        for (int i = 0; i < targetPos.length; i++) {
            int tx = x + targetPos[i][0], ty = y + targetPos[i][1];
            if (!legal(tx, ty)) continue;
            int l = i >> 1;
            int legX = x + leg[l][0], legY = y + leg[l][1];
            if (root.a[legX][legY] == 0) {
                move(root, x, y, tx, ty);
            }
        }
    }

    @Override
    public void move(Node root, int fx, int fy, int tx, int ty) {
        if (root.a[tx][ty] == 0) {//只考虑移动，不考虑吃子，因为吃子的在初始化的时候已经考虑过了
            root.move(fx, fy, tx, ty);
            int father = encoder.encode(root);
            root.move(tx, ty, fx, fy);
            if (nowResult == WIN) {
                a[father].win++;
                a[father].peace--;
                //父节点必败
                if (a[father].peace == 0 && a[father].lose == 0) {
                    q.add(father);
                }
            } else if (nowResult == LOSE) {
                a[father].lose++;
                a[father].peace--;
                q.add(father);//父节点必胜
            }
        }
    }

    public void init(OneTimeQueue q, Result[] a, int reverseChessType, int nowState) {
        this.q = q;
        this.a = a;
        this.reverseChessType = reverseChessType;
        nowResult = analyzeResult(a[nowState]);
        if (nowResult == PEACE) {//队列里面的结点必然是必胜/必败,而不能是和棋
            throw new RuntimeException("impossible");
        }
    }
}

int analyzeResult(Result r) {
    if (r.lose > 0) return 1;//必胜
    if (r.peace > 0) return 0;//比和
    return 3;//必败
}

Result[] handle(int chessType, int stateCount) {
    int chess[] = decodeChessType(chessType);
    int reverseChessType = encodeChessType(reverseChessType(chess));
    Result[] a = new Result[stateCount];
    //执行初始化操作
    OneTimeQueue q = new OneTimeQueue(stateCount);

    EatExpand eatExpand = new EatExpand();
    MoveExpand moveExpand = new MoveExpand();

    for (int i = 0; i < stateCount; i++) {
        Node node = decoder.decode(chess, i);
        eatExpand.init(reverseChessType);
        eatExpand.expand(node);
        int res = analyzeResult(eatExpand.result);
        a[i] = eatExpand.result;
        if (res != 0) {
//            q.add(i);
        }
    }
    while (!q.isEmpty()) {
        int state = q.poll();
        Node node = decoder.decode(chess, state);
        moveExpand.init(q, a, reverseChessType, state);
        moveExpand.expand(node);
    }
    return a;
}

/**
 * 把求出来的每个局面的状态保存下来，需要考虑压缩
 * 如果某个棋型，整个都是一边倒，则利用规则进行一些压缩
 */
void dump(int chessType, Result[] res) {
    for (int i = 0; i < chessType; i++) {

    }
}

void test() {
    for (int i = 0; i < 10000; i++) {
        if (i != encodeChessType(decodeChessType(i))) {
            throw new RuntimeException("baga");
        }
    }
}

void go() throws IOException {
    Point[] a = getSolvable();
    int cnt = 0;
    long beginTime = System.currentTimeMillis();
    long s = 1;
    long total = Arrays.stream(a).mapToLong(x -> x.y).sum();
    for (Point i : a) {
        //如果已经生成了，则跳过
        if (Files.exists(targetFolder.resolve(i.x + ".bin"))) {
            total -= i.y;
            continue;
        }
        Result res[] = handle(i.x, i.y);
        dump(i.x, res);
        s += i.y;
        double need = (System.currentTimeMillis() - beginTime) / 1000.0 / 3600.0 * total / s;
        System.out.println(cnt++ + "/" + a.length + " usedTime=" + (System.currentTimeMillis() - beginTime) / 1000.0 + "s totalState=" + i.y + " needHour=" + need);
    }
}

public static void main(String[] args) throws IOException {
    new TableGenerator().go();
}
}
