package shenjing;

public class Matrix {
	private static final int OPERATION_ADD = 1;
	private static final int OPERATION_SUB = 2;
	private static final int OPERATION_MUL = 3;
	/**
	 * �жϾ����Ƿ���Խ��мӡ�����������
	 * @param x ����x
	 * @param y ����y
	 * @param type �������������
	 * @return ����������򷵻�true������false
	 */
	private static boolean legalOperation(double[][] x, double[][] y, int type) {
		boolean legal = true;
		if (type == OPERATION_ADD || type == OPERATION_SUB) {
			if (x.length != y.length || x[0].length != y[0].length) {
				legal = false;
			}
		} else if (type == OPERATION_MUL) {
			if (x[0].length !=y.length) {
				legal = false;
			}
		}
		return legal;
	}
	/**
	 * ����ļӷ�
	 * @param x ����x
	 * @param y ����y
	 * @return ����Ϸ������ؽ�������㲻�Ϸ��׳������쳣�Ĵ���
	 */
	public static double[][] addMtrx(double[][] x, double[][] y) {
		double[][] result = new double[x.length][x[0].length];
		if (legalOperation(x, y , OPERATION_ADD)) {
			for (int i = 0; i < x.length; i++) {
				for (int j = 0; j < x[0].length; j++) {
					result[i][j] = x[i][j] + y[i][j];
				}
			}
			return result;
		} else {
			throw new IllegalArgumentException("�����������в���");
		}
		
	}
	/**
	 * ����ļ���
	 * @param x ����x
	 * @param y ����y
	 * @return ����Ϸ������ؽ�������㲻�Ϸ��׳������쳣�Ĵ���
	 */
	public static double[][] subMtrx(double[][] x, double[][] y) {
		double[][] result = new double[x.length][x[0].length];
		if (legalOperation(x, y , OPERATION_SUB)) {
			for (int i = 0; i < x.length; i++) {
				for (int j = 0; j < x[0].length; j++) {
					result[i][j] = x[i][j] - y[i][j];
				}
			}
			return result;
		} else {
			throw new IllegalArgumentException("�����������в���");
		}
		
	}
	/**
	 * ����ĳ˷�
	 * @param x ����x x.length��ʾ������x[0].length��ʾ����
	 * @param y ����y
	 * @return ����Ϸ������ؽ�������㲻�Ϸ��׳������쳣�Ĵ���
	 */
	public static double[][] mulMtrx(double[][] x, double[][] y) {
		double[][] result = new double[x.length][y[0].length];
		if (legalOperation(x, y , OPERATION_MUL)) {
			for (int i = 0; i < x.length; i++) {
				for (int j = 0; j < y[0].length; j++) {
					double temp = 0;
					for(int m = 0; m < x[0].length; m++) {
						temp += x[i][m] * y[m][j];
					}
					result[i][j] = temp;
				}
			}
			return result;
		}
		throw new IllegalArgumentException("�����������в���");
	}
	/**
	 * 
	 * @param x ����x
	 * @return ����x��ת��
	 */
	public static double[][] tranposeMtrx(double[][] x) {
		double[][] result = new double[x[0].length][x.length];
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[0].length; j++) {
				result[j][i] = x[i][j];
			}
		}
		return result;
	}
	/**
	 * ��������
	 * @param x ����x
	 * @return ���Ϲ��򷵻�result���������Ϸ��׳������쳣�Ĵ���,�����������׳������쳣�Ĵ���
	 */
	public static double[][] convMtrx(double[][] x) {
		if (x.length == x[0].length) {
			double[][] result = new double[x.length][x[0].length];
			//temp_matrix��Ÿþ���͵�λ����
			double[][] temp_matrix = new double[x.length][x.length * 2];
			for (int i = 0; i < x.length; i++) {
				for (int j = 0; j < x[0].length; j++) {
					temp_matrix[i][j] = x[i][j];
				}
				//����λ����������
				for (int j = x[0].length; j < temp_matrix[0].length; j++) {
					if (j == i + x[0].length) {
						temp_matrix[i][j] = 1;
					} else {
						temp_matrix[i][j] = 0;
					}
				}
			}
			// �����б任
			for (int i=0; i < temp_matrix.length; i++) {
				if (temp_matrix[i][i] == 0) {
					for (int j = i+1; j < temp_matrix.length; j++) {
						if (temp_matrix[j][i] != 0) {
							double[] temp_vector = temp_matrix[i];
							temp_matrix[i] = temp_matrix[j];
							temp_matrix[j] = temp_vector;
							break;
						}
					}
				} 
				if (temp_matrix[i][i] == 0) {
					throw new IllegalArgumentException("���󲻿���");
				}
				double temp = temp_matrix[i][i];
				for (int j = 0; j < temp_matrix[0].length; j++) {
					temp_matrix[i][j] /= temp;
				}
				for (int j = 0; j < temp_matrix.length;j++) {
					temp = temp_matrix[j][i];
					for (int m = i; m < temp_matrix[0].length; m++) {
						if (j == i) continue;
						temp_matrix[j][m] -= temp * temp_matrix[i][m];
					}
				}
			}
			//��ֵ��result
			for (int i = 0; i < temp_matrix.length; i++) {
				for(int j = 0; j < temp_matrix.length; j++)
				result[i][j] = temp_matrix[i][j + temp_matrix.length];  
			} 
			return result;
		} else {
			throw new IllegalArgumentException("�����������в���");
		}
	}
    /**
     *���˾���
     */
    public static double[][] mulMtrx(double coef,double[][] a){
        double[][] res=new double[a.length][a[0].length];
        for(int i=0;i<a.length;i++){
            for(int j=0;j<a[0].length;j++){
                res[i][j]=coef*a[i][j];
            }
        }
        return res;
    }

    /**
     *����һ��i��j�еľ���Ԫ��Ϊnum
     */
    public static double[][] genNums(double num,int i,int j){
        double[][] res=new double[i][j];
        for(int m=0;m<i;m++){
            for(int n=0;n<j;n++){
                res[m][n]=num;
            }
        }
        return res;
    }
    /**
     * ����ȡ����ֵ
     */
    public static double[][] absMtrx(double[][] a){
        double[][] abs=new double[a.length][a[0].length];
        int i,j;
        for(i=0;i<a.length;i++){
            for(j=0;j<a[0].length;j++){
                abs[i][j]=Math.abs(a[i][j]);
            }
        }
        return abs;
    }
    /**
     * ��һ����ͬ��С,��Ԫ��ȫΪͬһ�����������ȥһ������
     */
    public static double[][] subMtrx(int num, double[][] a){
        double[][] res=new double[a.length][a[0].length];
        for(int i=0;i<a.length;i++){
            for(int j=0;j<a[0].length;j++){
                res[i][j]=1.0-a[i][j];
            }
        }
        return res;
    }
}
