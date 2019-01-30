package ui;


public class Utility {
public static Node kaiJu() {
    Node start = new Node();
    byte[][] a = new byte[10][9];
    a[0][0] = a[0][8] = 1;
    a[0][1] = a[0][7] = 2;
    a[2][1] = a[2][7] = 3;
    a[0][2] = a[0][6] = 4;
    a[0][3] = a[0][5] = 5;
    a[0][4] = 6;
    a[3][0] = a[3][2] = a[3][4] = a[3][6] = a[3][8] = 7;
    for (int i = 0; i < 5; i++) {
        for (int j = 0; j < 9; j++) {
            a[9 - i][j] = (byte) -a[i][j];
        }
    }
    start.a = a;
    return start;
}

static Node canJu() {
    byte[][] a = new byte[10][9];
    a[0][4] = Chess.KING;
    a[4][6] = -Chess.CANNON;
    a[2][5] = Chess.ROOK;
    a[7][4] = Chess.ROOK;
    a[8][5] = -Chess.KING;
//    a[6][4] = Chess.CANNON;
    Node ans = new Node();
    ans.a = a;
    // ans=ans.newSon();
    return ans;
}

static void draw(Node board) {
    char[] chessName = "车马炮相士将卒".toCharArray();
    cout("\n");
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 9; j++)
            cout(chessName[Math.abs(board.a[i][j])] + "");
        cout("\n");
    }
}

static void show(byte[][] a, String title) {
    cout(title + "\n");
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 9; j++)
            if (a[i][j] == 0) cout(".");
            else cout(Chess.toEnglish(a[i][j]) + "");
        cout("\n");
    }
}

static void cout(String s) {
    System.out.print(s);
}
}
