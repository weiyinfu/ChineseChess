package xqwlight;

import ui.AI;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ai2 implements AI {
@Override
public String solve(String state) {
    System.out.println("solving " + state);
    Position p = new Position();
    p.fromFen(state);
    Search search = new Search(p, 16);
    int action = search.searchMain(5, 2000);
    int sqSrc = Position.SRC(action);
    int sqDst = Position.DST(action);
    int fx = sqSrc / 16 - 3, fy = sqSrc % 16 - 3, tx = sqDst / 16 - 3, ty = sqDst % 16 - 3;
    return Stream.of(fx, fy, tx, ty).map(x -> x + "").collect(Collectors.joining(" "));
}

public static void main(String[] args) {
    Ai2 ai = new Ai2();
    System.out.println(ai.solve("4K4/9/9/9/9/9/9/5k3/9/3R5 w - - - -"));
}
}
