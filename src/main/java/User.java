/**
 * Created by thongpv87 on 10/04/2017.
 */
public class User {
    private int id;

    public User(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    };


    @Override
    public boolean equals(Object o) {
        return id == ((User)o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
