package ui;

/**
 * 只走正数棋子
 */
public abstract class Expand {
public abstract void move(Node root, int fx, int fy, int tx, int ty);

public void expand(Node root) {
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 9; j++) {
            switch (root.a[i][j]) {
                case Chess.ROOK:
                    che(root, i, j);
                    break;
                case Chess.KNIGHT:
                    ma(root, i, j);
                    break;
                case Chess.CANNON:
                    pao(root, i, j);
                    break;
                case Chess.BISHOP:
                    xiang(root, i, j);
                    break;
                case Chess.ADVISOR:
                    shi(root, i, j);
                    break;
                case Chess.KING:
                    jiang(root, i, j);
                    break;
                case Chess.PAWN:
                    zu(root, i, j);
                    break;
            }
        }
    }
}

void che(Node root, int x, int y) {
    for (int dir : new int[]{1, -1}) {
        for (int i = x + dir; i >= 0 && i < 10; i += dir) {
            if (root.a[i][y] == 0) {//车不吃子
                move(root, x, y, i, y);
            } else {
                if (root.a[i][y] < 0) {
                    move(root, x, y, i, y);
                }
                break;
            }
        }
    }
    for (int dir : new int[]{1, -1}) {
        for (int i = y + dir; i >= 0 && i < 9; i += dir) {
            if (root.a[x][i] == 0)
                move(root, x, y, x, i);
            else {
                if (root.a[x][i] < 0)
                    move(root, x, y, x, i);
                break;
            }
        }
    }
}

public boolean legal(int x, int y) {
    return x >= 0 && y >= 0 && x < 10 && y < 9;
}

public void ma(Node root, int x, int y) {
    if (legal(x - 1, y) && root.a[x - 1][y] == 0) {
        if (legal(x - 2, y - 1) && root.a[x - 2][y - 1] <= 0)
            move(root, x, y, x - 2, y - 1);
        if (legal(x - 2, y + 1) && root.a[x - 2][y + 1] <= 0)
            move(root, x, y, x - 2, y + 1);
    }
    if (legal(x + 1, y) && root.a[x + 1][y] == 0) {
        if (legal(x + 2, y - 1) && root.a[x + 2][y - 1] <= 0)
            move(root, x, y, x + 2, y - 1);
        if (legal(x + 2, y + 1) && root.a[x + 2][y + 1] <= 0)
            move(root, x, y, x + 2, y + 1);
    }
    if (legal(x, y - 1) && root.a[x][y - 1] == 0) {
        if (legal(x + 1, y - 2) && root.a[x + 1][y - 2] <= 0)
            move(root, x, y, x + 1, y - 2);
        if (legal(x - 1, y - 2) && root.a[x - 1][y - 2] <= 0)
            move(root, x, y, x - 1, y - 2);
    }
    if (legal(x, y + 1) && root.a[x][y + 1] == 0) {
        if (legal(x + 1, y + 2) && root.a[x + 1][y + 2] <= 0)
            move(root, x, y, x + 1, y + 2);
        if (legal(x - 1, y + 2) && root.a[x - 1][y + 2] <= 0)
            move(root, x, y, x - 1, y + 2);
    }
}

void pao(Node root, int x, int y) {
    for (int i = x - 1; i >= 0; i--) {
        if (root.a[i][y] == 0) {
            move(root, x, y, i, y);
        } else {
            for (i--; i >= 0; i--) {
                if (root.a[i][y] != 0) {
                    if (root.a[i][y] < 0)
                        move(root, x, y, i, y);
                    break;
                }
            }
            break;
        }
    }
    for (int i = x + 1; i < 10; i++) {
        if (root.a[i][y] == 0) {
            move(root, x, y, i, y);
        } else {
            for (i++; i < 10; i++) {
                if (root.a[i][y] != 0) {
                    if (root.a[i][y] < 0)
                        move(root, x, y, i, y);
                    break;
                }
            }
            break;
        }
    }
    for (int i = y - 1; i >= 0; i--) {
        if (root.a[x][i] == 0) {
            move(root, x, y, x, i);
        } else {
            for (i--; i >= 0; i--) {
                if (root.a[x][i] != 0) {
                    if (root.a[x][i] < 0)
                        move(root, x, y, x, i);
                    break;
                }
            }
            break;
        }
    }
    for (int i = y + 1; i < 9; i++) {
        if (root.a[x][i] == 0) {
            move(root, x, y, x, i);
        } else {
            for (i++; i < 9; i++) {
                if (root.a[x][i] != 0) {
                    if (root.a[x][i] < 0)
                        move(root, x, y, x, i);
                    break;
                }
            }
            break;
        }
    }
}

void xiang(Node root, int x, int y) {
    if (x < 4) {
        if (y < 8 && root.a[x + 1][y + 1] == 0 && root.a[x + 2][y + 2] <= 0) {
            move(root, x, y, x + 2, y + 2);
        }
        if (y > 0 && root.a[x + 1][y - 1] == 0 && root.a[x + 2][y - 2] <= 0) {
            move(root, x, y, x + 2, y - 2);
        }
    }
    if (x > 0) {
        if (y > 0 && root.a[x - 1][y - 1] == 0 && root.a[x - 2][y - 2] <= 0) {
            move(root, x, y, x - 2, y - 2);
        }
        if (y < 8 && root.a[x - 1][y + 1] == 0 && root.a[x - 2][y + 2] <= 0) {
            move(root, x, y, x - 2, y + 2);
        }
    }
}

void shi(Node root, int x, int y) {
    if (x != 1) {
        if (root.a[1][4] <= 0) {
            move(root, x, y, 1, 4);
        }
    } else {
        if (root.a[0][3] <= 0)
            move(root, x, y, 0, 3);
        if (root.a[0][5] <= 0)
            move(root, x, y, 0, 5);
        if (root.a[2][3] <= 0)
            move(root, x, y, 2, 3);
        if (root.a[2][5] <= 0)
            move(root, x, y, 2, 5);
    }
}

void jiang(Node root, int x, int y) {
    if (y < 5 && root.a[x][y + 1] <= 0) {
        move(root, x, y, x, y + 1);
    }
    if (y > 3 && root.a[x][y - 1] <= 0) {
        move(root, x, y, x, y - 1);
    }
    if (x > 0 && root.a[x - 1][y] <= 0) {
        move(root, x, y, x - 1, y);
    }
    if (x < 2 && root.a[x + 1][y] <= 0) {
        move(root, x, y, x + 1, y);
    }
    for (int i = x + 1; i < 10; i++) {
        if (root.a[i][y] != 0) {
            if (root.a[i][y] == -6) {
                move(root, x, y, i, y);
            }
            break;
        }
    }
}

void zu(Node root, int x, int y) {
    if (x < 5) {
        if (root.a[x + 1][y] <= 0) {
            move(root, x, y, x + 1, y);
        }
    } else {
        if (legal(x + 1, y) && root.a[x + 1][y] <= 0) {
            move(root, x, y, x + 1, y);
        }
        if (legal(x, y - 1) && root.a[x][y - 1] <= 0) {
            move(root, x, y, x, y - 1);
        }
        if (legal(x, y + 1) && root.a[x][y + 1] <= 0) {
            move(root, x, y, x, y + 1);
        }
    }
}
}
