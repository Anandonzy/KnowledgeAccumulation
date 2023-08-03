package com.study.wc;

/**
 * @Author wangziyu1
 * @Date 2022/10/12 16:06
 * @Version 1.0 动态加载字节码
 */
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.ExpressionEvaluator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class TestExpression {
    public static void main(String[] args) throws CompileException, InvocationTargetException {
        ExpressionEvaluator evaluator = new ExpressionEvaluator();
        Class[] classes = new Class[]{};
        List<Class> list = new ArrayList<Class>();
        list.add(long.class);
        classes = new Class[list.size()];
        list.toArray(classes);
        String[] arr = new String[]{"level"};
        evaluator.setParameters(arr, classes);
        evaluator.setExpressionType(boolean.class);
        evaluator.cook("(  1==level || 3==level || 5==level)");
        Object[] objects = new Object[]{6};
        Object o = evaluator.evaluate(objects);
        System.out.println(o);
    }
}
