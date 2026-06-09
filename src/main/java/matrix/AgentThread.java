package matrix;

import java.util.concurrent.CyclicBarrier;

public class AgentThread extends Thread {

    private final Board board;
    private final int agentIndex;
    private final CyclicBarrier computeBarrier;
    private final CyclicBarrier applyBarrier;

    private int[] nextStep = null;

    public AgentThread(Board board, int agentIndex,
                       CyclicBarrier computeBarrier, CyclicBarrier applyBarrier) {
        super("Agente-" + agentIndex);
        this.board          = board;
        this.agentIndex     = agentIndex;
        this.computeBarrier = computeBarrier;
        this.applyBarrier   = applyBarrier;
    }

    @Override
    public void run() {
        while (!board.isGameOver()) {
            // 1. Compute next step: BFS toward Neo
            nextStep = board.bfsNextStep(board.getAgentPos(agentIndex), Board.NEO);

            // 2. Sync with compute barrier
            try { computeBarrier.await(); }
            catch (Exception e) { Thread.currentThread().interrupt(); return; }

            // 3. Apply move
            if (!board.isGameOver()) {
                board.moveAgent(agentIndex, nextStep);
            }

            // 4. Sync with apply barrier
            try { applyBarrier.await(); }
            catch (Exception e) { Thread.currentThread().interrupt(); return; }
        }
    }
}
