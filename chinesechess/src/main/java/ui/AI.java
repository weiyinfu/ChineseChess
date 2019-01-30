package ui;

/**
 * 所有AI需要遵守的接口
 */
public interface AI {
/**
 * @param state:fen字符串
 * @return 下一步着法，fx，fy，tx，ty，四个int之间用空格隔开
 */
String solve(String state);
}
