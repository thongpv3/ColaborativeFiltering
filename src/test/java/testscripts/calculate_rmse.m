%clear;
function [rmse] = calculate_rmse(file_path)
output = dlmread(file_path);
%remove first line
output = output(2:end,:);
output(:,5) = output(:,4)-output(:,3);
output(:,6) = output(:,5).*output(:,5);
sum_column = sum(output,1);
rmse = sqrt(sum_column(6)/size(output,1));
end