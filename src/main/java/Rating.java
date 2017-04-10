/**
 * Created by thongpv87 on 10/04/2017.
 */
public class Rating {
    private int uId;
    private int iId;
    private double rate;

    public Rating(int uId, int iId, double rate) {
        this.uId = uId;
        this.iId = iId;
        this.rate = rate;
    }

    public int user() {
        return uId;
    }

    public int item() {
        return iId;
    }

    public double rate() {
        return rate();
    }

    @Override
    public String toString() {
        return String.format("%1$12d - %2$12d - %3$5.2f", uId, iId, rate);
    }
}
