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
import recommend.ItemBasedCF;
import recommend.ModelCF;
import recommend.SlopOne;
import recommend.UserBasedCF;

import static common.Utils.*;

public class Main {
    public static void main(String[] args) {
        Matrix ratingsMatrix = readFileAsRatingsMatrix("D:\\work\\liujm\\2017\\9\\20170911\\ml-100k\\ml-100k\\u.data");
        Matrix test = impulation(ratingsMatrix, "item");
//////        Double sim = getSimilarity(test.selectColumns(Calculation.Ret.NEW, 0).transpose(), test.selectColumns(Calculation.Ret.NEW, 0).transpose(), "else");
//////        System.out.println(sim);
//        ItemBasedCF itemBasedCF = new ItemBasedCF(test, "cosine");
//        SlopOne slopOne=new SlopOne(ratingsMatrix);
        ModelCF modelCF = new ModelCF(test);
//        Matrix predictionsMatrix = slopOne.CalcRatings();
//        Matrix predictionsMatrix = modelCF.SGD(10,10,1.0,0.1,0.001);
        Matrix predictionsMatrix = modelCF.SVD("userAverage");//.round(Calculation.Ret.NEW);
        System.out.println(predictionsMatrix.getRowCount());
        System.out.println(predictionsMatrix.getColumnCount());
        for (int i = 0; i < predictionsMatrix.getRowCount(); i++) {
            System.out.println(predictionsMatrix.selectRows(Calculation.Ret.NEW, i));
        }


    }
}
