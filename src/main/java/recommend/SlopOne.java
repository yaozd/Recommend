package recommend;

import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version: 1.0
 * @author: Liujm
 * @site: https://github.com/liujm7
 * @contact: kaka206@163.com
 * @software: Idea
 * @date： 2017/10/8
 * @package_name: recommend
 */
public class SlopOne {
    final static Logger logger = LoggerFactory.getLogger(SlopOne.class);
    private Matrix ratingsMatrix;
    private Long userCounts;
    private Long itemCounts;
    private Matrix difMatrix;
    private Matrix nRatingsMatrix;

    public SlopOne(Matrix ratingsMatrix) {
        /**
         * @Method_name: SlopOne
         * @Description: 初始化变量
         * @Date: 2017/10/8
         * @Time: 12:19
         * @param: [ratingsMatrix, type]
         * @return:
         **/
        logger.info("SlopOne 初始化变量.");
        long startTime = System.currentTimeMillis();
        this.ratingsMatrix = ratingsMatrix;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        this.difMatrix = SparseMatrix.Factory.zeros(itemCounts, itemCounts);
        this.nRatingsMatrix = SparseMatrix.Factory.zeros(itemCounts, itemCounts);
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("SlopOne 初始化完成,用时 {} 秒.", runningTime);
    }

    private void BuildSimilarityMatrix() {
        /**
         * @Method_name: BuildSimilarityMatrix
         * @Description: 建立相似性矩阵，差值相似性
         * @Date: 2017/10/8
         * @Time: 12:19
         * @param: []
         * @return: void
         **/
        logger.info("建立差值相似性矩阵");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < itemCounts; i++) {
            for (int j = i + 1; j < itemCounts; j++) {
                Double nCounts = 0.;
                Double diff = 0.;
                for (int k = 0; k < userCounts; k++) {
                    if (ratingsMatrix.getAsDouble(k, i) > 0 && ratingsMatrix.getAsDouble(k, j) > 0) {
                        nCounts++;
                        diff += ratingsMatrix.getAsDouble(k, i) + ratingsMatrix.getAsDouble(k, j);
                    }
                }
                this.difMatrix.setAsDouble((diff + 1) / (nCounts + 1), i, j);
                this.difMatrix.setAsDouble(difMatrix.getAsDouble(i, j), j, i);
                this.nRatingsMatrix.setAsDouble(nCounts, i, j);
                this.nRatingsMatrix.setAsDouble(nCounts, j, i);
            }
        }
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("建立差值相似性矩阵完成,用时 {} 秒.", runningTime);
    }

    public Matrix CalcRatings() {
        /**
         * @Method_name: CalcRatings
         * @Description: 计算预测评分，评分为0的意味着已经评分过
         * @Date: 2017/10/8
         * @Time: 12:20
         * @param: []
         * @return: org.ujmp.core.Matrix
         **/
        Matrix predictionsMatrix = SparseMatrix.Factory.zeros(userCounts, itemCounts);
        BuildSimilarityMatrix();
        logger.info("计算评分矩阵");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < userCounts; i++) {
            for (int j = 0; j < itemCounts; j++) {
                if (ratingsMatrix.getAsDouble(i, j) == 0) {
                    Matrix ratingsI = ratingsMatrix.selectRows(Calculation.Ret.NEW, i);
                    Matrix difMatrixJ = difMatrix.selectRows(Calculation.Ret.NEW, j);
                    Matrix nRatingsMatrixJ = nRatingsMatrix.selectColumns(Calculation.Ret.NEW, j);
                    Double nRatingsSum = nRatingsMatrixJ.getValueSum();
                    Double prediction = ratingsI.plus(difMatrixJ).mtimes(Calculation.Ret.NEW, true, nRatingsMatrixJ).getAsDouble(0, 0) / nRatingsSum;
                    predictionsMatrix.setAsDouble(prediction, i, j);
                }
            }
        }
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("计算评分矩阵完成,用时 {} 秒", runningTime);
        return predictionsMatrix;
    }


}
