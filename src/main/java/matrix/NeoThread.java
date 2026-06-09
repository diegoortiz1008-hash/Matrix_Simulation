package matrix;

import java.util.concurrent.CyclicBarrier;

public class NeoThread extends Thread {

    private final Board board;
    private final CyclicBarrier computeBarrier;
    private final CyclicBarrier applyBarrier;

    private int[] nextStep = null;

    public NeoThread(Board board, CyclicBarrier computeBarrier, CyclicBarrier applyBarrier) {
        super("Neo");
        this.board          = board;
        this.computeBarrier = computeBarrier;
        this.applyBarrier   = applyBarrier;
    }

    @Override
    public void run() {
        while (!board.isGameOver()) {
            // 1. Compute next step via BFS toward nearest phone
            nextStep = board.bfsNextStep(board.getNeoPos(), Board.PHONE);

            // 2. Wait for all threads + main to sync (end of compute phase)
            try { computeBarrier.await(); }
            catch (Exception e) { Thread.currentThread().interrupt(); return; }

            // 3. Apply move (main waits for Enter before releasing applyBarrier)
            if (!board.isGameOver()) {
                board.moveNeo(nextStep);
            }

            // 4. Signal done applying
            try { applyBarrier.await(); }
            catch (Exception e) { Thread.currentThread().interrupt(); return; }
        }
    }
}
