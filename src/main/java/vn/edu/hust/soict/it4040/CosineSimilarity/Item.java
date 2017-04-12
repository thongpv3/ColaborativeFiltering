package vn.edu.hust.soict.it4040.CosineSimilarity;

/**
 * Created by thongpv87 on 10/04/2017.
 */
public class Item {
    private int id;

    public Item(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;

        Item item = (Item) o;

        return id == item.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    int compare(Item i2) {
        if (i2 == null)
            return 1;
        return id-i2.id;
    }
}
