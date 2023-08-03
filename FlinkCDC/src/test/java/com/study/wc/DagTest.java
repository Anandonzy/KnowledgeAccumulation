package com.study.wc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author wangziyu1
 * @Date 2022/9/27 16:43
 * @Version 1.0
 * 手动实现一个 DAG 有向无环图
 */
public class DagTest {

    public static void main(String[] args) {
        HashMap<String, List<String>> dag = new HashMap<>();

        // A => B
        // A => C
        ArrayList<String> ANeighbors = new ArrayList<>();
        ANeighbors.add("B");
        ANeighbors.add("C");
        dag.put("A", ANeighbors);


        // B => D
        // B => E
        ArrayList<String> BNeighbors = new ArrayList<>();
        BNeighbors.add("D");
        BNeighbors.add("E");
        dag.put("B", BNeighbors);

        // C => D
        // C => E
        ArrayList<String> CNeighbors = new ArrayList<>();
        CNeighbors.add("D");
        CNeighbors.add("E");
        dag.put("C", CNeighbors);

        // 拓扑排序算法
        // 第一个参数：有向无环图
        // 第二个参数：当前遍历到的顶点
        // 第三个参数：当前找到的路径
        topologicalSort(dag, "A", "A");

    }

    //拓扑排序
    //其中一种调用栈
    // topologicalSort(dag, "A", "A")
    // topologicalSort(dag, "B", "A => B")
    // topologicalSort(dag, "D", "A => B => D")
    private static void topologicalSort(HashMap<String, List<String>> dag, String vertex, String result) {

        if (vertex.equals("D") || vertex.equals("E")) {
            System.out.println(result);
        } else {
            for (String v : dag.get(vertex)) {
                topologicalSort(dag, v, result + " => " + v);
            }
        }

    }


}
