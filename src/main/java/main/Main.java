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

import org.ujmp.core.*;
import org.ujmp.core.calculation.Calculation;
import recommend.*;

import java.util.ArrayList;
import java.util.Arrays;

import static common.Utils.*;

public class Main {
    public static void printResult(Matrix predictionsMatrix) {
        System.out.println(predictionsMatrix.getRowCount());
        System.out.println(predictionsMatrix.getColumnCount());
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
        ModelCF modelCF = new ModelCF(ratingsMatrix);
        Matrix predictionsMatrix = modelCF.SVD("userAverage");
        printResult(predictionsMatrix);
    }


    public static void testCBF(Matrix ratingsMatrix, Matrix itemsFeatureMatrix) {
        CBF cbf = new CBF(ratingsMatrix, itemsFeatureMatrix, "userAverage");
        Matrix predictionsMatrix = cbf.CBFAverage();
        printResult(predictionsMatrix);
    }

    public static void testHybridCbfCF(Matrix ratingsMatrix, Matrix itemsFeatureMatrix) {
        HybridCbfCF hybridCbfCF = new HybridCbfCF(ratingsMatrix, itemsFeatureMatrix, "userAverage");
        Matrix predictionsMatrix = hybridCbfCF.CalcRatings();
        printResult(predictionsMatrix);
    }

    public static void main(String[] args) {
        Matrix ratingsMatrix = readFileAsRatingsMatrix("D:\\work\\liujm\\2017\\9\\20170911\\ml-100k\\ml-100k\\u.data");
        Matrix itemsFeatureMatrix = readFileAsItemsFeatureMatrix("D:\\work\\liujm\\2017\\9\\20170911\\ml-100k\\ml-100k\\u.item");
        testHybridCbfCF(ratingsMatrix, itemsFeatureMatrix);
    }

}
