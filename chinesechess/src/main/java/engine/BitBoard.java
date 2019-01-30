package engine;

/**
 * 位棋盘：使用三个int来表示
 * 96个bit的棋盘
 * <p>
 * 需要实现一些位运算
 * and,or,xor,not
 * 赋值位运算符，assignAnd，assignOr，assignXor，assignNot
 */
public class BitBoard {
int Low, Mid, Hi;

BitBoard(int low, int mid, int hi) {
    Low = low;
    Mid = mid;
    Hi = hi;
}

boolean notZero() {
    return (Low | Mid | Hi) != 0;
}

boolean equals(BitBoard board) {
    return ((Low == board.Low) && (Mid == board.Mid) && (Hi == board.Hi));
}

boolean notEquals(BitBoard board) {
    return Low != board.Low || Mid != board.Mid || Hi != board.Hi;
}

BitBoard opNot() {
    return new BitBoard(~Low, ~Mid, ~Hi);
}

BitBoard opAnd(BitBoard Arg) {
    return new BitBoard(Low & Arg.Low, Mid & Arg.Mid, Hi & Arg.Hi);
}

BitBoard opOr(BitBoard Arg) {
    return new BitBoard(Low | Arg.Low, Mid | Arg.Mid, Hi | Arg.Hi);
}

BitBoard opXor(BitBoard Arg) {
    return new BitBoard(Low ^ Arg.Low, Mid ^ Arg.Mid, Hi ^ Arg.Hi);
}

void assignAnd(BitBoard Arg) {
    Low &= Arg.Low;
    Mid &= Arg.Mid;
    Hi &= Arg.Hi;
}

void assignOr(final BitBoard Arg) {
    Low |= Arg.Low;
    Mid |= Arg.Mid;
    Hi |= Arg.Hi;
}

void assignXor(final BitBoard Arg) {
    Low ^= Arg.Low;
    Mid ^= Arg.Mid;
    Hi ^= Arg.Hi;
}

void setBitBoard(int Arg1, int Arg2, int Arg3) {
    Low = Arg1;
    Mid = Arg2;
    Hi = Arg3;
}

BitBoard getLeftShift(int Arg) {
    if (Arg < 0) {
        return getRightShift(-Arg);
    } else if (Arg == 0) {
        return new BitBoard(Low, Mid, Hi);
    } else if (Arg < 32) {
        return new BitBoard(Low << Arg, Mid << Arg | Low >>> (32 - Arg), Hi << Arg | Mid >>> (32 - Arg));
    } else if (Arg == 32) {
        return new BitBoard(0, Low, Mid);
    } else if (Arg < 64) {
        return new BitBoard(0, Low << (Arg - 32), Mid << (Arg - 32) | Low >>> (64 - Arg));
    } else if (Arg == 64) {
        return new BitBoard(0, 0, Low);
    } else if (Arg < 96) {
        return new BitBoard(0, 0, Low << (Arg - 64));
    } else {
        return new BitBoard(0, 0, 0);
    }
}

BitBoard getRightShift(int Arg) {
    if (Arg < 0) {
        return getLeftShift(-Arg);
    } else if (Arg == 0) {
        return new BitBoard(Low, Mid, Hi);
    } else if (Arg < 32) {
        return new BitBoard(Low >> Arg | Mid << (32 - Arg), Mid >> Arg | Hi << (32 - Arg), Hi >> Arg);
    } else if (Arg == 32) {
        return new BitBoard(Mid, Hi, 0);
    } else if (Arg < 64) {
        return new BitBoard(Mid >> (Arg - 32) | Hi << (64 - Arg), Hi >> (Arg - 32), 0);
    } else if (Arg == 64) {
        return new BitBoard(Hi, 0, 0);
    } else if (Arg < 96) {
        return new BitBoard(Hi >> (Arg - 64), 0, 0);
    } else {
        return new BitBoard(0, 0, 0);
    }
}

//左移操作
void leftShift(int Arg) {
    if (Arg < 0) {
        rightShift(-Arg);
    } else if (Arg == 0) {
    } else if (Arg < 32) {
        Hi <<= Arg;
        Hi |= Mid >>> (32 - Arg);
        Mid <<= Arg;
        Mid |= Low >>> (32 - Arg);
        Low <<= Arg;
    } else if (Arg == 32) {
        Hi = Mid;
        Mid = Low;
        Low = 0;
    } else if (Arg < 64) {
        Hi = Mid << (Arg - 32);
        Hi |= Low >>> (64 - Arg);
        Mid = Low << (Arg - 32);
        Low = 0;
    } else if (Arg == 64) {
        Hi = Low;
        Mid = 0;
        Low = 0;
    } else if (Arg < 96) {
        Hi = Low << (Arg - 64);
        Mid = 0;
        Low = 0;
    } else {
        Low = 0;
        Mid = 0;
        Hi = 0;
    }
}

//右移操作
void rightShift(int Arg) {
    if (Arg < 0) {
        leftShift(-Arg);
    } else if (Arg == 0) {
    } else if (Arg < 32) {
        Low >>>= Arg;
        Low |= Mid << (32 - Arg);
        Mid >>>= Arg;
        Mid |= Hi << (32 - Arg);
        Hi >>>= Arg;
    } else if (Arg == 32) {
        Low = Mid;
        Mid = Hi;
        Hi = 0;
    } else if (Arg < 64) {
        Low = Mid >>> (Arg - 32);
        Low |= Hi << (64 - Arg);
        Mid = Hi >>> (Arg - 32);
        Hi = 0;
    } else if (Arg == 64) {
        Low = Hi;
        Mid = 0;
        Hi = 0;
    } else if (Arg < 96) {
        Low = Hi >>> (Arg - 64);
        Mid = 0;
        Hi = 0;
    } else {
        Low = 0;
        Mid = 0;
        Hi = 0;
    }
}

//校验和，返回8bit异或值
static int CheckSum(final BitBoard board) {
    int temp1 = board.Low ^ board.Mid ^ board.Hi;
    int temp2 = (temp1 & 0xFFFF) ^ (temp1 >>> 16);
    return (temp2 & 0xFF) ^ (temp2 >>> 8);
}

//x是一个字节，重复4次组成一个int，位棋盘高中低三位都是此值
static BitBoard Duplicate(int x) {
    int temp1 = (x & 0xFF);
    int temp2 = temp1 | (temp1 << 8);
    int temp32 = temp2 | (temp2 << 16);
    return new BitBoard(temp32, temp32, temp32);
}

// msb = Most Significant Bit，最高有效位
static int msb32(int board) {
    int RetVal = 0;
    int TempArg = board;
    if ((TempArg & 0xffff0000) != 0) {
        RetVal += 16;
        TempArg &= 0xffff0000;
    }
    if ((TempArg & 0xff00ff00) != 0) {
        RetVal += 8;
        TempArg &= 0xff00ff00;
    }
    if ((TempArg & 0xf0f0f0f0) != 0) {
        RetVal += 4;
        TempArg &= 0xf0f0f0f0;
    }
    if ((TempArg & 0xcccccccc) != 0) {
        RetVal += 2;
        TempArg &= 0xcccccccc;
    }
    if ((TempArg & 0xaaaaaaaa) != 0) {
        RetVal += 1;
    }
    return RetVal;
}

static int msb(BitBoard Arg) {
    if (Arg.Hi != 0) {
        return msb32(Arg.Hi) + 64;
    } else if (Arg.Mid != 0) {
        return msb32(Arg.Mid) + 32;
    } else if (Arg.Low != 0) {
        return msb32(Arg.Low);
    } else {
        return -1;
    }
}

// lsb = Least Significant Bit，求最低有效位
static int lsb32(int board) {
    int RetVal = 31;
    int TempArg = board;
    if ((TempArg & 0x0000ffff) != 0) {
        RetVal -= 16;
        TempArg &= 0x0000ffff;
    }
    if ((TempArg & 0x00ff00ff) != 0) {
        RetVal -= 8;
        TempArg &= 0x00ff00ff;
    }
    if ((TempArg & 0x0f0f0f0f) != 0) {
        RetVal -= 4;
        TempArg &= 0x0f0f0f0f;
    }
    if ((TempArg & 0x33333333) != 0) {
        RetVal -= 2;
        TempArg &= 0x33333333;
    }
    if ((TempArg & 0x55555555) != 0) {
        RetVal -= 1;
    }
    return RetVal;
}

static int lsb(BitBoard board) {
    if (board.Low != 0) {
        return lsb32(board.Low);
    } else if (board.Mid != 0) {
        return lsb32(board.Mid) + 32;
    } else if (board.Hi != 0) {
        return lsb32(board.Hi) + 64;
    } else {
        return -1;
    }
}

/**
 * 统计int里面1的个数
 */
static int count32(int x) {
    int t;
    t = ((x >>> 1) & 0x55555555) + (x & 0x55555555);
    t = ((t >>> 2) & 0x33333333) + (t & 0x33333333);
    t = ((t >>> 4) & 0x0f0f0f0f) + (t & 0x0f0f0f0f);
    t = ((t >>> 8) & 0x00ff00ff) + (t & 0x00ff00ff);
    return (t >>> 16) + (t & 0x0000ffff);
}

static int count(BitBoard board) {
    return count32(board.Low) + count32(board.Mid) + count32(board.Hi);
}
}
