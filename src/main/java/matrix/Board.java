package matrix;

import java.util.*;

public class Board {

    public static final char EMPTY = '.';
    public static final char NEO   = 'N';
    public static final char AGENT = 'A';
    public static final char PHONE = 'T';
    public static final char WALL  = '#';

    private final int size;
    private final char[][] grid;

    private int[] neoPos;
    private final List<int[]> agents;
    private final List<int[]> phones;

    private volatile boolean neoWins  = false;
    private volatile boolean neoLoses = false;
    private int turn = 0;

    public Board(int size, int numAgents, int numPhones) {
        this.size   = size;
        this.grid   = new char[size][size];
        this.agents = new ArrayList<>();
        this.phones = new ArrayList<>();
        initBoard(numAgents, numPhones);
    }

    private void initBoard(int numAgents, int numPhones) {
        Random rnd = new Random();
        for (char[] row : grid) Arrays.fill(row, EMPTY);

        int walls = (int)(size * size * 0.15);
        placeRandom(walls, WALL, rnd, null);

        for (int i = 0; i < numPhones; i++)
            phones.add(placeRandom(1, PHONE, rnd, null)[0]);

        neoPos = placeRandom(1, NEO, rnd, null)[0];

        for (int i = 0; i < numAgents; i++)
            agents.add(placeRandom(1, AGENT, rnd, neoPos)[0]);
    }

    private int[][] placeRandom(int count, char entity, Random rnd, int[] forbidden) {
        int[][] positions = new int[count][2];
        int placed = 0;
        while (placed < count) {
            int r = rnd.nextInt(size);
            int c = rnd.nextInt(size);
            if (grid[r][c] != EMPTY) continue;
            if (forbidden != null && forbidden[0] == r && forbidden[1] == c) continue;
            grid[r][c] = entity;
            positions[placed++] = new int[]{r, c};
        }
        return positions;
    }

    public synchronized void print() {
        System.out.println();
        System.out.println("Turno " + turn);
        System.out.println();

        // Column header
        System.out.print("  ");
        for (int c = 0; c < size; c++) System.out.printf("%2d", c);
        System.out.println();

        for (int r = 0; r < size; r++) {
            System.out.printf("%2d", r);
            for (int c = 0; c < size; c++)
                System.out.print(" " + grid[r][c]);
            System.out.println();
        }

        System.out.println();
        System.out.println("N=Neo  A=Agente  T=Telefono  #=Pared");

        if (!isGameOver())
            System.out.println("Presiona ENTER para continuar...");
    }

    public synchronized int[] bfsNextStep(int[] from, char... targets) {
        Set<Character> targetSet = new HashSet<>();
        for (char t : targets) targetSet.add(t);

        int[][] dir = {{-1,0},{1,0},{0,-1},{0,1}};
        boolean[][] visited = new boolean[size][size];
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{from[0], from[1], -1, -1});
        visited[from[0]][from[1]] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int r = cur[0], c = cur[1], fr = cur[2], fc = cur[3];

            for (int[] d : dir) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nr >= size || nc < 0 || nc >= size) continue;
                if (visited[nr][nc]) continue;
                char cell = grid[nr][nc];
                if (cell == WALL) continue;

                int nfr = (fr == -1) ? nr : fr;
                int nfc = (fc == -1) ? nc : fc;

                if (targetSet.contains(cell)) return new int[]{nfr, nfc};

                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc, nfr, nfc});
            }
        }
        return null;
    }

    public synchronized boolean moveNeo(int[] newPos) {
        if (newPos == null) return false;
        int r = newPos[0], c = newPos[1];
        char dest = grid[r][c];
        if (dest == AGENT) { neoLoses = true; return false; }
        grid[neoPos[0]][neoPos[1]] = EMPTY;
        grid[r][c] = NEO;
        neoPos = newPos;
        if (dest == PHONE) {
            phones.removeIf(p -> p[0] == r && p[1] == c);
            neoWins = true;
        }
        return true;
    }

    public synchronized boolean moveAgent(int idx, int[] newPos) {
        if (newPos == null) return false;
        int[] cur = agents.get(idx);
        int r = newPos[0], c = newPos[1];
        char dest = grid[r][c];
        if (dest == NEO) {
            neoLoses = true;
            grid[cur[0]][cur[1]] = EMPTY;
            grid[r][c] = AGENT;
            agents.set(idx, newPos);
            return false;
        }
        if (dest == PHONE || dest == AGENT) return false;
        grid[cur[0]][cur[1]] = EMPTY;
        grid[r][c] = AGENT;
        agents.set(idx, newPos);
        return true;
    }

    public int getSize()            { return size; }
    public int[] getNeoPos()        { return neoPos.clone(); }
    public int getAgentCount()      { return agents.size(); }
    public int[] getAgentPos(int i) { return agents.get(i).clone(); }
    public boolean isNeoWins()      { return neoWins; }
    public boolean isNeoLoses()     { return neoLoses; }
    public boolean isGameOver()     { return neoWins || neoLoses; }
    public void incrementTurn()     { turn++; }
}
