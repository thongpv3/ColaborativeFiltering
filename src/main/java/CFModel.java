import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import org.apache.spark.ml.linalg.DenseMatrix;
import org.apache.spark.ml.linalg.Matrix;
import org.la4j.vector.DenseVector;
import org.la4j.vector.dense.BasicVector;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by thongpv87 on 10/04/2017.
 */

public class CFModel {
    //set thread count to available processors
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private ListMultimap<Integer, Pair<Integer, Double>> simMatrix;
    private ListMultimap<Integer, Pair<Integer, Double>> ratings;
    private int similars;
    ListMultimap<Integer, Integer> ratedList;

    private CFModel(ListMultimap<Integer, Pair<Integer, Double>> simMatrix, ListMultimap<Integer, Pair<Integer, Double>> ratings, int similars) {
        this.simMatrix = simMatrix;
        this.ratings = ratings;
        this.similars = similars;


        //Cached rated list
        ratedList = ArrayListMultimap.create();
        ratings.keySet().forEach((key)->{
            ratings.get(key).forEach((p)->{
                ratedList.put(p.t1(), key);
            });
        });
    }

    public static CFModel train(DataSet<Rating> dataSet, int similars) throws InterruptedException {
        ListMultimap<Integer, Pair<Integer, Double>> ratings = ArrayListMultimap.create();
        dataSet.stream().forEach(r -> ratings.put(r.user(), new Pair<Integer, Double>(r.item(), r.rate())));

        //PHASE 1 - STANDARDIZATION
        ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Callable<Void>> tasks = new LinkedList<>();

        ratings.keySet().forEach((key) -> {
            tasks.add(() -> {
                OptionalDouble avg = ratings.get(key).stream().mapToDouble((t) -> {
                    return t.t2();
                }).average();

                //print out computed avg values, just for debug
//                System.out.println(key + " - " +avg.getAsDouble());

                if (avg.isPresent()) {
                    ratings.get(key).stream().forEach(t -> {
                        t.setT2(new Double(t.t2() - avg.getAsDouble()));
                    });
                }
                return null;
            });
        });

        //Wait for all thread to finish computing tasks
        List<Future<Void>> futures = es.invokeAll(tasks);
        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //just for debug - get all standardization rating of key = 1 corresponding to user 1 with movie-rating pairs
        ratings.keySet().forEach((key) -> {
                    System.out.println("Key " + key + ":");
                    ratings.get(key).forEach((t) -> {
                        System.out.println(t.t1() + " - " + t.t2());
                    });
                }
        );
        System.out.println();


        //PHASE 2 - COMPUTE SIMILARITY MATRIX
        ListMultimap<Integer, Pair<Integer, Double>> simMatrix = ArrayListMultimap.create();
        tasks.clear();
        //we can't use vector because of user Id can be spare

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
                return null;
            });
        });
        //Wait for all thread to finish computing tasks
        futures = es.invokeAll(tasks);
        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
//        //just for test - print out simMatrix
//        simMatrix.get(1).forEach((pair) -> {
//            System.out.println(pair.t1() + " - " + pair.t2());
//        });

        ratings.clear();
        dataSet.stream().forEach(r -> ratings.put(r.user(), new Pair<Integer, Double>(r.item(), r.rate())));
        return new CFModel(simMatrix, ratings, similars);
    }

    public double predict(int uId, int mId) {
        List<Pair<Integer, Double>> sims = simMatrix.get(uId).stream().filter((p)->{
            return ratedList.get(mId).contains(p.t1());
        }).sorted((p1, p2)->{
            if (p1.t2().doubleValue() > p2.t2().doubleValue())
                return -1;
            else if (p1.t2().doubleValue() == p2.t2().doubleValue())
                return 0;
            return 1;
        }).limit(similars).collect(Collectors.toList());

        double S = 0.0;
        double P = 0.0;
        for (Pair<Integer, Double> p : sims) {
            S += p.t2();
            Optional<Pair<Integer, Double>> rate = ratings.get(p.t1()).stream().filter((p2)->{
                return p2.t1() == mId;
            }).findFirst();
            if (!rate.isPresent()) {
                System.err.println("Unexpected logic error");
                System.exit(1);
            }
            P += rate.get().t2()*p.t2().doubleValue();

        }
//        double sumRate = sims.mapToDouble((p)->{
//            Optional<Pair<Integer, Double>> rate = ratings.get(p.t1()).stream().filter((p2)->{
//                return p2.t1() == mId;
//            }).findFirst();
//            if (!rate.isPresent()) {
//                System.err.println("Unexpected logic error");
//                System.exit(1);
//            }
//            return rate.get().t2() * p.t2();
//        }).sum();
//
//        double sumSim = sims.mapToDouble((p)->{
//            return p.t2();
//        }).sum();

        if (S == 0)
            return 0;
        return P/S;
    }
}
