package end;

import java.io.IOException;
import java.nio.file.Files;

/**
 * 查看某个棋型的状态总数
 */
public class ChessTypeCount {
public static void main(String[] args) throws IOException {
    TableGenerator g = new TableGenerator();
    int index = g.encodeChessType(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1});
    String s = Files.readAllLines(TableGenerator.solvableFile).get(index);
    System.out.println(s);
}
}
