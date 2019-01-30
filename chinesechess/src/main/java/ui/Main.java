package ui;

import engine.Ai0;
import javafx.util.Pair;
import xqwlight.Ai2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * 上半部分是红色棋子，棋子值大于0，下半部分是黑色棋子，棋子值小于0
 * redTurn=true时，红色棋子走棋，redTurn=false时，黑色棋子走棋
 */
public class Main extends JFrame {

//图片加载器
static class ImageLoader {
    static Map<String, BufferedImage> ma = new TreeMap<>();

    static BufferedImage get(String path) {
        if (ma.get(path) == null) {
            try {
                ma.put(path, ImageIO.read(new File(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ma.get(path);
    }
}

//棋盘画板
class BoardPanel extends JPanel {
    private int movingX = -1, movingY = -1;//当前选中的棋子（准备移动的棋子）
    final int MOVING_BIGGER = 4;//正在移动的棋子需要变大的尺寸
    final int CHESS_SIZE = 58;//棋盘格子的大小
    final int PAD = 22;//棋盘上的留白

    int reverseColor = 1;//是否颜色翻转

    BoardPanel() {
        addMouseListener(listenMouse);
        setPreferredSize(new Dimension(PAD * 2 + 9 * CHESS_SIZE, PAD * 2 + CHESS_SIZE * 10));
    }

    public void paint(Graphics gg) {
        BufferedImage bit = ImageLoader.get("img/main.gif");
        Graphics2D g = (Graphics2D) gg;
        g.clearRect(0, 0, getWidth(), getHeight());
        g.drawImage(bit, 0, 0, null);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                int value = node.a[i][j] * reverseColor;
                if (value == 0)
                    continue;
                BufferedImage pic = ImageLoader.get("img/" + value + ".gif");
                if (movingX == i && movingY == j) {
                    //对于当前正在移动的棋子，画的大些
                    g.drawImage(pic, j * CHESS_SIZE + PAD - MOVING_BIGGER, i * CHESS_SIZE + PAD - MOVING_BIGGER, CHESS_SIZE + MOVING_BIGGER * 2, CHESS_SIZE + MOVING_BIGGER * 2, null);
                } else {
                    g.drawImage(pic, j * CHESS_SIZE + PAD, i * CHESS_SIZE + PAD, null);
                }
            }
        }
        //如果选中了某个棋子，把这个棋子可以去的地方都用方框标出来
        if (movingX != -1) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 9; j++) {
                    if (canReach[movingX][movingY][i][j]) {
                        g.drawRect(j * CHESS_SIZE + 38, i * CHESS_SIZE + 38, 20, 20);
                    }
                }
            }
        }
        //绘制刚刚移动过的棋子
        int pos = bookPanel.lis.getSelectedIndex();
        if (book.size() > 0 && pos > 0) {
            Move m = book.get(pos);
            g.setColor(Color.RED);
            g.fillOval(m.fy * CHESS_SIZE + 40, m.fx * CHESS_SIZE + 40, 10, 10);
            g.setStroke(new BasicStroke(2));
            g.drawRect(m.ty * CHESS_SIZE + PAD, m.tx * CHESS_SIZE + PAD, 50, 50);
        }
    }

    public Point parsePosition(int mouseX, int mouseY) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 9; j++) {
                int x = j * CHESS_SIZE + PAD + CHESS_SIZE / 2;
                int y = i * CHESS_SIZE + PAD + CHESS_SIZE / 2;
                if (Math.hypot(x - mouseX, y - mouseY) < CHESS_SIZE / 2) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }

    void clear() {
        movingX = movingY = -1;
        repaint();
    }

    MouseAdapter listenMouse = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (redTurn) return;//轮不到人走棋
            Point p = boardPanel.parsePosition(e.getX(), e.getY());
            if (p == null) return;
            //重复点击，取消焦点
            if (movingX == p.x && movingY == p.y) {
                movingX = movingY = -1;
                repaint();
            } else if (node.a[p.x][p.y] < 0) {//点击的是自己的棋子，并且轮到人走，重新选中焦点
                movingX = p.x;
                movingY = p.y;
                repaint();
            } else if (movingX != -1 && canReach[movingX][movingY][p.x][p.y]) {//如果当前棋子可以去往点击的位置
                Main.this.move(movingX, movingY, p.x, p.y);
            }

        }
    };
}

class BookPanel extends JPanel {
    DefaultListModel<String> model = new DefaultListModel<>();
    JList<String> lis = new JList<>(model);

    BookPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));
        lis.setDragEnabled(false);
        JScrollPane scrollPane = new JScrollPane(lis);
        scrollPane.setAutoscrolls(true);
        add(scrollPane);
    }

    public void updateBook() {
        List<String> a = Record.getRecord(startNode, book);
        for (int i = 0; i < a.size(); i++) {
            if (model.size() <= i) {
                model.add(i, a.get(i));
                lis.setSelectedIndex(i);
            } else {
                if (!a.get(i).equals(model.get(i))) {
                    model.set(i, a.get(i));
                }
            }
        }
        if (model.size() != a.size()) {
            model.removeRange(a.size(), model.size() - 1);
            if (lis.getSelectedIndex() > model.size()) {
                lis.setSelectedIndex(-1);
            }
        }
    }
}

class StatusBar extends JToolBar {
    JLabel label = new JLabel("hello");

    StatusBar() {
        add(label);
        setFont(new Font("Consolas", Font.PLAIN, 20));
        setFloatable(false);
    }

    void setInfo(String text) {
        label.setText(text);
    }
}

//设置双方玩家
static class Player {//玩家
    static AI red = getAi1();
    static AI black = null;//为null表示让人移动

    static AI getAi0() {
        return new Ai0();
    }

    static AI getAi1() {
        return new Ai2();
    }
}

abstract class AiThread implements Runnable {

    abstract AI getAI();

    abstract boolean amIred();//我是红色吗？

    @Override
    public void run() {
        while (true) {
            //如果没有轮到我，或者我没有AI
            if (amIred() != redTurn || getAI() == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (amIred()) {
                String op = getAI().solve(node.toFen());
                int[] pos = Arrays.stream(op.split("\\s+")).mapToInt(Integer::parseInt).toArray();
                if (amIred() != redTurn || getAI() == null) return;
                move(pos[0], pos[1], pos[2], pos[3]);
            } else {
                String op = getAI().solve(node.newSon().toFen());
                int[] pos = Arrays.stream(op.split("\\s+")).mapToInt(Integer::parseInt).toArray();
                if (amIred() != redTurn || getAI() == null) return;
                move(9 - pos[0], pos[1], 9 - pos[2], pos[3]);
            }
        }
    }
}

Node startNode;//开始局面
boolean startRedTurn = true;//开始局面轮到谁走棋

BoardPanel boardPanel = new BoardPanel();//棋盘展示器
BookPanel bookPanel = new BookPanel();//棋谱记录器
StatusBar statusBar = new StatusBar();

boolean[][][][] canReach;//当前局面每个棋子可以到达的位置
Node node;//当前局面
volatile boolean redTurn = false;
List<Move> book = new ArrayList<>(100);//棋谱记录
AiThread redThread = new AiThread() {

    @Override
    AI getAI() {
        return Player.red;
    }

    @Override
    boolean amIred() {
        return true;
    }


};
AiThread blackThread = new AiThread() {
    @Override
    AI getAI() {
        return Player.black;
    }

    @Override
    boolean amIred() {
        return false;
    }
};


public static void main(String[] args) throws Exception {
    new Main();
}

//移动到棋谱中的某一步
Node go(int pos) {
    Node now = startNode.copy();
    boolean turn = startRedTurn;
    for (int i = 0; i <= pos; i++) {
        Move m = book.get(i);
        now = now.move(m.fx, m.fy, m.tx, m.ty);
        turn = !turn;
    }
    node = now;
    redTurn = turn;
    canReach = getCanReach(node);
    boardPanel.clear();
    return now;
}

//判断着法是否可行，如果正在被将军，则必须应将
boolean legal(int fx, int fy, int tx, int ty) {
    if (!canReach[fx][fy][tx][ty]) {
        return false;
    }
    Node now = node.copy().move(fx, fy, tx, ty);
    //如果现在是电脑在走，那么下一步轮到人走
    if (redTurn) now = now.newSon();
    int score = OverJudger.judge(now, 2);
    if (score == 1) return false;//如果我走完一步之后，对手必胜，那么我不能走这步
    return true;
}

void move(int fx, int fy, int tx, int ty) {
    if (!legal(fx, fy, tx, ty)) {
        throw new RuntimeException("invalid move");
    }
    book.add(new Move(fx, fy, tx, ty));//入栈
    bookPanel.updateBook();
    node.move(fx, fy, tx, ty);
    canReach = getCanReach(node);
    int currentGameState = getGameState();
    if (currentGameState != 0) {
        this.repaint();
        if (currentGameState == -1) {
            info("man win ! how smart you are.");
        } else {
            info("man lose ! how stupid you are.");
        }
        newGame();
    } else {
        redTurn = !redTurn;
    }
    statusBar.setInfo("轮到" + (redTurn ? "敌人" : "我方") + "走棋");
    boardPanel.clear();//擦除掉moving的痕迹
}

//获取当前局面的可行着法
boolean[][][][] getCanReach(Node node) {
    final boolean reach[][][][] = new boolean[10][9][10][9];
    Expand pand = new Expand() {
        public void move(Node root, int fx, int fy, int tx, int ty) {
            reach[fx][fy][tx][ty] = true;
        }
    };
    pand.expand(node);
    Expand pand2 = new Expand() {
        public void move(Node root, int fx, int fy, int tx, int ty) {
            reach[9 - fx][fy][9 - tx][ty] = true;
        }
    };
    pand2.expand(node.newSon());
    return reach;
}

//只处理数据，不处理界面，新开游戏
void newGame() {
    redTurn = startRedTurn;
    node = startNode.copy();
    book.clear();
    canReach = getCanReach(node);
}

//判断游戏是否结束,0表示未结束，1表示敌人胜利，-1表示我胜利
//需要在绝杀的那一步进行判定输赢
int getGameState() {
    Node no = node;
    //如果上一步是计算机走的，注意计算gameState的时候，computerTurn还没有变化
    if (redTurn) no = no.newSon();
    int score = OverJudger.judge(no, 3);
    if (redTurn) score = -score;
    return score;
}

void info(String s) {
    JOptionPane.showMessageDialog(this, s);
}

JMenuBar getMenubar() {
    JMenuBar menubar = new JMenuBar();
    JMenu game = new JMenu("游戏");
    JMenuItem replay = new JMenuItem("重玩");
    game.add(replay);
    replay.addActionListener(e -> {
        newGame();
        boardPanel.clear();
    });
    game.add(new JMenuItem("加载FEN"));

    JMenu engine = new JMenu("设置");
    engine.add(new JLabel("敌人"));

    ActionListener listenSelectEngine = e -> {
        switch (e.getActionCommand()) {
            case "red-ai0":
                Player.red = Player.getAi0();
                break;
            case "red-ai1":
                Player.red = Player.getAi1();
                break;
            case "black-ai0":
                Player.black = Player.getAi0();
                break;
            case "black-man":
                Player.black = null;
                break;
            case "black-ai1":
                Player.black = Player.getAi1();
                break;
        }
    };

    ButtonGroup bg = new ButtonGroup();
    for (Pair<String, String> i : Arrays.asList(new Pair<>("ai0", "red-ai0"), new Pair<>("ai1", "red-ai1"))) {
        JRadioButtonMenuItem use = new JRadioButtonMenuItem(i.getKey());
        if (i.getKey().equals("ai1")) use.setSelected(true);
        use.setActionCommand(i.getValue());
        use.addActionListener(listenSelectEngine);
        bg.add(use);
        engine.add(use);
    }
    bg = new ButtonGroup();
    engine.add(new JLabel("我方"));
    for (Pair<String, String> i : Arrays.asList(new Pair<>("ai0", "black-ai0"), new Pair<>("ai1", "black-ai1"), new Pair<>("man", "black-man"))) {
        JRadioButtonMenuItem use = new JRadioButtonMenuItem(i.getKey());
        if (i.getKey().equals("man")) use.setSelected(true);
        use.setActionCommand(i.getValue());
        use.addActionListener(listenSelectEngine);
        bg.add(use);
        engine.add(use);
    }

    JMenu view = new JMenu("视图");
    JCheckBoxMenuItem revertColor = new JCheckBoxMenuItem("颜色翻转");
    view.add(revertColor);
    revertColor.addActionListener(e -> {
        boardPanel.reverseColor = revertColor.getState() ? -1 : 1;
        boardPanel.repaint();
    });
    JMenuItem flipHorizon = new JMenuItem("水平翻转");
    view.add(flipHorizon);
    flipHorizon.addActionListener(e -> {
        node = node.flipHorizon();
        boardPanel.repaint();
    });
    JMenuItem flipVertical = new JMenuItem("竖直翻转");
    view.add(flipVertical);
    flipVertical.addActionListener(e -> {
        node = node.newSon();
        canReach = getCanReach(node);
        book.clear();
        bookPanel.updateBook();
        boardPanel.repaint();
    });

    menubar.add(game);
    menubar.add(engine);
    menubar.add(view);
    return menubar;
}

Main() throws Exception {
    startNode = Utility.kaiJu();
    newGame();
    setTitle("中国象棋");
    setIconImage(ImageIO.read(new File("img/-4.gif")));
    JPanel mainPanel = new JPanel(new BorderLayout());
    setJMenuBar(getMenubar());
    mainPanel.add(BorderLayout.CENTER, boardPanel);
    mainPanel.add(BorderLayout.EAST, bookPanel);
    mainPanel.add(BorderLayout.SOUTH, statusBar);
    getContentPane().add(mainPanel);
    addKeyListener(listenKey);
    pack();
    setResizable(false);
    setVisible(true);
    new Thread(redThread).start();
    new Thread(blackThread).start();
    setLocationRelativeTo(null);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
}

//TODO:更多快捷键
KeyAdapter listenKey = new KeyAdapter() {
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE://退格键悔棋
                node = go(book.size() - 1);
                break;
        }
    }
};

}

