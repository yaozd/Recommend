package recommend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.Matrix;


import java.util.HashMap;


import static org.ujmp.core.util.MathUtil.round;

import static common.Utils.*;

/**
 * @version: 1.0
 * @author: Liujm
 * @site: https://github.com/liujm7
 * @contact: kaka206@163.com
 * @software: Idea
 * @date： 2017/10/14
 * @package_name: recommend
 */
public class BaselinePredictor {
    final static Logger logger = LoggerFactory.getLogger(BaselinePredictor.class);
    private Matrix ratingsMatrix;
    private Long userCounts;
    private Long itemCounts;
    private HashMap<Integer, Double> rowDiffMap = new HashMap<Integer, Double>();
    private HashMap<Integer, Double> columnDiffMap = new HashMap<Integer, Double>();
    private Double allMeanValue;

    public BaselinePredictor(Matrix ratingsMatrix, Integer lambda1, Integer lambda2) {
        logger.info("BaselinePredictor基准预测 初始化");
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
        this.ratingsMatrix = ratingsMatrix;
        this.allMeanValue = getAllMeanValue(ratingsMatrix);
        this.columnDiffMap = getColumnDiffMap(ratingsMatrix, lambda1);
        this.rowDiffMap = getRowDiffMap(ratingsMatrix, lambda2, columnDiffMap);
        logger.info("BaselinePredictor基准预测 初始化完成");
    }


    public Matrix CalcRatings() {
        logger.info("调用BaselinePredictor基准预测 开始");
        long startTime = System.currentTimeMillis();
        Matrix predictionsMatrix = Matrix.Factory.zeros(userCounts, itemCounts);
        for (int i = 0; i < userCounts; i++) {
            Double diffI = rowDiffMap.get(i);
            for (int j = 0; j < itemCounts; j++) {
                Double diffJ = columnDiffMap.get(j);
                predictionsMatrix.setAsDouble(allMeanValue + diffI + diffJ, i, j);
            }
        }
        Double runningTime = (System.currentTimeMillis() - startTime) / 1000.0;
        Double cost = getMSE(ratingsMatrix, predictionsMatrix);
        logger.info("调用BaselinePredictor基准预测结束,用时 {} 秒,误差:{}.", runningTime, cost);
        return predictionsMatrix;
    }


}
