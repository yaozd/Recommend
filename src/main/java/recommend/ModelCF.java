package recommend;

import no.uib.cipr.matrix.MatrixSingularException;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static common.Utils.*;
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


    private Double getSGDCostFunction(Matrix P, Matrix Qt, Integer K, Double l) {
        Double cost = 0.;
        Long counts = 0L;
        for (int i = 0; i < userCounts; i++) {
            for (int j = 0; j < itemCounts; j++) {
                if (ratingsMatrix.getAsDouble(i, j) > 0) {
                    counts += 1;
                    cost += Math.pow(ratingsMatrix.getAsDouble(i, j) - P.selectRows(Calculation.Ret.NEW, i).mtimes(Qt.selectColumns(Calculation.Ret.NEW, j)).getAsDouble(0, 0), 2);
                    for (int k = 0; k < K; k++) {
                        cost += l * (Math.pow(P.getAsDouble(i, k), 2) + Math.pow(Qt.getAsDouble(k, j), 2));
                    }
                }
            }
        }
        return cost / counts;
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
        Matrix P = Matrix.Factory.rand(userCounts, K);
        Matrix Q = Matrix.Factory.rand(itemCounts, K);

        Matrix Qt = Q.transpose();

        for (int it = 1; it <= iterations; it++) {
            logger.info("第 {} 次迭代开始.", it);
            Double p00 = P.getAsDouble(0, 0);
            Double qt00 = Qt.getAsDouble(0, 0);
            logger.info(p00.toString());
            logger.info(qt00.toString());
            for (int i = 0; i < userCounts; i++) {
                for (int j = 0; j < itemCounts; j++) {
                    if (ratingsMatrix.getAsDouble(i, j) > 0) {
                        Double pij = P.selectRows(Calculation.Ret.NEW, i).mtimes(Qt.selectColumns(Calculation.Ret.NEW, j)).getValueSum();
                        Double eij = round(ratingsMatrix.getAsDouble(i, j) - pij, 2);
                        for (int k = 0; k < K; k++) {
                            Double Pik = round(alpha * (2 * eij * Qt.getAsDouble(k, j) - l * P.getAsDouble(i, k)), 2);
                            P.setAsDouble(round(P.getAsDouble(i, k), 2) + Pik, i, k);
                            Double Qtkj = round(alpha * (2 * eij * P.getAsDouble(i, k) - l * Qt.getAsDouble(k, j)), 2);
                            Qt.setAsDouble(round(Qt.getAsDouble(k, j), 2) + Qtkj, k, j);
                        }
                    }

                }
            }
            alpha = alpha * 0.99;
            Double cost = getSGDCostFunction(P, Qt, K, l);
            logger.info("第 {} 次迭代，误差是 {} .", it, cost);
            if (cost < tol)
                break;
        }
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("调用SGD方法结束,用时 {} 秒", runningTime);
        return P.mtimes(Qt);
    }


    public Matrix ALS(Integer K, Integer iterations, Double lambda, Double tol) {
        logger.info("调用ALS交替最小下降法开始");
        long startTime = System.currentTimeMillis();
        Matrix P = Matrix.Factory.rand(userCounts, K);
        Matrix Q = Matrix.Factory.rand(itemCounts, K);
        Matrix Qt = Q.transpose();
        Matrix mask = Matrix.Factory.zeros(userCounts, itemCounts);
        for (int i = 0; i < userCounts; i++) {
            for (int j = 0; j < itemCounts; j++) {
                if (ratingsMatrix.getAsDouble(i, j) > 0)
                    mask.setAsDouble(1., i, j);
            }
        }
        for (int it = 0; it < iterations; it++) {
            Long itStartTime = System.currentTimeMillis();
            for (int i = 0; i < userCounts; i++) {
                Matrix mi = mask.selectRows(Calculation.Ret.NEW, i);
                Matrix Mi = Matrix.Factory.zeros(itemCounts, itemCounts);
                for (int j = 0; j < itemCounts; j++) {
                    Mi.setAsDouble(mi.getAsDouble(0, j), j, j);
                }
                Matrix pi = (Qt.mtimes(Mi).mtimes(Qt.transpose()).plus(Matrix.Factory.eye(K, K)
                        .times(lambda / 2.))).inv().mtimes(Qt).mtimes(Mi)
                        .mtimes(ratingsMatrix.selectRows(Calculation.Ret.NEW, i).transpose());

                for (int k = 0; k < K; k++) {
                    P.setAsDouble(round(pi.getAsDouble(k, 0), 1), i, k);
                }
            }

            for (int j = 0; j < itemCounts; j++) {
                Matrix mj = mask.selectColumns(Calculation.Ret.NEW, j);
                Matrix Mj = Matrix.Factory.zeros(userCounts, userCounts);
                for (int i = 0; i < userCounts; i++) {
                    Mj.setAsDouble(mj.getAsDouble(i, 0), i, i);
                }
                Matrix qj = (P.transpose().mtimes(Mj).mtimes(P).plus(Matrix.Factory.eye(K, K)
                        .times(lambda / 2.))).inv().mtimes(P.transpose()).mtimes(Mj)
                        .mtimes(ratingsMatrix.selectColumns(Calculation.Ret.NEW, j));
                for (int k = 0; k < K; k++) {
                    Qt.setAsDouble(round(qj.getAsDouble(k, 0), 1), k, j);
                }
            }
            Matrix predictionsMatrix = P.mtimes(Qt);
            Double cost = getMSE(ratingsMatrix, predictionsMatrix);
            Double itRunTime = (System.currentTimeMillis() - itStartTime) / 1000.0;
            logger.info("平均误差为{}.", cost);
            logger.info("第 {} 次迭代,误差是 {}.本次迭代时间:{}.", it, cost, itRunTime);
            if (cost < tol) break;
        }
        Matrix predictionsMatrix = modifyIllegalValue(P.mtimes(Qt));
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("调用ALS方法结束,用时 {} 秒", runningTime);

        return predictionsMatrix;
    }

    public Matrix SVD(String inp, Integer K) {
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
        Matrix userFeaturesMatrix = getSubMatrix(featuresMatrix[0], userCounts, K.longValue());
        Matrix singularValueMatrix = getSubMatrix(featuresMatrix[1], K.longValue(), K.longValue());
        Matrix itemFeaturesMatrix = getSubMatrix(featuresMatrix[2], itemCounts, K.longValue());
        logger.info("矩阵userFeaturesMatrix的行列数{},{}", userFeaturesMatrix.getRowCount(), userFeaturesMatrix.getColumnCount());
        System.out.println(userFeaturesMatrix);
        logger.info("矩阵singularValueMatrix的行列数{},{}", singularValueMatrix.getRowCount(), singularValueMatrix.getColumnCount());
        System.out.println(singularValueMatrix);
        logger.info("矩阵itemFeaturesMatrix的行列数{},{}", itemFeaturesMatrix.getRowCount(), itemFeaturesMatrix.getColumnCount());
        System.out.println(itemFeaturesMatrix);
        Matrix predictionsMatrix = userFeaturesMatrix.mtimes(singularValueMatrix).mtimes(itemFeaturesMatrix.transpose());

        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("调用SVD方法结束,用时 {} 秒", runningTime);
        Double cost = getMSE(ratingsMatrix, predictionsMatrix);
        logger.info("平均误差为{}.", cost);
        return predictionsMatrix;
    }

    public Matrix getRatingsMatrix() {
        return ratingsMatrix;
    }

    public void setRatingsMatrix(Matrix ratingsMatrix) {
        this.ratingsMatrix = ratingsMatrix;
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
}
