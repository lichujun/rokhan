import com.lee.rokhan.container.instance.Determine;

/**
 * @author lichujun
 * @date 2019/6/18 11:41
 */
public class CBean {
    private String name;

    public CBean(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void doSome(Determine determine) {

    }
}
