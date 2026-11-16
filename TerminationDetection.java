import java.util.*;

public class TerminationDetection {
    static int n;
    static boolean[] active;
    static int[] parent;
    static int[] deficit;

    // Send a computation message from sender -> receiver
    static void sendMessage(int sender, int receiver) {
        if (!active[sender]) {
            System.out.println("Process " + sender + " is passive and cannot send messages.");
            return;
        }
        deficit[sender]++;
        System.out.println("MSG: Process " + sender + " -> Process " + receiver + " (P" + sender + " deficit=" + deficit[sender] + ")");

        if (!active[receiver]) {
            active[receiver] = true;
            parent[receiver] = sender; // Join spanning tree under sender
            System.out.println("   Process " + receiver + " becomes ACTIVE (Parent = P" + sender + ")");
        } else {
            // Already active: immediately acknowledge so it doesn't alter the tree
            deficit[sender]--;
            System.out.println("   Process " + receiver + " already active -> immediate ACK to P" + sender + " (P" + sender + " deficit=" + deficit[sender] + ")");
        }
    }

    // Mark a process as having finished its local computation
    static void finishWork(int p) {
        if (!active[p]) {
            System.out.println("Process " + p + " is already passive.");
            return;
        }
        if (deficit[p] > 0) {
            System.out.println("Process " + p + " finished local work, waiting for " + deficit[p] + " ACK(s)...");
            return;
        }

        // Local work done AND deficit == 0 -> become passive and notify parent
        active[p] = false;
        System.out.println("DONE: Process " + p + " becomes PASSIVE.");

        if (parent[p] != -1) {
            int par = parent[p];
            parent[p] = -1;
            deficit[par]--;
            System.out.println("   ACK: Process " + p + " -> Parent P" + par + " (P" + par + " deficit=" + deficit[par] + ")");
            // Recursively check if parent can now terminate
            finishWork(par);
        }
    }

    static boolean isTerminated() {
        return !active[0] && deficit[0] == 0;
    }

    public static void main(String[] args) {
        n = 4; // Processes 0, 1, 2, 3
        active = new boolean[n];
        parent = new int[n];
        deficit = new int[n];
        Arrays.fill(parent, -1);

        // Step 1: Initiator (Process 0) starts computation
        active[0] = true;
        System.out.println("=== Dijkstra-Scholten Termination Detection ===");
        System.out.println("Process 0 starts as ACTIVE (Initiator).\n");

        // Step 2: Simulate distributed message passing
        sendMessage(0, 1); // P0 activates P1
        sendMessage(1, 2); // P1 activates P2
        sendMessage(1, 3); // P1 activates P3
        sendMessage(2, 3); // P3 already active -> immediate ACK

        System.out.println("\n--- Processes Finishing Work ---");
        finishWork(1); // Cannot become passive yet (waiting for P2 and P3)
        finishWork(2); // Passive -> sends ACK to P1
        finishWork(3); // Passive -> sends ACK to P1 -> P1's deficit hits 0 -> P1 becomes passive & ACKs P0
        finishWork(0); // P0's deficit is 0 -> P0 becomes passive

        System.out.println("\nGlobal Termination Detected? " + isTerminated());
    }
}