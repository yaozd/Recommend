package main;

/**
 * @version: 1.0
 * @author: Liujm
 * @site: https://github.com/liujm7
 * @contact: kaka206@163.com
 * @software: Idea
 * @date： 2017/9/30
 * @package_name: main
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.*;
import org.ujmp.core.calculation.Calculation;
import recommend.*;


import static common.Utils.*;

public class Main {
    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void printResult(Matrix predictionsMatrix) {
        /**
         * @Method_name: printResult
         * @Description: 循环输出结果矩阵
         * @Date: 2017/10/10
         * @Time: 15:15
         * @param: [predictionsMatrix]
         * @return: void
         **/
        logger.info("行数:{},列数:{}",predictionsMatrix.getRowCount(),predictionsMatrix.getColumnCount());
        for (int i = 0; i < predictionsMatrix.getRowCount(); i++) {
            System.out.println(predictionsMatrix.selectRows(Calculation.Ret.NEW, i));
        }
    }

    public static void testUserBasedCF(Matrix ratingsMatrix) {
        UserBasedCF userBasedCF = new UserBasedCF(ratingsMatrix, "cosine");
        Matrix predictionsMatrix = userBasedCF.CalcRatings();
        printResult(predictionsMatrix);

    }

    public static void testItemBasedCF(Matrix ratingsMatrix) {
        ItemBasedCF itemBasedCF = new ItemBasedCF(ratingsMatrix, "cosine");
        Matrix predictionsMatrix = itemBasedCF.CalcRatings();
        printResult(predictionsMatrix);
    }

    public static void testSlopOne(Matrix ratingsMatrix) {
        SlopOne slopOne = new SlopOne(ratingsMatrix);
        Matrix predictionsMatrix = slopOne.CalcRatings();
        printResult(predictionsMatrix);
    }

    public static void testModeCF(Matrix ratingsMatrix) {
        //**当alpha =0.008左右的时候才收敛 - 原因未知**
        ModelCF modelCF = new ModelCF(ratingsMatrix);
//        Matrix predictionsMatrix = modelCF.SVD("userAverage");
        Matrix predictionsMatrix = modelCF.SGD(20, 30, 0.008, 0.008, 0.001);
        printResult(predictionsMatrix);
    }

    public static void testALS(Matrix ratingsMatrix){
        ModelCF modelCF = new ModelCF(ratingsMatrix);
        Matrix predictionsMatrix = modelCF.ALS(20,10,0.01,0.01);
        printResult(predictionsMatrix);
    }

    public static void testSVD(Matrix ratingsMatrix) {
        ModelCF modelCF = new ModelCF(ratingsMatrix);
        Matrix predictionsMatrix = modelCF.SVD("userAverage", 50);
//        printResult(predictionsMatrix);
    }


    public static void testCBF(Matrix ratingsMatrix, Matrix itemsFeatureMatrix) {
        CBF cbf = new CBF(ratingsMatrix, itemsFeatureMatrix, "userAverage");
        Matrix predictionsMatrix = cbf.CBFAverage();
        printResult(predictionsMatrix);
    }

    public static void testCBFRegression(Matrix ratingsMatrix, Matrix itemsFeatureMatrix) {
        CBF cbf = new CBF(ratingsMatrix, itemsFeatureMatrix, "none");
        Matrix predictionsMatrix = cbf.CBFRegression(1.5, 0.1, 20, 0.001);
//        printResult(predictionsMatrix);
    }

    public static void testHybridCbfCF(Matrix ratingsMatrix, Matrix itemsFeatureMatrix) {
        HybridCbfCF hybridCbfCF = new HybridCbfCF(ratingsMatrix, itemsFeatureMatrix, "userAverage");
        Matrix predictionsMatrix = hybridCbfCF.CalcRatings();
        printResult(predictionsMatrix);
    }

    public static void main(String[] args) {
        Matrix ratingsMatrix = readFileAsRatingsMatrix("D:\\work\\liujm\\2017\\9\\20170911\\ml-100k\\ml-100k\\u.data");
//        Matrix itemsFeatureMatrix = readFileAsItemsFeatureMatrix("D:\\work\\liujm\\2017\\9\\20170911\\ml-100k\\ml-100k\\u.item");
        testALS(ratingsMatrix);

    }


}
