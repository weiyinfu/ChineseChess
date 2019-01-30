/*
 * 创建日期 2005-3-18
 *
 * 更改所生成文件模板为
 * 窗口 > 首选项 > Java > 代码生成 > 代码和注释
 */
package engine;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Random;
import java.util.zip.ZipInputStream;

public class SearchEngine {
public static final int MaxBookMove = 40;// 使用开局库的最大步数
public static final int MaxKiller = 4;// 搜索杀着的最大步数
private static final int BookUnique = 1;// 指示结点类型，下同
private static final int BookMulti = 2;
private static final int HashAlpha = 4;
private static final int HashBeta = 8;
private static final int HashPv = 16;

private static final int ObsoleteValue = -CCEvalue.MaxValue - 1;
private static final int UnknownValue = -CCEvalue.MaxValue - 2;
// private static final int BookUniqueValue = CCEvalue.MaxValue + 1;
// private static final int BookMultiValue = CCEvalue.MaxValue +
// 2;//推荐使用开局库,值要足够大

public static final int CLOCK_S = 1000;// 1秒=1000毫秒
public static final int CLOCK_M = 1000 * 60;// 1分=60秒
private static final Random rand = new Random();
private MoveNode bestMove = null;

// for search control
private int depth;
private long properTimer, limitTimer;
// 搜索过程的全局变量，包括：
// 1. 搜索树和历史表

private ActiveBoard activeBoard;
private int histTab[][];

public void setActiveBoard(ActiveBoard activeBoard) {
    this.activeBoard = activeBoard;
}

// 2. 搜索选项
private int selectMask, style;// 下棋风格 default = EngineOption.Normal;
private boolean wideQuiesc, futility, nullMove;
// SelectMask:随机性 , WideQuiesc(保守true if Style == EngineOption.solid)
// Futility(true if Style == EngineOption.risky冒进)
// NullMove 是否空着剪裁
private boolean ponder;
// 3. 时间控制参数
private long startTimer, minTimer, maxTimer;
private int startMove;
private boolean stop;
// 4. 统计信息：Ai0 Search Nodes, Quiescence Search Nodes and Hash Nodes
private int nodes, nullNodes, hashNodes, killerNodes, betaNodes, pvNodes, alphaNodes, mateNodes, leafNodes;
private int quiescNullNodes, quiescBetaNodes, quiescPvNodes, quiescAlphaNodes, quiescMateNodes;
private int hitBeta, hitPv, hitAlpha;
// 5. 搜索结果
private int lastScore, pvLineNum;
private MoveNode pvLine[] = new MoveNode[ActiveBoard.MAX_MOVE_NUM];

// 6. Hash and Book Structure
private int hashMask, maxBookPos, bookPosNum;
private HashRecord[] hashList;
private BookRecord[] bookList;

public SearchEngine(ActiveBoard chessP) {
    this();
    activeBoard = chessP;
}

public SearchEngine() {
    int i;
    // Position = new ChessPosition();
    histTab = new int[90][90];
    nodes = nullNodes = hashNodes = killerNodes = betaNodes = pvNodes = alphaNodes = mateNodes = leafNodes = 0;
    selectMask = 0;// 1<<10-1;//随机性
//    style = true;//EngineOption.Normal;
    wideQuiesc = false;//保守style == EngineOption.Solid;
    futility = true;//冒进 style == EngineOption.Risky;
    nullMove = false;
    // Search results
    lastScore = 0;
    pvLineNum = 0;
    MoveNode PvLine[] = new MoveNode[ActiveBoard.MAX_MOVE_NUM];
    for (i = 0; i < ActiveBoard.MAX_MOVE_NUM; i++) {
        PvLine[i] = new MoveNode();
    }
    newHash(17, 14);
    depth = 8;
    properTimer = CLOCK_M * 1;
    limitTimer = CLOCK_M * 20;
}

// Begin History and Hash Table Procedures
public void newHash(int HashScale, int BookScale) {
    histTab = new int[90][90];
    hashMask = (1 << HashScale) - 1;
    maxBookPos = 1 << BookScale;
    hashList = new HashRecord[hashMask + 1];
    for (int i = 0; i < hashMask + 1; i++) {
        hashList[i] = new HashRecord();
    }
    bookList = new BookRecord[maxBookPos];
    // for (int i=0; i< MaxBookPos; i++){
    // BookList[i]=new BookRecord();
    // }

    clearHistTab();
    clearHash();
    // BookRand = rand.nextLong();//(unsigned long) time(NULL);
}

public void delHash() {
    histTab = null;
    hashList = null;
    bookList = null;
}

public void clearHistTab() {
    int i, j;
    for (i = 0; i < 90; i++) {
        for (j = 0; j < 90; j++) {
            histTab[i][j] = 0;
        }
    }
}

public void clearHash() {
    int i;
    for (i = 0; i <= hashMask; i++) {
        hashList[i].flag = 0;
    }
}

private int probeHash(MoveNode HashMove, int Alpha, int Beta, int Depth) {
    boolean MateNode;
    HashRecord TempHash;
    int tmpInt = (int) (activeBoard.getZobristKey() & hashMask);
    long tmpLong1 = activeBoard.getZobristLock(), tmpLong2;
    TempHash = hashList[(int) (activeBoard.getZobristKey() & hashMask)];
    tmpLong2 = TempHash.zobristLock;
    if (TempHash.flag != 0 && TempHash.zobristLock == activeBoard.getZobristLock()) {
        MateNode = false;
        if (TempHash.value > CCEvalue.MaxValue - ActiveBoard.MAX_MOVE_NUM / 2) {
            TempHash.value -= activeBoard.getMoveNum() - startMove;
            MateNode = true;
        } else if (TempHash.value < ActiveBoard.MAX_MOVE_NUM / 2 - CCEvalue.MaxValue) {
            TempHash.value += activeBoard.getMoveNum() - startMove;
            MateNode = true;
        }
        if (MateNode || TempHash.depth >= Depth) {
            if ((TempHash.flag & HashBeta) != 0) {
                if (TempHash.value >= Beta) {
                    hitBeta++;
                    return TempHash.value;
                }
            } else if ((TempHash.flag & HashAlpha) != 0) {
                if (TempHash.value <= Alpha) {
                    hitAlpha++;
                    return TempHash.value;
                }
            } else if ((TempHash.flag & HashPv) != 0) {
                hitPv++;
                return TempHash.value;
            } else {
                return UnknownValue;
            }
        }
        if (TempHash.bestMove.src == -1) {
            return UnknownValue;
        } else {
            HashMove = TempHash.bestMove;
            return ObsoleteValue;
        }
    }
    return UnknownValue;
}

private void recordHash(MoveNode hashMove, int hashFlag, int value, int depth) {
    HashRecord tempHash;
    tempHash = hashList[(int) (activeBoard.getZobristKey() & hashMask)];
    if ((tempHash.flag != 0) && tempHash.depth > depth) {
        return;
    }
    tempHash.zobristLock = activeBoard.getZobristLock();
    tempHash.flag = hashFlag;
    tempHash.depth = depth;
    tempHash.value = value;
    if (tempHash.value > CCEvalue.MaxValue - ActiveBoard.MAX_MOVE_NUM / 2) {
        tempHash.value += activeBoard.getMoveNum() - startMove;
    } else if (tempHash.value < ActiveBoard.MAX_MOVE_NUM / 2 - CCEvalue.MaxValue) {
        tempHash.value -= activeBoard.getMoveNum() - startMove;
    }
    tempHash.bestMove = hashMove;
    hashList[(int) (activeBoard.getZobristKey() & hashMask)] = tempHash;
}

private void GetPvLine() {
    HashRecord tempHash;
    tempHash = hashList[(int) (activeBoard.getZobristKey() & hashMask)];
    if ((tempHash.flag != 0) && tempHash.bestMove.src != -1
            && tempHash.zobristLock == activeBoard.getZobristLock()) {
        pvLine[pvLineNum] = tempHash.bestMove;
        activeBoard.movePiece(tempHash.bestMove);
        pvLineNum++;
        if (activeBoard.isLoop(1) == 0) {// ???????
            GetPvLine();
        }
        activeBoard.undoMove();
    }
}

// record example: i0h0 4
// rnbakabr1/9/4c1c1n/p1p1N3p/9/6p2/P1P1P3P/2N1C2C1/9/R1BAKAB1R w - - 0 7
// i0h0:Move , 4: evalue, other: FEN String

public void loadBook(final String bookFile) throws IOException {// 开局库
    int bookMoveNum;
    String lineStr;
    // LineStr;
    int index = 0;
    MoveNode bookMove;// note:wrong
    HashRecord tempHash;
    ActiveBoard BookPos = new ActiveBoard();// note:wrong
    ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(Paths.get(bookFile)));
    zipIn.getNextEntry();
    BufferedReader inFile = new BufferedReader(new InputStreamReader(zipIn));
    bookPosNum = 0;
    int recordedToHash = 0;// for test
    int cnt = 0;
    while ((lineStr = inFile.readLine()) != null) {
        cnt++;
        bookMove = new MoveNode();
        bookMove.move(lineStr);
        index = 0;
        if (bookMove.src != -1) {
            index += 5;
            while (lineStr.charAt(index) == ' ') {
                index++;
            }
            BookPos.loadFen(lineStr.substring(index));
            long tmpZob = BookPos.getZobristKey();
            int tmp = BookPos.getSquares(bookMove.src);// for test
            if (BookPos.getSquares(bookMove.src) != 0) {
                tempHash = hashList[(int) (BookPos.getZobristKey() & hashMask)];
                if (tempHash.flag != 0) {// 占用
                    if (tempHash.zobristLock == BookPos.getZobristLock()) {// 局面相同
                        if ((tempHash.flag & BookMulti) != 0) {// 多个相同走法
                            bookMoveNum = bookList[tempHash.value].moveNum;
                            if (bookMoveNum < MaxBookMove) {
                                bookList[tempHash.value].moveList[bookMoveNum] = bookMove;
                                bookList[tempHash.value].moveNum++;
                                recordedToHash++;// for test
                            }
                        } else {
                            if (bookPosNum < maxBookPos) {
                                tempHash.flag = BookMulti;
                                bookList[bookPosNum] = new BookRecord();
                                bookList[bookPosNum].moveNum = 2;
                                bookList[bookPosNum].moveList[0] = tempHash.bestMove;
                                bookList[bookPosNum].moveList[1] = bookMove;
                                tempHash.value = bookPosNum;
                                bookPosNum++;
                                hashList[(int) (BookPos.getZobristKey() & hashMask)] = tempHash;
                                recordedToHash++;// for test
                            }
                        }
                    }
                } else {
                    tempHash.zobristLock = BookPos.getZobristLock();
                    tempHash.flag = BookUnique;
                    tempHash.depth = 0;
                    tempHash.value = 0;
                    tempHash.bestMove = bookMove;
                    hashList[(int) (BookPos.getZobristKey() & hashMask)] = tempHash;
                    recordedToHash++;
                }
            }
        }
    }
    System.out.println("ai0 加载开局库 " + cnt + " 个");
    inFile.close();
}

// End History and Hash Tables Procedures

// Begin Search Procedures
// Search Procedures
private int RAdapt(int depth) {
    // 根据不同情况来调整R值的做法,称为“适应性空着裁剪”(Adaptive Null-Move Pruning)，
    // 它首先由Ernst Heinz发表在1999年的ICCA杂志上。其内容可以概括为
    // a. 深度小于或等于6时，用R = 2的空着裁剪进行搜索
    // b. 深度大于8时，用R = 3；
    // c. 深度是6或7时，如果每方棋子都大于或等于3个,则用 R = 3，否则用 R = 2。

    if (depth <= 6) {
        return 2;
    } else if (depth <= 8) {
        return activeBoard.getEvalue(0) < CCEvalue.EndgameMargin
                || activeBoard.getEvalue(1) < CCEvalue.EndgameMargin ? 2 : 3;
    } else {
        return 3;
    }
}

private int quiesc(int Alpha, int Beta) {// 只对吃子
    int i, bestValue, thisAlpha, thisValue;
    boolean inCheck, movable;
    MoveNode thisMove;
    SortedMoveNodes moveSort = new SortedMoveNodes();

    // 1. Return if a Loop position is detected
    if (activeBoard.getMoveNum() > startMove) {
        thisValue = activeBoard.isLoop(1);// note:wrong
        if (thisValue != 0) {
            return activeBoard.loopValue(thisValue, activeBoard.getMoveNum() - startMove);
        }
    }

    // 2. Initialize
    inCheck = activeBoard.lastMove().chk;
    movable = false;
    bestValue = -CCEvalue.MaxValue;
    thisAlpha = Alpha;

    // 3. For non-check position, try Null-Move before generate moves
    if (!inCheck) {
        movable = true;
        thisValue = activeBoard.evaluation()
                + (selectMask != 0 ? (rand.nextInt() & selectMask) - (rand.nextInt() & selectMask) : 0);
        if (thisValue > bestValue) {
            if (thisValue >= Beta) {
                quiescNullNodes++;
                return thisValue;
            }
            bestValue = thisValue;
            if (thisValue > thisAlpha) {
                thisAlpha = thisValue;
            }
        }
    }
    // 4. Generate and sort all moves for check position, or capture moves
    // for non-check position
    moveSort.GenMoves(activeBoard, inCheck ? histTab : null);
    for (i = 0; i < moveSort.MoveNum; i++) {
        moveSort.BubbleSortMax(i);
        thisMove = moveSort.MoveList[i];
        if (inCheck || activeBoard.narrowCap(thisMove, wideQuiesc)) {
            if (activeBoard.movePiece(thisMove)) {
                movable = true;

                // 5. Call Quiescence Alpha-Beta Search for every leagal
                // moves
                thisValue = -quiesc(-Beta, -thisAlpha);
                // for debug
                String tmpStr = "";
                for (int k = 0; k < activeBoard.getMoveNum(); k++) {
                    tmpStr = tmpStr + activeBoard.moveList[k] + ",";
                }

                tmpStr = tmpStr + "Value:" + thisValue + "\n";
                activeBoard.undoMove();

                // 6. Select the best move for Fail-Soft Alpha-Beta
                if (thisValue > bestValue) {
                    if (thisValue >= Beta) {
                        quiescBetaNodes++;
                        return thisValue;
                    }
                    bestValue = thisValue;
                    if (thisValue > thisAlpha) {
                        thisAlpha = thisValue;
                    }
                }
            }
        }
    }

    // 7. Return a loose value if no leagal moves
    if (!movable) {
        quiescMateNodes++;
        return activeBoard.getMoveNum() - startMove - CCEvalue.MaxValue;
    }
    if (thisAlpha > Alpha) {
        quiescPvNodes++;
    } else {
        quiescAlphaNodes++;
    }
    return bestValue;
}
// 搜索算法，包括
// 1. Hash Table;
// 2. 超出边界的Alpha-Beta搜索
// 3. 适应性空着裁减
// 4. 选择性扩展
// 5. 使用Hash Table的迭代加深;
// 6. 杀着表
// 7. 将军扩展
// 8. 主要变例搜索
// 9. 历史启发表

private int search(KillerStruct KillerTab, int Alpha, int Beta, int Depth) {
    int i, j, thisDepth, futPrune, hashFlag;
    boolean inCheck, movable, searched;
    int hashValue, bestValue, thisAlpha, thisValue, futValue = 0;
    MoveNode thisMove = new MoveNode();
    MoveNode bestMove = new MoveNode();
    SortedMoveNodes moveSort = new SortedMoveNodes();
    KillerStruct subKillerTab = new KillerStruct();
    // Alpha-Beta Search:
    // 1. 重复循环检测
    if (activeBoard.getMoveNum() > startMove) {
        thisValue = activeBoard.isLoop(1);//
        if (thisValue != 0) {
            return activeBoard.loopValue(thisValue, activeBoard.getMoveNum() - startMove);
        }
    }

    // 2. 是否需要扩展
    inCheck = activeBoard.lastMove().chk;
    thisDepth = Depth;
    if (inCheck) {
        thisDepth++;
    }

    // 3. Return if hit the Hash Table
    hashValue = probeHash(thisMove, Alpha, Beta, thisDepth);
    if (hashValue >= -CCEvalue.MaxValue && hashValue <= CCEvalue.MaxValue) {
        return hashValue;
    }

    // 4. Return if interrupted or timeout
    if (interrupt()) {
        return 0;
    }
    ;

    // 5. 正式开始搜索：
    if (thisDepth > 0) {
        movable = false;
        searched = false;
        bestValue = -CCEvalue.MaxValue;
        thisAlpha = Alpha;
        hashFlag = HashAlpha;
        subKillerTab.moveNum = 0;

        // 6. 是否需要空着裁减与冒进？
        futPrune = 0;
        if (futility) {
            // 冒进
            if (thisDepth == 3 && !inCheck && activeBoard.evaluation() + CCEvalue.RazorMargin <= Alpha
                    && activeBoard.getEvalue(activeBoard.getOppPlayer()) > CCEvalue.EndgameMargin) {
                thisDepth = 2;
            }
            if (thisDepth < 3) {
                futValue = activeBoard.evaluation()
                        + (thisDepth == 2 ? CCEvalue.ExtFutMargin : CCEvalue.SelFutMargin);
                if (!inCheck && futValue <= Alpha) {
                    futPrune = thisDepth;
                    bestValue = futValue;
                }
            }
        }

        // 7. 空着裁减
        if (nullMove && futPrune == 0 && !inCheck && activeBoard.lastMove().src != -1
                && activeBoard.getEvalue(activeBoard.getPlayer()) > CCEvalue.EndgameMargin) {
            activeBoard.nullMove();
            thisValue = -search(subKillerTab, -Beta, 1 - Beta, thisDepth - 1 - RAdapt(thisDepth));
            activeBoard.undoNull();
            if (thisValue >= Beta) {
                nullNodes++;
                return Beta;
            }
        }

        // 8. 搜索命中Hash Table
        if (hashValue == ObsoleteValue) {
            // System.out.println(ThisMove.Coord());
            if (activeBoard.movePiece(thisMove)) {
                movable = true;
                if (futPrune != 0
                        && -activeBoard.evaluation()
                        + (futPrune == 2 ? CCEvalue.ExtFutMargin : CCEvalue.SelFutMargin) <= Alpha
                        && activeBoard.lastMove().chk) {
                    activeBoard.undoMove();
                } else {
                    thisValue = -search(subKillerTab, -Beta, -thisAlpha, thisDepth - 1);
                    searched = true;
                    activeBoard.undoMove();
                    if (stop) {
                        return 0;
                    }
                    if (thisValue > bestValue) {
                        if (thisValue >= Beta) {
                            histTab[thisMove.src][thisMove.dst] += 1 << (thisDepth - 1);
                            recordHash(thisMove, HashBeta, Beta, thisDepth);
                            hashNodes++;
                            return thisValue;
                        }
                        bestValue = thisValue;
                        bestMove = thisMove;
                        if (thisValue > thisAlpha) {
                            thisAlpha = thisValue;
                            hashFlag = HashPv;
                            if (activeBoard.getMoveNum() == startMove) {
                                recordHash(bestMove, hashFlag, thisAlpha, thisDepth);
                                popInfo(thisAlpha, Depth);
                            }
                        }
                    }
                }
            }
        }

        // 9. 命中杀着表
        for (i = 0; i < KillerTab.moveNum; i++) {
            thisMove = KillerTab.moveList[i];
            if (activeBoard.legalMove(thisMove)) {
                if (activeBoard.movePiece(thisMove)) {
                    movable = true;
                    if (futPrune != 0
                            && -activeBoard.evaluation()
                            + (futPrune == 2 ? CCEvalue.ExtFutMargin : CCEvalue.SelFutMargin) <= Alpha
                            && activeBoard.lastMove().chk) {
                        activeBoard.undoMove();
                    } else {
                        if (searched) {
                            thisValue = -search(subKillerTab, -thisAlpha - 1, -thisAlpha, thisDepth - 1);
                            if (thisValue > thisAlpha && thisValue < Beta) {
                                thisValue = -search(subKillerTab, -Beta, -thisAlpha, thisDepth - 1);
                            }
                        } else {
                            thisValue = -search(subKillerTab, -Beta, -thisAlpha, thisDepth - 1);
                            searched = true;
                        }
                        activeBoard.undoMove();
                        if (stop) {
                            return 0;
                        }
                        if (thisValue > bestValue) {
                            if (thisValue >= Beta) {
                                killerNodes++;
                                histTab[thisMove.src][thisMove.dst] += 1 << (thisDepth - 1);
                                recordHash(thisMove, HashBeta, Beta, thisDepth);
                                return thisValue;
                            }
                            bestValue = thisValue;
                            bestMove = thisMove;
                            if (thisValue > thisAlpha) {
                                thisAlpha = thisValue;
                                hashFlag = HashPv;
                                if (activeBoard.getMoveNum() == startMove) {
                                    recordHash(bestMove, hashFlag, thisAlpha, thisDepth);
                                    popInfo(thisAlpha, Depth);
                                }
                            }
                        }
                    }
                }
            }
        }

        // 10. 生成当前所有合法着法并排序
        moveSort.GenMoves(activeBoard, histTab);
        nodes += moveSort.MoveNum;
        for (i = 0; i < moveSort.MoveNum; i++) {
            moveSort.BubbleSortMax(i);
            thisMove = moveSort.MoveList[i];

            if (activeBoard.movePiece(thisMove)) {
                movable = true;
                // 11. Alpha-Beta Search
                if (futPrune != 0
                        && -activeBoard.evaluation()
                        + (futPrune == 2 ? CCEvalue.ExtFutMargin : CCEvalue.SelFutMargin) <= Alpha
                        && activeBoard.lastMove().chk) {
                    activeBoard.undoMove();
                } else {
                    if (searched) {
                        thisValue = -search(subKillerTab, -thisAlpha - 1, -thisAlpha, thisDepth - 1);
                        if (thisValue > thisAlpha && thisValue < Beta) {
                            thisValue = -search(subKillerTab, -Beta, -thisAlpha, thisDepth - 1);
                        }
                    } else {
                        thisValue = -search(subKillerTab, -Beta, -thisAlpha, thisDepth - 1);
                        searched = true;
                    }
                    activeBoard.undoMove();
                    if (stop) {
                        return 0;
                    }
                    // 12. 超出边界Alpha-Beta
                    if (thisValue > bestValue) {
                        if (thisValue >= Beta) {
                            betaNodes++;
                            histTab[thisMove.src][thisMove.dst] += 1 << (thisDepth - 1);
                            recordHash(thisMove, HashBeta, Beta, thisDepth);
                            if (KillerTab.moveNum < MaxKiller) {
                                KillerTab.moveList[KillerTab.moveNum] = thisMove;
                                KillerTab.moveNum++;
                            }
                            return thisValue;
                        }
                        bestValue = thisValue;
                        bestMove = thisMove;
                        if (thisValue > thisAlpha) {
                            thisAlpha = thisValue;
                            hashFlag = HashPv;
                            if (activeBoard.getMoveNum() == startMove) {
                                recordHash(bestMove, hashFlag, thisAlpha, thisDepth);
                                popInfo(thisAlpha, Depth);
                            }
                        }
                    }
                }
            }
        }
        // 13.无路可走，输棋！
        if (!movable) {
            mateNodes++;
            return activeBoard.getMoveNum() - startMove - CCEvalue.MaxValue;
        }

        // 14. Update History Tables and Hash Tables
        if (futPrune != 0 && bestValue == futValue) {
            bestMove.src = bestMove.dst = -1;
        }
        if ((hashFlag & HashAlpha) != 0) {
            alphaNodes++;
        } else {
            pvNodes++;
            histTab[bestMove.src][bestMove.dst] += 1 << (thisDepth - 1);
            if (KillerTab.moveNum < MaxKiller) {
                KillerTab.moveList[KillerTab.moveNum] = bestMove;
                KillerTab.moveNum++;
            }
        }
        recordHash(bestMove, hashFlag, thisAlpha, thisDepth);
        return bestValue;

        // 15. 静态搜索
    } else {
        thisValue = quiesc(Alpha, Beta);
        thisMove.src = bestMove.dst = -1;
        if (thisValue <= Alpha) {
            recordHash(thisMove, HashAlpha, Alpha, 0);
        } else if (thisValue >= Beta) {
            recordHash(thisMove, HashBeta, Beta, 0);
        } else {
            recordHash(thisMove, HashPv, thisValue, 0);
        }
        leafNodes++;
        return thisValue;
    }
}

// End Search Procedures
// Start Control Procedures
private boolean interrupt() {
    if (stop)
        return true;
    return false;
}

public void stopSearch() {
    this.stop = true;
}

private void popInfo(int value, int depth) {
    int i, quiescNodes, nps, npsQuiesc;
    char[] moveStr;
    long tempLong;
    if (depth != 0) {
        String logString = "PVNode:  depth=" + depth + ",score=" + value + ",Move: " + "\n";
        pvLineNum = 0;
        GetPvLine();
        for (i = 0; i < pvLineNum; i++) {
            moveStr = pvLine[i].location();
            logString += " " + String.copyValueOf(moveStr) + "\n";
        }
        if (ponder && System.currentTimeMillis() > minTimer && value + CCEvalue.InadequateValue > lastScore) {
            stop = true;
        }
    }
}

public void setupControl(int depth, long proper, long limit) {
    this.depth = depth;
    this.properTimer = proper;
    this.limitTimer = limit;
}

public void control() throws Exception {
    // int Depth, int ProperTimer, int LimitTimer) throws IOException {
    int i, MoveNum, ThisValue;
    char[] MoveStr;
    stop = false;
    bestMove = null;
    MoveNode ThisMove = new MoveNode(), UniqueMove = new MoveNode();
    HashRecord TempHash;
    SortedMoveNodes MoveSort = new SortedMoveNodes();
    KillerStruct SubKillerTab = new KillerStruct();
    // The Computer Thinking Procedure：
    // 1. Search the moveNodes in Book
    int tmpInt = (int) (activeBoard.getZobristKey() & hashMask);
    TempHash = hashList[(int) (activeBoard.getZobristKey() & hashMask)];
    if (TempHash.flag != 0 && TempHash.zobristLock == activeBoard.getZobristLock()) {
        if ((TempHash.flag == BookUnique)) {
            MoveStr = TempHash.bestMove.location();
            bestMove = new MoveNode(String.copyValueOf(MoveStr));
            return;
        } else if (TempHash.flag == BookMulti) {
            ThisValue = 0;
            i = Math.abs(rand.nextInt()) % (bookList[TempHash.value].moveNum);
            MoveStr = bookList[TempHash.value].moveList[i].location();
            bestMove = new MoveNode(String.copyValueOf(MoveStr));
            return;
        }
    }

    // 2. Initailize Timer and other Counter
    startTimer = System.currentTimeMillis();
    minTimer = startTimer + (properTimer >> 1);
    maxTimer = properTimer << 1;
    if (maxTimer > limitTimer) {
        maxTimer = limitTimer;
    }
    maxTimer += startTimer;
    stop = false;
    startMove = activeBoard.getMoveNum();
    nodes = nullNodes = hashNodes = killerNodes = betaNodes = pvNodes = alphaNodes = mateNodes = leafNodes = 0;
    quiescNullNodes = quiescBetaNodes = quiescPvNodes = quiescAlphaNodes = quiescMateNodes = 0;
    hitBeta = hitPv = hitAlpha = 0;
    pvLineNum = 0;

    // 3. 不合法：主动送将
    if (activeBoard.checked(activeBoard.getOppPlayer())) {
        return;
    }
    ThisValue = activeBoard.isLoop(3);
    if (ThisValue != 0) {
        throw new Exception("不可常捉!");
    }
    if (activeBoard.getMoveNum() > ActiveBoard.MAX_CONSECUTIVE_MOVES) {
        throw new Exception("最大步数,和棋!");
    }

    // 4. 测试所有应将的着法
    if (activeBoard.lastMove().chk) {
        MoveNum = 0;
        MoveSort.GenMoves(activeBoard, histTab);
        for (i = 0; i < MoveSort.MoveNum; i++) {
            ThisMove = MoveSort.MoveList[i];
            if (activeBoard.movePiece(ThisMove)) {
                activeBoard.undoMove();
                UniqueMove = ThisMove;
                MoveNum++;
                if (MoveNum > 1) {
                    break;
                }
            }
        }
        if (MoveNum == 0) {
            System.out.println("score " + -CCEvalue.MaxValue + "\n");
        }
        if (MoveNum == 1) {
            MoveStr = UniqueMove.location();
            System.out.println("bestmove " + String.copyValueOf(MoveStr) + "\n");
            bestMove = new MoveNode(String.copyValueOf(MoveStr));
            return;
        }
    }

    // 5. 迭代加深
    if (depth == 0) {
        return;
    }
    for (i = 4; i <= depth; i++) {
        SubKillerTab.moveNum = 0;
        ThisValue = search(SubKillerTab, -CCEvalue.MaxValue, CCEvalue.MaxValue, i);
        popInfo(ThisValue, depth);
        if (stop) {
            break;
        }
        lastScore = ThisValue;

        // 6. Stop thinking if timeout or solved
        if (!ponder && System.currentTimeMillis() > minTimer) {
            break;
        }
        if (ThisValue > CCEvalue.MaxValue - ActiveBoard.MAX_MOVE_NUM / 2
                || ThisValue < ActiveBoard.MAX_MOVE_NUM / 2 - CCEvalue.MaxValue) {
            break;
        }
    }

    // 7. 得到最佳着法及其线路
    if (pvLineNum != 0) {
        MoveStr = pvLine[0].location();
        bestMove = new MoveNode(String.copyValueOf(MoveStr));
        System.out.println("bestmove: " + String.copyValueOf(MoveStr) + "\n");
        if (pvLineNum > 1) {
            MoveStr = pvLine[1].location();
            System.out.println("ponder:" + String.copyValueOf(MoveStr) + "\n");
        }
    } else {
        System.out.println("score:" + ThisValue);
    }
}

// End Control Procedures

public MoveNode getBestMove() throws Exception {
    control();
    return bestMove;
}

// for test
public static void main(String[] args) throws IOException {
    long start, end;
    RandomAccessFile testResult;
    System.out.println("begin search, please wait......");
    start = System.currentTimeMillis();
    int steps = 8;
    ActiveBoard cp = new ActiveBoard();
    String FenStr = "1c1k1abR1/4a4/4b4/6NP1/4P4/2C1n1P2/r5p2/4B4/4A4/2BAK4 w - - 0 20";
    cp.loadFen(FenStr);
    SearchEngine searchMove = new SearchEngine(cp);
    searchMove.loadBook("./data/book.zip");
    // System.out.println(cp.AllPieces);
    // searchMove.Control(steps,CLOCK_M*2,CLOCK_M*4);
    System.out.println(FenStr);
    end = System.currentTimeMillis();
    long second = (end - start) / 1000;
    long minutes = second / 60;
    testResult = new RandomAccessFile("./data/test.log", "rw");
    Calendar c = Calendar.getInstance();
    String tmpStr = "\n\n********************************************************************\n";
    tmpStr = tmpStr + "[Test Time] " + c.getTime() + "\n";
    tmpStr = tmpStr + "[Fen String] " + FenStr + "\n";
    tmpStr = tmpStr + "   Deep =" + steps + ",Used Time:" + minutes + ":" + second % 60 + "\n";
    tmpStr = tmpStr + "[Nodes] " + searchMove.nodes + "\n";
    tmpStr = tmpStr + "[AlphaNodes] " + searchMove.alphaNodes + "\n";
    tmpStr = tmpStr + "[BetaNodes] " + searchMove.betaNodes + "\n";
    tmpStr = tmpStr + "[HashNodes] " + searchMove.hashNodes + "\n";
    tmpStr = tmpStr + "[KillerNodes] " + searchMove.killerNodes + "\n";
    tmpStr = tmpStr + "[LeafNodes] " + searchMove.leafNodes + "\n";
    tmpStr = tmpStr + "[NullNodes] " + searchMove.nullNodes + "\n";
    tmpStr = tmpStr + "[QuiescAlphaNodes] " + searchMove.quiescAlphaNodes + "\n";
    tmpStr = tmpStr + "[QuiescBetaNodesNodes] " + searchMove.quiescBetaNodes + "\n";
    tmpStr = tmpStr + "[QuiescMateNodes] " + searchMove.quiescMateNodes + "\n";
    tmpStr = tmpStr + "[QuiescNullNodes] " + searchMove.quiescNullNodes + "\n";
    tmpStr = tmpStr + "[QuiescPvNodes] " + searchMove.quiescPvNodes + "\n";
    tmpStr = tmpStr + "[HitAlpha] " + searchMove.hitAlpha + "\n";
    tmpStr = tmpStr + "[HitBeta] " + searchMove.hitBeta + "\n";
    tmpStr = tmpStr + "[HitPv] " + searchMove.hitPv + "\n";

    tmpStr = tmpStr + "[BetaNode] " + searchMove.betaNodes + "\n";
    tmpStr = tmpStr + "[BPS] " + searchMove.nodes / (1 + second);
    int count = 0;
    for (int i = 1; i < searchMove.hashList.length; i++) {
        if (searchMove.hashList[i].flag != 0)
            count++;
    }
    tmpStr = tmpStr + "[HashTable] length=" + searchMove.hashList.length + ", occupied=" + count;
    testResult.seek(testResult.length());
    testResult.writeBytes(tmpStr);

    testResult.close();
    System.out.println(tmpStr);
    searchMove = null;
    cp = null;
    System.gc();
}

class BookRecord {
    int moveNum;
    MoveNode[] moveList;// [MaxBookMove];

    public BookRecord() {
        moveList = new MoveNode[SearchEngine.MaxBookMove];
        moveNum = 0;
    }
}

class KillerStruct {
    int moveNum;
    MoveNode[] moveList;// [MaxKiller];

    public KillerStruct() {
        moveList = new MoveNode[SearchEngine.MaxKiller];
        for (int i = 0; i < SearchEngine.MaxKiller; i++)
            moveList[i] = new MoveNode();
        moveNum = 0;
    }
}

class HashRecord {
    public HashRecord() {
        flag = 0;
        depth = 0;
        value = 0;
        zobristLock = 0;
        bestMove = new MoveNode();
    }

    long zobristLock;
    int flag, depth;
    int value;
    MoveNode bestMove;
}

}
