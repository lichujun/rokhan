import com.lee.rokhan.common.utils.ReflectionUtils;
import lombok.AllArgsConstructor;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/6/17 14:07
 */
public class Test {

    private static final Enhancer ENHANCER = new Enhancer();

    private String name;

    public static void main(String[] args1) throws Throwable {

        Test test = new Test();
        TestCglib testCglib = new TestCglib(test);
        Class<?> superClass = test.getClass();
        ENHANCER.setSuperclass(superClass);
        ENHANCER.setInterfaces(test.getClass().getInterfaces());
        ENHANCER.setCallback(testCglib);
        Object obj = ENHANCER.create();
        //Field field = ReflectionUtils.getDeclaredField(obj, "name");
        ReflectionUtils.setFieldValue(test, "name", "hello");
        ((Test) obj).test();
    }

    public void test() {
        System.out.println(name);
    }

    @AllArgsConstructor
    public static class TestCglib implements MethodInterceptor {

        private Object realObject;

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            System.out.println("proxy");
            return method.invoke(realObject, objects);
        }
    }
}
