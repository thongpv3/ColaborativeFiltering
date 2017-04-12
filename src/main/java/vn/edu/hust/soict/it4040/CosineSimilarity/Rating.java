package vn.edu.hust.soict.it4040.CosineSimilarity;

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
        return rate;
    }

    @Override
    public String toString() {
        return String.format("%1$12d - %2$12d - %3$5.2f", uId, iId, rate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rating)) return false;

        Rating rating = (Rating) o;

        if (uId != rating.uId) return false;
        return iId == rating.iId;
    }

    @Override
    public int hashCode() {
        int result = uId;
        result = 31 * result + iId;
        return result;
    }
}
