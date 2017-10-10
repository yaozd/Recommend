package recommend;

import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static common.Utils.impulation;
import static org.ujmp.core.util.MathUtil.round;

/**
 * @version: 1.0
 * @author: Liujm
 * @site: https://github.com/liujm7
 * @contact: kaka206@163.com
 * @software: Idea
 * @date： 2017/10/8
 * @package_name: recommend
 */
public class ModelCF {
    final static Logger logger = LoggerFactory.getLogger(ModelCF.class);
    private Matrix ratingsMatrix;
    private Long userCounts;
    private Long itemCounts;

    public ModelCF(Matrix ratingsMatrix) {
        /**
         * @Method_name: ModelCF
         * @Description: 初始化变量
         * @Date: 2017/10/9
         * @Time: 9:01
         * @param: [ratingsMatrix]
         * @return:
         **/
        logger.info("ModelCF 初始化");
        long startTime = System.currentTimeMillis();
        this.ratingsMatrix = ratingsMatrix;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("初始化变量完成,用时 {} 秒", runningTime);
    }

    public Matrix SGD(Integer K, Integer iterations, Double alpha, Double l, Double tol) {
        /**
         * @Method_name: SGD
         * @Description: 随机梯度下降法
         * @Date: 2017/10/8
         * @Time: 14:10
         * @param: [K, iterations, alpha, l, tol] K:矩阵的阶数 iterations:迭代次数 alpha学习速率 l 正则化系数,防止过拟合 tol 收敛判据
         * @return: org.ujmp.core.Matrix
         **/
        logger.info("调用SGD随机梯度下降法开始");
        long startTime = System.currentTimeMillis();
        Matrix P = SparseMatrix.Factory.randn(userCounts, K);
        Matrix Q = SparseMatrix.Factory.randn(itemCounts, K);
        Matrix Qt = Q.transpose();

        for (int it = 1; it <= iterations; it++) {
            logger.info("第 {} 次迭代开始.", it);
            for (int i = 0; i < userCounts; i++) {
                for (int j = 0; j < itemCounts; j++) {
                    if (ratingsMatrix.getAsDouble(i, j) > 0) {
//                        System.out.println(ratingsMatrix.getAsDouble(i, j) - P.selectRows(Calculation.Ret.NEW, i).mtimes(Qt.selectColumns(Calculation.Ret.NEW, j)).getAsDouble(0, 0));
                        Double eij = round(ratingsMatrix.getAsDouble(i, j) - P.selectRows(Calculation.Ret.NEW, i).mtimes(Qt.selectColumns(Calculation.Ret.NEW, j)).getAsDouble(0, 0), 3);
//                        System.out.println(eij);
                        for (int k = 0; k < K; k++) {
                            Double Pik = round(alpha * (2 * eij * Qt.getAsDouble(k, j) - l * P.getAsDouble(i, k)), 0);
                            P.setAsDouble(round(P.getAsDouble(i, k), 0) + Pik, i, k);
                            Double Qtkj = round(alpha * (2 * eij * P.getAsDouble(i, k) - l * Qt.getAsDouble(k, j)), 0);
                            Qt.setAsDouble(round(Qt.getAsDouble(k, j), 0) + Qtkj, k, j);
                        }
                    }
                }
            }
//            System.out.println(P);
            Double cost = 0.;
            for (int i = 0; i < userCounts; i++) {
                for (int j = 0; j < itemCounts; j++) {
                    if (ratingsMatrix.getAsDouble(i, j) > 0) {
                        cost += Math.pow(ratingsMatrix.getAsDouble(i, j) - P.selectRows(Calculation.Ret.NEW, i).mtimes(Qt.selectColumns(Calculation.Ret.NEW, j)).getAsDouble(0, 0), 2);
                        for (int k = 0; k < K; k++) {
                            cost += l * (Math.pow(P.getAsDouble(i, k), 2) + Math.pow(Qt.getAsDouble(k, j), 2));
                        }
                    }
                }
            }
            logger.info("第 {} 次迭代，误差是 {} .", it, cost);
            System.out.println(P.mtimes(Qt));
            if (cost < tol)
                break;
        }
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("调用SGD方法结束,用时 {} 秒", runningTime);
        return P.mtimes(Qt);
    }

    public Matrix SVD(String inp) {
        /**
         * @Method_name: SVD
         * @Description: 使用svd分解后聚合，但是目前该矩阵类并没有截断方法
         * @Date: 2017/10/9
         * @Time: 10:59
         * @param: [inp] 选择插值的方法
         * @return: org.ujmp.core.Matrix
         **/
        logger.info("调用SVD方法开始");
        long startTime = System.currentTimeMillis();
        if (inp.equalsIgnoreCase("none")) {
            this.ratingsMatrix = impulation(ratingsMatrix, inp);
        }
        Matrix[] featuresMatrix = ratingsMatrix.svd();
        Matrix predictionsMatrix = featuresMatrix[0].mtimes(featuresMatrix[1]).mtimes(featuresMatrix[2].transpose());
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("调用SVD方法结束,用时 {} 秒", runningTime);
        return predictionsMatrix;
    }


}
