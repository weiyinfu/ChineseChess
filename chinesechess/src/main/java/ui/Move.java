package ui;


public class Move {
int fx, fy, tx, ty;
byte eat;

Move(int fx, int fy, int tx, int ty) {
    this.fx = fx;
    this.fy = fy;
    this.tx = tx;
    this.ty = ty;
}

Move(int fx, int fy, int tx, int ty, byte eat) {
    this.fx = fx;
    this.fy = fy;
    this.tx = tx;
    this.ty = ty;
    this.eat = eat;
}

}