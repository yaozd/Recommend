package recommend;

import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;


import static common.Utils.getUserSimilarityMatrix;

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

    public UserBasedCF(Matrix ratingsMatrix, Integer K, String type) {
        this.ratingsMatrix = ratingsMatrix;
        this.K = K;
        this.type = type;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        this.userSimilarityMatrix = getUserSimilarityMatrix(this.ratingsMatrix, this.type);
        if (K != -1){
            Matrix userNeighsSimilarityMatrix = DenseMatrix.Factory.zeros(this.userCounts,this.userCounts);
            for(int i=0;i<this.userCounts;i++){
//                userSimilarityMatrix.selectRows(Calculation.Ret.NEW,i)
            }
        }
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

    public Matrix getUserSimilarityMatrix(Matrix ratingsMatrix, String type) {
        return userSimilarityMatrix;
    }

    public void setUserSimilarityMatrix(Matrix userSimilarityMatrix) {
        this.userSimilarityMatrix = userSimilarityMatrix;
    }
}
