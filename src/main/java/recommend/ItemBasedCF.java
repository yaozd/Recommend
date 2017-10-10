package recommend;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static common.Utils.calcItemsSimilarityMatrix;

/**
 * @version: 1.0
 * @author: Liujm
 * @site: https://github.com/liujm7
 * @contact: kaka206@163.com
 * @software: Idea
 * @date： 2017/10/5
 * @package_name: recommend
 */
public class ItemBasedCF {
    final static Logger logger = LoggerFactory.getLogger(ItemBasedCF.class);
    private Matrix ratingsMatrix;
    private Integer K;
    private String type;
    private Long userCounts;
    private Long itemCounts;
    private Matrix itemSimilarityMatrix;

    public ItemBasedCF(Matrix ratingsMatrix, String type) {
        /**
         * @Method_name: ItemBasedCF
         * @Description: 初始化所需要的临时变量
         * @Date: 2017/10/5
         * @Time: 19:38
         * @param: [ratingsMatrix, type]
         * @return:
         **/
        logger.info("基于物品的协同过滤,初始化变量");
        long startTime = System.currentTimeMillis();
        this.ratingsMatrix = ratingsMatrix;
        this.type = type;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        this.itemSimilarityMatrix = calcItemsSimilarityMatrix(ratingsMatrix, type);
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("初始化变量完成,用时 {} 秒", runningTime);
    }

    public Matrix CalcRatings() {
        /**
         * @Method_name: CalcRatings
         * @Description: 计算评分矩阵
         * @Date: 2017/10/5
         * @Time: 19:39
         * @param: []
         * @return: org.ujmp.core.Matrix
         **/
        logger.info("计算评分矩阵开始");
        long startTime = System.currentTimeMillis();
        Matrix predictionsMatrix = ratingsMatrix.mtimes(Calculation.Ret.NEW, true, itemSimilarityMatrix);
        for (int j = 0; j < itemCounts; j++) {
            Matrix simi = itemSimilarityMatrix.selectColumns(Calculation.Ret.NEW, j);
            Double absValue = simi.getAbsoluteValueSum();
            if (absValue == 0)
                absValue++;
            for (int i = 0; i < userCounts; i++) {
                predictionsMatrix.setAsDouble(predictionsMatrix.getAsDouble(i, j) / absValue, i, j);
            }
        }
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("计算评分矩阵完成,用时 {} 秒", runningTime);
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

    public Matrix getItemSimilarityMatrix() {
        return itemSimilarityMatrix;
    }

    public void setItemSimilarityMatrix(Matrix itemSimilarityMatrix) {
        this.itemSimilarityMatrix = itemSimilarityMatrix;
    }

}
