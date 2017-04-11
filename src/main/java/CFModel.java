import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.la4j.*;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic1DMatrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.matrix.sparse.CRSMatrix;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * Created by thongpv87 on 10/04/2017.
 */

public class CFModel {
    //set thread count to available processors
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private ListMultimap<Integer, Pair<Integer, Double>> simMatrix;
    private HashMap<Pair<Integer, Integer>, Double> ratings;
    private HashMap<Pair<Integer, Integer>, Double> predicted;
    private int similars;
    ListMultimap<Integer, Integer> ratedList;

    private CFModel(ListMultimap<Integer, Pair<Integer, Double>> simMatrix, HashMap<Pair<Integer, Integer>, Double> ratings, int similars) {
        this.simMatrix = simMatrix;
        this.ratings = ratings;
        this.similars = similars;
        this.predicted = new HashMap<>();

        //Cached rated list
        ratedList = ArrayListMultimap.create();
        ratings.keySet().stream().forEach((key)->{
            ratedList.put(key.t2(), key.t1());
        });
    }

    public static CFModel train2(DataSet<Rating> trainingSet, int similars) {
        //todo check minUserId and minItemId
        int nUser, nItem;
        nUser = trainingSet.stream().mapToInt((r)->{
            return r.user();
        }).max().getAsInt() +1;
        nItem = trainingSet.stream().mapToInt((r)->{
            return r.item();
        }).max().getAsInt() + 1;

        Matrix data = new CRSMatrix(nUser, nItem);
        trainingSet.forEach(r->{
            data.set(r.user(), r.item(), r.rate());
        });

        Utils.printMatrix(data);
        //todo parallel
        //PHASE 1 - STANDARDIZATION
        for (int i=0; i<nUser; i++) {
            Vector row = data.getRow(i);
            double rowSum = row.sum();
            if (rowSum > 0)
                row = row.subtract(row.divide(rowSum));
            data.setRow(i, row);
        }

        Utils.printMatrix(data);

        Matrix simMatrix = new Basic2DMatrix(nUser, nUser);
        for (int i=0; i<nUser; i++) {
            Vector rowi = data.getRow(i);
            for (int j=i+1; j<nUser; j++) {
                Vector rowj = data.getRow(j);
                double innerProduct = rowi.innerProduct(rowj);
                double module1 = rowi.hadamardProduct(rowi).sum();
                double module2 = rowi.hadamardProduct(rowi).sum();
                simMatrix.set(i,j, innerProduct/Math.sqrt(module1 * module2));
                simMatrix.set(j,i, innerProduct/Math.sqrt(module1 * module2));
            }
        }

        for (int i=0; i<nUser; i++) {
            Vector row = simMatrix.getRow(i);
            row.forEach((d)->{
                System.out.print(String.format("%1$5.1f", d));
            });
            System.out.println();
        }


        return null;
    }

    public static CFModel train(DataSet<Rating> dataSet, int similars) throws InterruptedException {
        ListMultimap<Integer, Pair<Integer, Double>> ratings = ArrayListMultimap.create();
        dataSet.stream().forEach(r -> ratings.put(r.user(), new Pair<Integer, Double>(r.item(), r.rate())));

        //PHASE 1 - STANDARDIZATION
        ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Runnable> tasks = new LinkedList<>();

        ratings.keySet().forEach((key) -> {
            tasks.add(() -> {
                OptionalDouble avg = ratings.get(key).stream().mapToDouble((t) -> {
                    return t.t2();
                }).average();

                if (avg.isPresent()) {
                    ratings.get(key).stream().forEach(t -> {
                        t.setT2(new Double(t.t2() - avg.getAsDouble()));
                    });
                }
            });
        });

        //Wait for all thread to finish computing tasks
        final List<CompletableFuture<Void>> futures = new ArrayList<>();

        tasks.forEach( r->{
            futures.add(CompletableFuture.runAsync(r, es));
        });
        try {
            CompletableFuture<Void>[] futuresArr = new CompletableFuture[futures.size()];
            futures.toArray(futuresArr);
            CompletableFuture.allOf(futuresArr).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        //PHASE 2 - COMPUTE SIMILARITY MATRIX
        ListMultimap<Integer, Pair<Integer, Double>> simMatrix = ArrayListMultimap.create();
        tasks.clear();
        //we can't use vector because of user Id can be spared

        ratings.keySet().forEach((key1) -> {
            tasks.add(() -> {
                //compute cosine similarity
                HashMap<Integer, Double> vec1 = new HashMap<>();
                HashMap<Integer, Double> vec2 = new HashMap<>();
                vec1.clear();
                ratings.get(key1).forEach((p) -> {
                    vec1.put(p.t1(), p.t2());
                });
                ratings.keySet().forEach((key2) -> {
                    if (key1.intValue() < key2.intValue()) {
                        vec2.clear();
                        //we can transform into hashmap because each key-value pairs are identical
                        ratings.get(key2).forEach((p) -> {
                            vec2.put(p.t1(), p.t2());
                        });

                        //compute scalar product of two vectors
                        double sum = 0;
                        for (Map.Entry<Integer, Double> e : vec1.entrySet()) {
                            Double r2 = vec2.get(e.getKey());
                            if (r2 != null)
                                sum = sum + e.getValue().doubleValue() * r2.doubleValue();
                        }
                        ;

                        //compute product of two modules
                        double modules1 = vec1.entrySet().stream().mapToDouble((v) -> {
                            return v.getValue().doubleValue() * v.getValue().doubleValue();
                        }).sum();
                        double modules2 = vec2.entrySet().stream().mapToDouble((v) -> {
                            return v.getValue().doubleValue() * v.getValue().doubleValue();
                        }).sum();

                        double modules = Math.sqrt(modules1) * Math.sqrt(modules2);

                        if (modules != 0) {
                            simMatrix.put(new Integer(key1), new Pair<>(key2, sum / modules));
                            simMatrix.put(new Integer(key2), new Pair<>(key1, sum / modules));
                        } else {
                            simMatrix.put(new Integer(key1), new Pair<>(key2, 0.0));
                            simMatrix.put(new Integer(key2), new Pair<>(key1, 0.0));
                        }
                    }
                });
            });
        });
        //Wait for all thread to finish computing tasks
        tasks.forEach( r->{
            futures.add(CompletableFuture.runAsync(r, es));
        });
        try {
            CompletableFuture<Void>[] futuresArr = new CompletableFuture[futures.size()];
            futures.toArray(futuresArr);
            CompletableFuture.allOf(futuresArr).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //sort sim matrix
        tasks.clear();
        simMatrix.keySet().forEach(
                (key)->{
                    tasks.add(()->{
                        simMatrix.get(key).sort((p1, p2)->{
                            if (p1.t2() > p2.t2())
                                return -1;
                            else if (p1.t2() == p2.t2())
                                return 0;
                            return 1;
                        });
                    });
                });
        //Wait for all thread to finish computing tasks
        tasks.forEach( r->{
            futures.add(CompletableFuture.runAsync(r, es));
        });
        try {
            CompletableFuture<Void>[] futuresArr = new CompletableFuture[futures.size()];
            futures.toArray(futuresArr);
            CompletableFuture.allOf(futuresArr).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        HashMap<Pair<Integer, Integer>, Double> rts = new HashMap<>();
        dataSet.stream().forEach(r -> rts.put(new Pair<Integer, Integer>(r.user(), r.item()), new Double(r.rate())));

        System.gc();
        return new CFModel(simMatrix, rts, similars);
    }

    public double predict(int uId, int mId) {
        Pair<Integer, Integer> pair = new Pair<>(uId, mId);
        Double result;
        result = (ratings.get(pair) != null) ?  ratings.get(pair) :  predicted.get(pair);
        if (result != null)
            return result.doubleValue();

        List<Pair<Integer, Double>> sims = simMatrix.get(uId).stream().filter((p)->{
            return ratedList.get(mId).contains(p.t1());
        }).limit(similars).collect(Collectors.toList());

        double S = 0.0;
        double P = 0.0;
        for (Pair<Integer, Double> p : sims) {
            S += p.t2();
            double rate = ratings.get(new Pair<Integer, Integer>(p.t1(), mId));
            P += rate*p.t2().doubleValue();
        }
        result = (S==0) ? 0 : P/S;
        predicted.put(pair, result);

        return result.doubleValue();
    }

    public HashMap<Pair<Integer, Integer>, Double> predicted() {
        return predicted;
    }
}

class Utils {
    public static void printMatrix(Matrix m) {
        System.out.println();
        for (int i=0; i<m.rows(); i++) {
            Vector row = m.getRow(i);
            row.forEach((d)->{
                System.out.print(String.format("%1$5.2f", d));
            });
            System.out.println();
        }
        System.out.println();
    }
}