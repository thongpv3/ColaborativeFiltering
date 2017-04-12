package vn.edu.hust.soict.it4040;

/**
 * Created by thongpv87 on 12/04/2017.
 */
public interface CollaborativeFilteringModel {
    double predict(int user, int item);
    Rating[] recommendProducts(int user, int num);
    Rating[] recommendUsers(int item, int num);
}
