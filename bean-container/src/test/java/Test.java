import lombok.AllArgsConstructor;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/6/17 14:07
 */
public class Test {

    private static final Enhancer ENHANCER = new Enhancer();

    public static void main(String[] args1) throws Throwable {

        Test test = new Test();
        TestCglib testCglib = new TestCglib(test);
        Class<?> superClass = test.getClass();
        ENHANCER.setSuperclass(superClass);
        ENHANCER.setInterfaces(test.getClass().getInterfaces());
        ENHANCER.setCallback(testCglib);
        ((Test)ENHANCER.create()).test();
    }

    public void test() {

    }

    @AllArgsConstructor
    public static class TestCglib implements MethodInterceptor {

        private Object realObject;

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            System.out.println("proxy");
            return realObject;
        }
    }
}
