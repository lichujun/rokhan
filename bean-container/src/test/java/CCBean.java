import com.lee.rokhan.container.instance.Determine;
import com.lee.rokhan.container.instance.impl.IocConstructorDetermine;

/**
 * @author lichujun
 * @date 2019/6/18 11:42
 */
public class CCBean extends CBean {

    private String name;

    public CCBean(String name) {
        super(name);
    }

    @Override
    public String getName() {
        System.out.println(super.getName());
        return super.getName();
    }

}
