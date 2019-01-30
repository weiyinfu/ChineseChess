package ui;

/**
 * 棋盘局面
 * 车马炮相士将卒
 * rook,knight,cannon,bishop,advisor,king,pawn
 * <p>
 * 1,2,3,4,5,6,7
 * -1,-2,-3,-4,-5,-6,-7
 * <p>
 * rncbakp
 * 大写表示一方，小写表示另一方
 * 0空白
 * 1234567：车马炮相士将卒
 */
public class Node {
public byte[][] a;// 10*9 chess board，正数九宫在上半部分

/**
 * 上下翻转棋盘+翻转颜色
 */
public Node newSon() {
    byte[][] a = new byte[10][9];
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 9; j++) {
            a[i][j] = (byte) -this.a[9 - i][j];
        }
    }
    Node node = new Node();
    node.a = a;
    return node;
}

public Node move(int fx, int fy, int tx, int ty) {
    Node node = this;
    node.a[tx][ty] = node.a[fx][fy];
    node.a[fx][fy] = 0;
    return node;
}

Node copy() {
    Node node = new Node();
    byte[][] b = new byte[a.length][a[0].length];
    for (int i = 0; i < b.length; i++) {
        for (int j = 0; j < b[i].length; j++) {
            b[i][j] = a[i][j];
        }
    }
    node.a = b;
    return node;
}

public String toFen() {
    /**
     * 小写的九宫必须在上半部分，小写的棋子对应fen中黑色，总是轮到黑方走棋
     * */
    StringBuilder builder = new StringBuilder(120);
    for (int i = 0; i < a.length; i++) {
        for (int j = 0; j < a[i].length; ) {
            if (a[i][j] == 0) {
                int cnt = 0;
                while (j < a[i].length && a[i][j] == 0) {
                    cnt++;
                    j++;
                }
                builder.append(cnt);
            } else {
                builder.append(Chess.toEnglish(a[i][j]));
                j++;
            }
        }
        if (i != a.length - 1)
            builder.append("/");
    }
    builder.append(" b - - - -");
    return builder.toString();
}

public static void main(String[] args) {
    Node node = Utility.kaiJu().move(0, 1, 2, 2);
    Utility.show(node.a, "haah");
}

public Node flipHorizon() {
    byte[][] b = new byte[10][9];
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 9; j++) {
            b[i][j] = a[i][8 - j];
        }
    }
    Node no = new Node();
    no.a = b;
    return no;
}
}
