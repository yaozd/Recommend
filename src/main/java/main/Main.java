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
import recommend.UserBasedCF;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static common.Utils.*;
import static recommend.UserBasedCF.*;

public class Main {
    public static void main(String[] args) {
//        dense.setAsDouble(5.0, 2, 3);
//        System.out.println(dense);
//        Matrix sparse = SparseMatrix.Factory.zeros(4, 5);
//        Matrix sparseMatrix =SparseMatrix.Factory.rand(5,4);
//        sparse.setAsDouble(2.0, 0, 0);
//        sparse.setAsDouble(3.0,0,1);
//        System.out.println(sparse);
//        System.out.println(sparseMatrix);
//        System.out.println(sparse.mtimes(sparseMatrix));
//
//        System.out.println(sparse.sum(Calculation.Ret.NEW,0,true));
//        System.out.println(sparse.sum(Calculation.Ret.NEW,1,true));
//        long[] a = sparse.getSize();
//        for (Long i : a) {
//            System.out.println(i);
//        }
//        System.out.println(sparse.getSize(0));
//        System.out.println(sparse.getSize(1));
//        System.out.println(sparse.getAsDouble(0,0));
//        System.out.println(a);
        Matrix ratingsMatrix = readFileAsRatingsMatrix("D:\\work\\liujm\\2017\\9\\20170911\\ml-100k\\ml-100k\\u.data");
////////        Matrix itemsFeatureMatrix = readFileAsItemsFeatureMatrix("D:\\work\\liujm\\2017\\9\\20170911\\ml-100k\\ml-100k\\u.item");
////////        Long userCounts = ratingsMatrix.getSize(0);
////////        Long itemCounts = ratingsMatrix.getSize(1);
////////        Long featuresCounts = itemsFeatureMatrix.getSize(1);
////
        Matrix test = impulation(ratingsMatrix, "userAverage");
////        Double sim = getSimilarity(test.selectColumns(Calculation.Ret.NEW, 0).transpose(), test.selectColumns(Calculation.Ret.NEW, 0).transpose(), "else");
////        System.out.println(sim);
        UserBasedCF userBasedCF =new UserBasedCF(test,"cosine");
        Matrix predictionsMatrix=userBasedCF.CalcRatings();
        for(int i=0;i<predictionsMatrix.getRowCount();i++){
            System.out.println(predictionsMatrix.selectRows(Calculation.Ret.NEW,i));
        }




    }
}
