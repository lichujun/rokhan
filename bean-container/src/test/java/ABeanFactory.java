/**
 * @author lichujun
 * @date 2019/6/18 11:41
 */
public class ABeanFactory {

    public static ABean getABean(String name, CCBean cb) {
        return new ABean(name, cb);
    }

    public ABean getABean2(String name, CCBean cb) {
        return new ABean(name, cb);
    }
}

