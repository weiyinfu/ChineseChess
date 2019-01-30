package ui;

public class Chess {
public static final int ROOK = 1, KNIGHT = 2, CANNON = 3, BISHOP = 4, ADVISOR = 5, KING = 6, PAWN = 7;

public static char toChar(byte ch) {
    return " 车马炮相士将卒".charAt(Math.abs(ch));
}

public static char toEnglish(byte ch) {
    String s = "rncbakp";
    if (ch < 0) s = s.toUpperCase();
    return s.charAt(Math.abs(ch) - 1);
}
}
