package recommend;

import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;

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
    private Matrix ratingsMatrix;
    private Long userCounts;
    private Long itemCounts;

    public ModelCF(Matrix ratingsMatrix) {
        this.ratingsMatrix = ratingsMatrix;
        this.userCounts = ratingsMatrix.getRowCount();
        this.itemCounts = ratingsMatrix.getColumnCount();
    }

    public Matrix SGD(Integer K, Integer iterations, Double alpha, Double l, Double tol) {
        Matrix P = SparseMatrix.Factory.rand(userCounts, K);
        Matrix Q = SparseMatrix.Factory.rand(itemCounts, K);
        Matrix Qt = Q.transpose();

        for (int it = 1; it <= iterations; it++) {
            for (int i = 0; i < userCounts; i++) {
                for (int j = 0; j < itemCounts; j++) {
                    if (ratingsMatrix.getAsDouble(i, j) > 0) {
                        Double eij = ratingsMatrix.getAsDouble(i, j) - P.selectRows(Calculation.Ret.NEW, i).mtimes(Qt.selectColumns(Calculation.Ret.NEW, j)).getAsDouble(0, 0);
                        for (int k = 0; k < K; k++) {
                            Double Pik = alpha * (2 * eij * Qt.getAsDouble(k, j) - l * P.getAsDouble(i, k));
                            P.setAsDouble(P.getAsDouble(i, k) + Pik, i, j);
                            Double Qtkj = alpha * (2 * eij * P.getAsDouble(i, k) - l * Qt.getAsDouble(k, j));
                            Qt.setAsDouble(Qt.getAsDouble(k, j) + Qtkj, k, j);
                        }
                    }
                }
            }
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
            if (cost < tol)
                break;
        }
        return P.mtimes(Qt);
    }

}
