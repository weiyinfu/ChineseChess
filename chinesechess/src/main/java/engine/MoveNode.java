package engine;

public class MoveNode {
int src, cap, dst;
boolean chk;

// "cap" & "chk" are only used in history moves
MoveNode() {
    src = dst = cap = -1;
    chk = false;
}

// public MoveNode(int s, int d)
public MoveNode(int s, int d) {
    src = s;
    dst = d;
    chk = false;
}

MoveNode(String moveStr) {
    move(moveStr);
    chk = false;
}

char[] location() {
    char[] loc = new char[4];
    loc[0] = (char) (src / 10 + 'a');//第几列
    loc[1] = (char) (src % 10 + '0');//第几行
    loc[2] = (char) (dst / 10 + 'a');
    loc[3] = (char) (dst % 10 + '0');
    return loc;
}

public void move(String moveStr) {
    src = (char) (ActiveBoard.BOTTOM[moveStr.charAt(0) - 'a'] + moveStr.charAt(1) - '0');
    dst = (char) (ActiveBoard.BOTTOM[moveStr.charAt(2) - 'a'] + moveStr.charAt(3) - '0');
    if (src < 0 || src >= 90 || dst < 0 || dst >= 90) {// invalid move
        src = dst = -1;
    }
}

// for test
public String toString() {
    return (src >= 0 && dst >= 0) ? String.copyValueOf(location()) : src + "-->" + dst;
}
}
