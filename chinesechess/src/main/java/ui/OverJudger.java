package ui;

//游戏结束判别器，一个3层的博弈树
//需要考虑憋死的情况
public class OverJudger {
//1表示必胜，-1表示必败，0表示无法确定
static int scoreOf(Node node) {
    for (int x = 0; x < 3; x++)
        for (int y = 3; y < 6; y++)
            if (node.a[x][y] == Chess.KING) {
                for (x = 9; x > 6; x--)
                    for (y = 3; y < 6; y++)
                        if (node.a[x][y] == -Chess.KING)
                            return 0;
                return 1;
            }

    return -1;
}

static int getScore(Node node, int depth, int maxDepth) {
    if (depth == maxDepth) return scoreOf(node);
    int mine = scoreOf(node);
    if (mine != 0) return mine;
    int[] mi = new int[1];
    mi[0] = Integer.MAX_VALUE;
    Expand expand = new Expand() {
        @Override
        public void move(Node root, int fx, int fy, int tx, int ty) {
            Node next = root.copy().move(fx, fy, tx, ty).newSon();
            int score = getScore(next, depth + 1, maxDepth);
            mi[0] = Math.min(mi[0], score);
        }
    };
    //mi[0]没有发生变化，说明我已经没步了
    if (mi[0] == Integer.MAX_VALUE) mi[0] = 1;//我憋死了，人家赢了
    expand.expand(node);
    return -mi[0];
}

static int judge(Node node, int maxDepth) {
    return getScore(node, 0, maxDepth);
}

public static void main(String[] args) {
    Utility.show(Utility.canJu().a, "");
    int res = judge(Utility.canJu().newSon(), 3);
    System.out.println(res);
}
}
