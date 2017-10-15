package recommend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;


import java.util.HashMap;

import static common.Utils.*;
import static org.ujmp.core.util.MathUtil.round;

/**
 * @version: 1.0
 * @author: Liujm
 * @site: https://github.com/liujm7
 * @contact: kaka206@163.com
 * @software: Idea
 * @date： 2017/10/14
 * @package_name: recommend
 */
public class SVD {
    final static Logger logger = LoggerFactory.getLogger(SVD.class);
    private Matrix ratingsMatrix;
    public Matrix impulationRatingsMatrix;
    private Long userCounts;
    private Long itemCounts;
    private HashMap<Integer, Double> usersDiffMap = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> itemsDiffMap = new HashMap<Integer, Double>();
    private Double allMeanValue;

    public SVD(Matrix ratingsMatrix, Integer lambda1, Integer lambda2, String inp) {
        logger.info("SVD 初始化");
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        this.ratingsMatrix = ratingsMatrix;
        this.allMeanValue = getAllMeanValue(ratingsMatrix);
        this.itemsDiffMap = getColumnDiffMap(ratingsMatrix, lambda1);
        this.usersDiffMap = getRowDiffMap(ratingsMatrix, lambda2, itemsDiffMap);
        this.impulationRatingsMatrix = impulation(ratingsMatrix, inp);
        logger.info("SVD 初始化完成");
    }


    public Matrix CalcRatings(Integer K, Integer iterations, Double alpha, Double lambda) {
        logger.info("调用svd分解预测 开始");
        long startTime = System.currentTimeMillis();
        Matrix P = Matrix.Factory.rand(userCounts, K);
        Matrix Q = Matrix.Factory.rand(itemCounts, K);
        Matrix predictionsMatrix = P.mtimes(Q.transpose());
        for (int it = 0; it < iterations; it++) {
            Long itStartTime = System.currentTimeMillis();
            for (int i = 0; i < userCounts; i++) {
                for (int j = 0; j < userCounts; j++) {
                    if (ratingsMatrix.getAsDouble(i, j) > 0) {
                        Double eij = round(ratingsMatrix.getAsDouble(i, j) - predictionsMatrix.getAsDouble(i, j), 3);
                        Double bi = round(usersDiffMap.get(i) + alpha * (eij - lambda * usersDiffMap.get(i)), 3);
                        usersDiffMap.put(i, bi);
                        Double bj = round(itemsDiffMap.get(j) + alpha * (eij - lambda * itemsDiffMap.get(j)), 3);
                        itemsDiffMap.put(j, bj);
                        for (int k = 0; k < K; k++) {
                            Double qjk = round(Q.getAsDouble(j, k) + alpha * (eij * P.getAsDouble(i, k) - lambda * Q.getAsDouble(j, k)), 3);
                            Q.setAsDouble(qjk, j, k);
                            Double pik = round(P.getAsDouble(i, k) + alpha * (eij * Q.getAsDouble(j, k) - lambda * P.getAsDouble(i, k)), 3);
                            P.setAsDouble(pik, i, k);
                        }
                    }
                }
            }
            for (int i = 0; i < userCounts; i++) {
                for (int j = 0; j < itemCounts; j++) {
                    Double predictionsIJ = allMeanValue + itemsDiffMap.get(j) + usersDiffMap.get(i) + P.selectRows(Calculation.Ret.NEW, i).mtimes(Q.selectRows(Calculation.Ret.NEW, j).transpose()).getAsDouble(0, 0);
                    predictionsMatrix.setAsDouble(predictionsIJ, i, j);
                }
            }
            Double cost = getMSE(ratingsMatrix, predictionsMatrix);
            Double itRunTime = (System.currentTimeMillis() - itStartTime) / 1000.0;
            logger.info("第 {} 次迭代,平均误差是 {}.本次迭代时间:{}.", it + 1, cost, itRunTime);

        }
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        logger.info("调用svd分解预测结束,用时 {} 秒.", runningTime);
        return predictionsMatrix;
    }
}
