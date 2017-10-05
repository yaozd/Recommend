package recommend;

import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;


import static common.Utils.*;

/**
 * @version: 1.0
 * @author: Liujm
 * @site: https://github.com/liujm7
 * @contact: kaka206@163.com
 * @software: Idea
 * @date： 2017/9/30
 * @package_name: recommend
 */
public class UserBasedCF {
    private Matrix ratingsMatrix;
    private Integer K;
    private String type;
    private Long userCounts;
    private Long itemCounts;
    private Matrix userSimilarityMatrix;


    public UserBasedCF(Matrix ratingsMatrix, String type) {
        this.ratingsMatrix = ratingsMatrix;
        this.type = type;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        this.userSimilarityMatrix = calcUserSimilarityMatrix(ratingsMatrix, type);
    }

    public Matrix CalcRatings() {
        Matrix ratingsDiffMatrix = SparseMatrix.Factory.zeros(userCounts, itemCounts);
        for (int i = 0; i < userCounts; i++) {
            Matrix Ri = ratingsMatrix.selectRows(Calculation.Ret.NEW, i);
            Double meanValue = Ri.getMeanValue();
            for (int j = 0; j < itemCounts; j++) {
                Double ratingDiff = ratingsDiffMatrix.getAsDouble(i, j) - meanValue;
                ratingsDiffMatrix.setAsDouble(ratingDiff, i, j);
            }
        }
        Matrix predictionsMatrix = userSimilarityMatrix.mtimes(ratingsDiffMatrix);
        for (int i = 0; i < userCounts; i++) {
            Matrix Ri = ratingsMatrix.selectRows(Calculation.Ret.NEW, i);
            Matrix simi = userSimilarityMatrix.selectRows(Calculation.Ret.NEW, i);
            Double meanValue = Ri.getMeanValue();
            Double absValue = simi.getAbsoluteValueSum();
            for (int j = 0; j < itemCounts; j++) {
                predictionsMatrix.setAsDouble(predictionsMatrix.getAsDouble(i, j) / absValue + meanValue, i, j);
            }
        }
        return predictionsMatrix;
    }


    public Matrix getRatingsMatrix() {
        return ratingsMatrix;
    }

    public void setRatingsMatrix(Matrix ratingsMatrix) {
        this.ratingsMatrix = ratingsMatrix;
    }

    public Integer getK() {
        return K;
    }

    public void setK(Integer k) {
        K = k;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserCounts() {
        return userCounts;
    }

    public void setUserCounts(Long userCounts) {
        this.userCounts = userCounts;
    }

    public Long getItemCounts() {
        return itemCounts;
    }

    public void setItemCounts(Long itemCounts) {
        this.itemCounts = itemCounts;
    }

    public Matrix getUserSimilarityMatrix() {
        return userSimilarityMatrix;
    }

    public void setUserSimilarityMatrix(Matrix userSimilarityMatrix) {
        this.userSimilarityMatrix = userSimilarityMatrix;
    }


}
