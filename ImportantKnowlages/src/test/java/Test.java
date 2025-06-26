/**
 * @author wangziyu
 * @version 1.0
 * @since 2024/11/5 19:56
 */
public class Test {
    public static void main(String[] args) {
        System.out.println(jump(10));
    }

    public static int jump(int n) {
        if (n == 1) return 1;
        if (n == 2) return 2;
        else return jump(n - 1) + jump(n - 2);
    }
}
