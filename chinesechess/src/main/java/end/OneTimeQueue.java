package end;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 只进一次的队列
 */
public class OneTimeQueue {
Queue<Integer> q = new LinkedList<>();
boolean visited[];

OneTimeQueue(int count) {
    visited = new boolean[count];
}

void add(int x) {
    if (visited[x]) return;
    visited[x] = true;
    q.add(x);
}

int poll() {
    return q.poll();
}

boolean isEmpty() {
    return q.isEmpty();
}
}
