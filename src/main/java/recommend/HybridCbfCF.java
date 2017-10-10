package recommend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @date： 2017/10/10
 * @package_name: recommend
 */
public class HybridCbfCF {
    final static Logger logger = LoggerFactory.getLogger(CBF.class);
    private Matrix ratingsMatrix;
    private Matrix itemsFeatureMatrix;
    private String type;
    private Long userCounts;
    private Long itemCounts;
    private Long itemFeaturesCounts;

    public HybridCbfCF(Matrix ratingsMatrix, Matrix itemsFeatureMatrix, String inp) {
        /**
         * @Method_name: HybridCbfCF
         * @Description: 使用cbf混合协同过滤矩阵，数据初始化
         * @Date: 2017/10/10
         * @Time: 15:00
         * @param: [ratingsMatrix, itemsFeatureMatrix, inp]
         * @return:
         **/
        logger.info("使用cbf混合协同过滤矩阵，数据初始化");
        long startTime = System.currentTimeMillis();
        if (!inp.equalsIgnoreCase("none"))
            this.ratingsMatrix = impulation(ratingsMatrix, inp);
        else
            this.ratingsMatrix = ratingsMatrix;
        this.itemsFeatureMatrix = itemsFeatureMatrix;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = itemsFeatureMatrix.getRowCount();
        this.itemFeaturesCounts = itemsFeatureMatrix.getColumnCount();
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("使用cbf混合协同过滤矩阵，数据初始化完成，用时{}秒", runningTime);
    }


    public Matrix CalcRatings() {
        /**
        * @Method_name: CalcRatings
        * @Description: 计算评分矩阵,将效用矩阵用用户特征向量进行扩充，计算用户相关性系数，计算评分
        * @Date: 2017/10/10
        * @Time: 15:02
        * @param: []
        * @return: org.ujmp.core.Matrix
        **/
        logger.info("使用效用矩阵增加用户类型特征进行推荐");
        long startTime = System.currentTimeMillis();
        Matrix ratingsAndFeatsMatrix = SparseMatrix.Factory.zeros(userCounts, itemCounts + itemFeaturesCounts);
        for (int i = 0; i < userCounts; i++) {
            Double meanUserRatingI = ratingsMatrix.selectRows(Calculation.Ret.NEW, i).getMeanValue();
            for (int j = 0; j < itemCounts; j++) {
                ratingsAndFeatsMatrix.setAsDouble(ratingsMatrix.getAsDouble(i, j) - meanUserRatingI, i, j);
            }
            for (long n = itemCounts; n < (itemCounts + itemFeaturesCounts); n++) {
                Double v = 0.;
                Double d = 0.;
                for (int j = 0; j < itemCounts; j++) {
                    if (itemsFeatureMatrix.getAsDouble(j, n) > 0)
                        d += itemsFeatureMatrix.getAsDouble(j, n);
                    v += (ratingsMatrix.getAsDouble(i, j) - meanUserRatingI) * itemsFeatureMatrix.getAsDouble(j, n);
                }
                ratingsAndFeatsMatrix.setAsDouble(v / d, i, n);
            }
        }
        Matrix userSimilarityMatrix = calcUserSimilarityMatrix(ratingsAndFeatsMatrix, "cosine");
        Matrix predictionsMatrix = userSimilarityMatrix.mtimes(ratingsMatrix);
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("使用效用矩阵增加用户类型特征进行推荐完成,用时 {} 秒", runningTime);
        return predictionsMatrix;
    }
}
