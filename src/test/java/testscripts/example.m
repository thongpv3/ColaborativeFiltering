path = "/home/thongpv87/IdeaProjects/ColaborativeFiltering/src/test/java/result/ml-100k1.result";

A = [
    1.00    0.00    3.00    0.00    0.00    5.00    0.00    0.00    5.00    0.00    4.00    0.00
    0.00    0.00    5.00    4.00    0.00    0.00    4.00    0.00    0.00    2.00    1.00    3.00
    2.00    4.00    0.00    1.00    2.00    0.00    3.00    0.00    4.00    3.00    5.00    0.00
    0.00    2.00    4.00    0.00    5.00    0.00    0.00    4.00    0.00    0.00    2.00    0.00
    0.00    0.00    4.00    3.00    4.00    2.00    0.00    0.00    0.00    0.00    2.00    5.00
    1.00    0.00    3.00    0.00    3.00    0.00    0.00    2.00    0.00    0.00    4.00    0.00];
    
    sim = cosine_similarity(A);
    disp("Cosine similarity matrix: ");
    disp(sim);
    
    rmse = calculate_rmse(path);
    disp("Calculated RMSE: ");
    disp(rmse);