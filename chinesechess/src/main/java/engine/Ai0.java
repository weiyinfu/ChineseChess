package engine;

import ui.AI;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ai0 implements AI {
static SearchEngine searchEngine = new SearchEngine();

static {
    try {
        searchEngine.clearHash();
        searchEngine.clearHistTab();
        searchEngine.loadBook("book/book.zip");
    } catch (IOException e) {
        e.printStackTrace();
    }
}


@Override
public String solve(String state) {
    System.out.println("solving " + state);
    ActiveBoard board = new ActiveBoard();
    board.loadFen(state);
    searchEngine.setActiveBoard(board);
    try {
        MoveNode move = searchEngine.getBestMove();
        String op = move.toString();
        int fy = op.charAt(0) - 'a', fx = op.charAt(1) - '0', ty = op.charAt(2) - 'a', tx = op.charAt(3) - '0';
        fx = 9 - fx;
        tx = 9 - tx;
        return Stream.of(fx, fy, tx, ty).map(x -> x + "").collect(Collectors.joining(" "));
    } catch (Exception e) {
    }
    return null;
}

public static void main(String[] args) {
    Ai0 ai = new Ai0();
    //TODO：这个move为啥为空？
//    System.out.println(ai.solve("rnbakabnr/9/4c2c1/p1p1C1p1p/9/9/P3P1P1P/7C1/9/RNBAKABNR w - - - -"));
//    System.out.println(ai.solve("rnbakab1r/9/1c4nc1/p1p1C1p1p/9/9/P1P1P1P1P/7C1/9/RNBAKABNR b - - - -"));
    System.out.println(ai.solve("rnbakab1r/9/1c4nc1/p1p1p1p1p/9/9/P1P1P1P1P/2N1C2C1/9/R1BAKABNR b - - - -"));
}
}
