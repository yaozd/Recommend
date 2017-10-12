package recommend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;

import static org.ujmp.core.util.MathUtil.round;

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
public class CBF {

    final static Logger logger = LoggerFactory.getLogger(CBF.class);
    private Matrix ratingsMatrix;
    private Matrix itemsFeatureMatrix;
    private String type;
    private Long userCounts;
    private Long itemCounts;
    private Long itemFeaturesCounts;


    public CBF(Matrix ratingsMatrix, Matrix itemsFeatureMatrix, String inp) {
        /**
         * @Method_name: CBF
         * @Description: 从商品的内容抽取特征，初始化变量
         * @Date: 2017/10/10
         * @Time: 14:58
         * @param: [ratingsMatrix, itemsFeatureMatrix, inp]
         * @return:
         **/
        logger.info("从商品内容抽取出特征,初始化变量");
        long startTime = System.currentTimeMillis();
        if (!inp.equalsIgnoreCase("none"))
            this.ratingsMatrix = impulation(ratingsMatrix, inp);
        else
            this.ratingsMatrix = ratingsMatrix;
        this.itemsFeatureMatrix = itemsFeatureMatrix;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        this.itemFeaturesCounts = itemsFeatureMatrix.getColumnCount();
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("{}", itemCounts);
        logger.info("初始化变量完成,用时 {} 秒", runningTime);
    }


    public Matrix CBFAverage() {
        /**
         * @Method_name: CBFAverage
         * @Description: 计算每个类型的偏好，作为用户特征矩阵
         * @Date: 2017/10/10
         * @Time: 14:59
         * @param: []
         * @return: org.ujmp.core.Matrix
         **/

        logger.info("使用类别平均评分进行推荐");
        long startTime = System.currentTimeMillis();
        Matrix userFeaturesMatrix = SparseMatrix.Factory.zeros(userCounts, itemFeaturesCounts);

        for (int i = 0; i < userCounts; i++) {
            Double meanUserRatingI = ratingsMatrix.selectRows(Calculation.Ret.NEW, i).getMeanValue();
            for (int n = 0; n < itemFeaturesCounts; n++) {
                Double v = 0.;
                Double d = 0.;
                for (int j = 0; j < itemCounts; j++) {
                    if (itemsFeatureMatrix.getAsDouble(j, n) > 0)
                        d += itemsFeatureMatrix.getAsDouble(j, n);
                    Double temp = ratingsMatrix.getAsDouble(i, j) - meanUserRatingI;
                    if (temp < 0)
                        temp = 0.;
                    v += temp * itemsFeatureMatrix.getAsDouble(j, n);
                }
                userFeaturesMatrix.setAsDouble(v / d, i, n);
            }
        }

        Matrix predictionsMatrix = SparseMatrix.Factory.zeros(userCounts, itemCounts);
        for (int i = 0; i < userCounts; i++) {
            for (int j = 0; j < itemCounts; j++) {
                predictionsMatrix.setAsDouble(calcSimilarity(userFeaturesMatrix.selectRows(Calculation.Ret.NEW, i), itemsFeatureMatrix.selectRows(Calculation.Ret.NEW, j), "cosine"), i, j);
            }
        }

        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("使用类别平均评分进行推荐完成,用时 {} 秒", runningTime);
        return predictionsMatrix;
    }

    public Matrix CBFRegression(Double alpha, Double l, Integer iterations, Double tol) {
        logger.info("使用正则化线性回归进行推荐");
        long startTime = System.currentTimeMillis();
        Matrix itemsFeatureMatrix2 = SparseMatrix.Factory.ones(itemCounts, itemFeaturesCounts + 1);
        for (int i = 0; i < itemCounts; i++) {
            for (int j = 1; j < itemCounts; j++) {
                itemsFeatureMatrix2.setAsDouble(itemsFeatureMatrix.getAsDouble(i, j), i, j);
            }
        }
        Matrix pMatrix = SparseMatrix.Factory.rand(userCounts, itemFeaturesCounts + 1);
        for (int i = 0; i < userCounts; i++) {
            pMatrix.setAsDouble(1., i, 0);
        }

        for (int it = 1; it <= iterations; it++) {
            for (int i = 0; i < userCounts; i++) {
                for (int f = 0; f < (itemFeaturesCounts) + 1; f++) {
                    if (f == 0) {
                        for (int j = 0; j < itemCounts; j++) {
                            if (ratingsMatrix.getAsDouble(i, j) > 0) {
                                Double diff = round(pMatrix.selectRows(Calculation.Ret.NEW, i).mtimes(itemsFeatureMatrix2.selectRows(Calculation.Ret.NEW, j).transpose()).getValueSum(), 2) - round(ratingsMatrix.getAsDouble(i, j),2);
                                pMatrix.setAsDouble(round(pMatrix.getAsDouble(i, j) - alpha * (diff * itemsFeatureMatrix2.getAsDouble(j, f)), 2), i, f);
                            }
                        }
                    } else {
                        for (int j = 0; j < itemCounts; j++) {
                            if (ratingsMatrix.getAsDouble(i, j) > 0) {
                                Double diff = round(pMatrix.selectRows(Calculation.Ret.NEW, i).mtimes(itemsFeatureMatrix2.selectRows(Calculation.Ret.NEW, j).transpose()).getValueSum(), 2) - round(ratingsMatrix.getAsDouble(i, j),2);
                                pMatrix.setAsDouble(round(pMatrix.getAsDouble(i, j) - alpha * (diff * itemsFeatureMatrix2.getAsDouble(j, f)) + l * pMatrix.getAsDouble(i, f), 2), i, f);
                            }
                        }
                    }
                }

            }
            Matrix predictionsMatrix = pMatrix.mtimes(itemsFeatureMatrix2.transpose());
            Double cost = getMSE(ratingsMatrix,predictionsMatrix);
            if (cost < tol) break;
            logger.info("第{}次迭代,误差为:{}.", it, cost);
            alpha *= 0.5;
        }
        Matrix predictionsMatrix = (pMatrix.mtimes(itemsFeatureMatrix2.transpose()));
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("使用正则化线性回归进行推荐推荐完成,用时 {} 秒", runningTime);

        return predictionsMatrix;
    }

    public Matrix getRatingsMatrix() {
        return ratingsMatrix;
    }

    public void setRatingsMatrix(Matrix ratingsMatrix) {
        this.ratingsMatrix = ratingsMatrix;
    }

    public Matrix getItemsFeatureMatrix() {
        return itemsFeatureMatrix;
    }

    public void setItemsFeatureMatrix(Matrix itemsFeatureMatrix) {
        this.itemsFeatureMatrix = itemsFeatureMatrix;
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

    public Long getItemFeaturesCounts() {
        return itemFeaturesCounts;
    }

    public void setItemFeaturesCounts(Long itemFeaturesCounts) {
        this.itemFeaturesCounts = itemFeaturesCounts;
    }
}
