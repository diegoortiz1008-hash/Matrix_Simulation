package matrix;

import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Game {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== MATRIX ===");
        System.out.println("Neo busca el telefono. Los agentes persiguen a Neo.");
        System.out.println();

        int size = readInt(scanner, "Tamano del tablero (minimo 5): ", 5, 50);
        int max  = Math.max(1, (int)(size * size * 0.3));
        int numAgents = readInt(scanner, "Numero de agentes (1-" + max + "): ", 1, max);
        int numPhones = readInt(scanner, "Numero de telefonos (1-" + max + "): ", 1, max);

        Board board = new Board(size, numAgents, numPhones);

        // parties = Neo(1) + agentes(N) + main(1)
        int parties = 1 + numAgents + 1;
        CyclicBarrier computeBarrier = new CyclicBarrier(parties);
        CyclicBarrier applyBarrier   = new CyclicBarrier(parties);

        NeoThread neo = new NeoThread(board, computeBarrier, applyBarrier);
        neo.setDaemon(true);
        neo.start();

        for (int i = 0; i < numAgents; i++) {
            AgentThread agent = new AgentThread(board, i, computeBarrier, applyBarrier);
            agent.setDaemon(true);
            agent.start();
        }

        board.print();

        while (!board.isGameOver()) {
            scanner.nextLine();
            if (board.isGameOver()) break;

            board.incrementTurn();

            try { computeBarrier.await(); } catch (BrokenBarrierException e) { break; }
            try { applyBarrier.await();   } catch (BrokenBarrierException e) { break; }

            board.print();
        }

        Thread.sleep(100);
        System.out.println();
        if (board.isNeoWins())
            System.out.println("Neo alcanzo el telefono. Neo gana.");
        else
            System.out.println("Los agentes atraparon a Neo. Agentes ganan.");

        scanner.close();
        System.exit(0);
    }

    private static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v >= min && v <= max) return v;
                System.out.println("Ingresa un numero entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida.");
            }
        }
    }
}
