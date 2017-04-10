/**
 * Created by thongpv87 on 10/04/2017.
 */
public class CFModel {
    private int rank;   //the number of latent factors in the model.
    private int iterations;  //the number of iterations to run
    private double lambda;  //regularization parameter in ALS.
    private double alpha;   //confidence parameter

    public static CFModel train(DataSet<Rating> dataSet, int rank, int iteration, double lamda, double alpha) {
        return null;
    }
}
