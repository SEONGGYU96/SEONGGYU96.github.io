import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int N = Integer.parseInt(st.nextToken());
        int K = Integer.parseInt(st.nextToken());

        System.out.println(solution(N, K));
    }

    private static int solution(int N, int K) {
        int answer = -1;
        int[] offset = {1, -1, 2};

        boolean[] isVisited = new boolean[100001];

        Queue<Integer> queue = new LinkedList<>();
        queue.add(N);
        isVisited[N] = true;

        while (!queue.isEmpty()) {
            answer++;
            //System.out.println("============== " + answer + " ============");
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int current = queue.poll();
                //System.out.println(current);
                if (current == K) {
                    return answer;
                }
                for (int _offset : offset) {
                    int newPosition = _offset != 2 ? current + _offset : current * _offset;
                    if (newPosition > 100000 || newPosition < 0 || isVisited[newPosition]) {
                        continue;
                    }
                    queue.add(newPosition);
                    isVisited[newPosition] = true;
                }
            }
        }
        return -1;
    }
}

