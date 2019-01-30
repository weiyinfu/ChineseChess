package ui;

import java.awt.Point;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * 棋谱计算器，使用可读性较好的方法展示棋谱
 * 如：
 * 车五进二、炮三平四
 * <p>
 * 路：棋盘一共有九路纵队
 * <p>
 * 我方从右往左为一二三四五六七八九
 * 敌方从左往右为123456789
 * <p>
 * 这样做的好处是：棋谱与棋盘视图相分离，二者完全对称
 * <p>
 * 进退平三种操作
 * 若为进退，则说  棋子、出发点的路数、进退、进退的数量
 * 若为平，则说 棋子、出发点的路数、平、到达的路数
 *
 * 对于走直线的棋子，进退的宾语是移动的格子数
 * 对于马相士这三种不走直线的棋子，没有“平”只有“进退”，进退直接说从几路到几路而不说进退了多少格
 * <p>
 * 一种歧义：共路的两个车、两个炮、若干个卒无法区分是第几个
 * 故说：前车进二、后炮退三
 * <p>
 * 对于卒子，可能依旧无法区分，那就采用：一卒进二，二卒平一这种说法，一卒指的是从左上角往右下角数第几个卒
 */
public class Record {
static String getKey(int chess, int fx, int fy) {
    return chess + "," + fy;
}

//获取路数
static String getFile(byte chess, int y) {
    if (chess > 0) {
        return "123456789".charAt(y) + "";
    } else if (chess < 0) {
        return "一二三四五六七八九".charAt(8 - y) + "";
    } else {
        throw new RuntimeException("impossible! here is space ");
    }
}

//获取主语
static String getNoun(Node node, int fx, int fy, TreeMap<String, List<Point>> nameCount) {
    String name = getKey(node.a[fx][fy], fx, fy);
    List<Point> ps = nameCount.get(name);
    if (ps.size() == 1) return Chess.toChar(node.a[fx][fy]) + getFile(node.a[fx][fy], fy);
    else if (ps.size() == 2 && Math.abs(node.a[fx][fy]) != Chess.PAWN) {
        int who = node.a[fx][fy] < 0 ? 0 : 1;
        int front = fy == ps.get(0).y ? 0 : 1;
        return "前后".charAt(who ^ front) + "" + Chess.toChar(node.a[fx][fy]);
    } else {
        int cnt = 0, total = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                if (node.a[i][j] == node.a[fx][fy]) {
                    if (i < fx || (i == fx && j < fy)) {//fx,fy前面卒子的个数
                        cnt++;
                    }
                    //卒子的总个数
                    total++;
                }
            }
        }
        if (node.a[fx][fy] < 0) cnt = total - cnt;
        else cnt++;
        return " 一二三四五".charAt(cnt) + "卒";
    }
}

//解析棋子的名字
static TreeMap<String, List<Point>> parse(Node node) {
    TreeMap<String, List<Point>> a = new TreeMap<>();
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 9; j++) {
            if (node.a[i][j] != 0) {
                String name = node.a[i][j] + "," + j;
                List<Point> li = a.getOrDefault(name, new LinkedList<>());
                li.add(new Point(i, j));
                a.put(name, li);
            }
        }
    }
    for (String i : a.keySet()) {
        a.get(i).sort((o1, o2) -> {
            if (o1.y != o2.y) return o1.y - o2.y;
            return o1.x - o2.x;
        });
    }
    return a;
}

//把棋谱转换为字符串列表的形式
static List<String> getRecord(Node startNode, List<Move> moves) {
    Node now = startNode.copy();
    List<String> a = new LinkedList<>();
    for (Move i : moves) {
        byte ch = now.a[i.fx][i.fy];
        TreeMap<String, List<Point>> nameCount = parse(now);
        String noun = getNoun(now, i.fx, i.fy, nameCount);//获取主语
        int who = ch < 0 ? 0 : 1;
        String verb;//获取谓语
        if (i.fx == i.tx) {
            verb = "平" + getFile(ch, i.ty);
        } else {
            verb = "进退".charAt(1 ^ who ^ (i.fx < i.tx ? 0 : 1)) + "";
            //如果是相马士，直接使用路数而不使用步数
            if (Arrays.asList(Chess.BISHOP, Chess.KNIGHT, Chess.ADVISOR).contains(Math.abs(ch))) {
                verb += getFile(ch, i.ty);
            } else {
                verb += Math.abs(i.fx - i.tx);
            }
        }
        a.add(noun + verb);
        now.move(i.fx, i.fy, i.tx, i.ty);
    }
    return a;
}
}
