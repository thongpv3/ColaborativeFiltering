package vn.edu.hust.soict.it4040;

/**
 * Created by thongpv87 on 10/04/2017.
 */
public class User {
    private int id;

    private double rdu = 0;

    public User(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return id == user.id;
    }

    void setRDU(double RDU) {
        this.rdu = RDU;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int compare(User u2) {
        if (u2 == null)
            return 1;
        return id-u2.id;
    }
}
