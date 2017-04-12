function [similarity_matrix] = cosine_similarity(ratings_matrix)
  %compute avg vector
  avgs = sum(ratings_matrix,2)./sum(ratings_matrix>0,2);
  
  StandardMatrix = [];
  
  %for each row
  for (i=1:size(ratings_matrix,1))
    t1 = ratings_matrix(i,:);
    t2 = [];
    t2(t1==0) = 0;
    t2(t1!=0) = avgs(i);
    StandardMatrix(i, :) = t1 - t2;
  end
    
  SimMatrix = [];
  for (i=1:size(ratings_matrix, 1))
    vec1 = StandardMatrix(i,:);
    for (j=i:size(ratings_matrix,1))
      vec2 = StandardMatrix(j, :);
      sim = dot(vec1, vec2)/norm(vec1)/norm(vec2);
      SimMatrix(i,j) = SimMatrix(j,i) = sim;
    end
   end 
   similarity_matrix = SimMatrix;
end %end function