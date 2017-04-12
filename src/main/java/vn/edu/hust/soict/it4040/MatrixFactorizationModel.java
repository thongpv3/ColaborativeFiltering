package vn.edu.hust.soict.it4040;

/**
 * Created by thongpv87 on 12/04/2017.
 */
public class MatrixFactorizationModel implements CollaborativeFilteringModel {
    @Override
    public double predict(int user, int item) {
        return 0;
    }

    @Override
    public Rating[] recommendProducts(int user, int num) {
        return new Rating[0];
    }

    @Override
    public Rating[] recommendUsers(int item, int num) {
        return new Rating[0];
    }
}
