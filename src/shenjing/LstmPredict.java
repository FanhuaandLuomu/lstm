package shenjing;
import static java.lang.Math.pow;
import static shenjing.Matrix.absMtrx;
import static shenjing.Matrix.subMtrx;
import static shenjing.Matrix.mulMtrx;
import static shenjing.Matrix.tranposeMtrx;

import java.time.Year;
import java.util.Arrays;
import java.util.Random;

import javax.naming.spi.DirStateFactory.Result;
import javax.print.DocFlavor.INPUT_STREAM;

import shenjing.IdentityActivator;

public class LstmPredict {
	/**
	 * ��һ�����������ѵ��һ����������һ��lstmԤ��
	 * @param x ��������
	 * @param t	x[i]ÿ��������Ӧ��ʵ�����t[i];
	 * @param iter �������´���
	 * @param predictTime Ԥ��ʱ�䳤��
	 * @param hideNum ���ز����
	 * @return
	 */
	public static double[] singleLstmPrediction(double[][] x, double[][] t,int iter, int predictTime, int hideNum, double[] history) {
		// ���ά�����ж����1�������ֱ��Ԥ������ҪԤ���������
		int outputNum = t[0].length;
		// ����Ȩֵ����,y = Wx * x, ��Ԫ��������Ȩֵ����������������ټ�Ȩֵ����û������
		// double[][] Wx_input = new double[hideNum][x[0].length];
		// ������Ԫ���Ȩֵ����,y = Wy * x;
		// �����ԪҲ��Ȩֵ��������ټ�Ȩֵ����Ҳû������
		// double[][] Wy_output = new double[outputNum][hideNum];
		// ���������
		double[] output_array = new double[predictTime];
		// ���ز����������
		// double[] hide_error = new double[hideNum];
		// ��������
		// double[] input_error = new double[hideNum];
		// ���ز���lstm��Ԫ
		Lstm[] lstms = new Lstm[hideNum];
			
		for (int i = 0; i < hideNum; i++) {
	        // ��һ�������������ά������������Ԫ����
			// �ڶ��������������ά��,�������Ԫ��������Ϊ1������Ϊ7
	        // ������������ѧϰ��
			// ���ĸ�������ÿ�ε�����Ҫǰ�����������
			lstms[i] = new Lstm(x[0].length, 1, 0.0000000000000001, x.length + predictTime);
		}
		
		// ʹ������ݶ��½������¾�����������������ڵ�������ֹͣ����,�������ݼ�δ����
		for (int i = 0; i < iter; i++) {
			// ��ÿ�����ݽ��е������£�ĳʱ��ֵ��Ҫ��ǰ����m��
			for (int m = 0; m < x.length; m++) {
				System.out.println(m);
				for (int n = 0; n < lstms.length; n++) {
					lstms[n].resetStates();
				}
				for (int n = 0; n < m; n++) {
					for (int j = 0; j < lstms.length; j++) {
						double[][] input = {x[n]};
						// double[][] Wx_input_one = {Wx_input[j]};
						// input = mulMtrx(Wx_input_one, tranposeMtrx(input));
						input = tranposeMtrx(input);
						lstms[j].forward(input);
					}					
				}
	            // ������������,�����������
	            double[][] real_output = {t[m]};
	            double[][] output = new double[outputNum][1];
	            for (int j = 0; j < outputNum; j++) {
	            	for (int j2 = 0; j2 < lstms.length; j2++) {
	            		output[j][0] += lstms[j2].hVecs[m][j][0];
					}
				}
	            double[][] input_x = {x[m]};
	            double[][] delta_h = absMtrx(subMtrx(tranposeMtrx(real_output),output));
	            //���򴫲�����
	            for (int j = 0; j < lstms.length; j++) {
	            	System.out.println("���򴫲�");
					lstms[j].backward(input_x, delta_h, IdentityActivator.getInstance());
				}
	            //�����ݶ��½�����Ȩ��
	            for (int j = 0; j < lstms.length; j++) {
	            	System.out.println("�ݶȸ���");
	            	lstms[j].update();
				}
			}
		}
		for (int i = 0; i < lstms.length; i++) {
			lstms[i].resetStates();
		}
		double[] predict_input = new double[history.length + predictTime];
		double[] predict = new double[predictTime];
		for (int i = 0; i < history.length; i++) {
			predict_input[i] = history[i];
		}
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < lstms.length; j++) {
				double[][] input_x = {x[i]};
				input_x = tranposeMtrx(input_x);
				lstms[j].forward(input_x);
			}
		}
		for (int i = x.length; i < x.length+predictTime; i++) {
			for (int j = 0; j < lstms.length; j++) {
				double[] input = new double[x[0].length];
				for (int k = 0; k < input.length; k++) {
					input[k] = predict_input[i + k];
				}
				double[][] input_x = {input};
				input_x = tranposeMtrx(input_x);
				lstms[j].forward(input_x);
			}
			for (int j = 0; j < lstms.length; j++) {
				
				predict_input[i + x[0].length] += lstms[j].hVecs[i][0][0];
			}
			/*if (predict_input[i + x[0].length] < 0.0) {
				predict_input[i + x[0].length] = 0.0;
			} else {
				predict_input[i + x[0].length] = Math.round(predict_input[i + x[0].length]);
			}*/
			predict[i - x.length] = predict_input[i + x[0].length];

		}
		return predict;		
	}
	public static void main(String[] args) {
		double[][] x = new double[][]{{0.0},{4.0},{1.0}, {0.0}, {0.0},{0.0},{ 0.0}, {5.0}, {8.0}, {5.0}, {0.0}, {17.0}, {0.0}, {1.0},{ 16.0}, {5.0}, {7.0}, {15.0}, {0.0}, {0.0}, {1.0}, {0.0}, {5.0}, {3.0}, {12.0}, {0.0}}; 
		double[][] t = new double[][]{{4.0},{1.0}, {0.0}, {0.0},{0.0},{ 0.0}, {5.0}, {8.0}, {5.0}, {0.0}, {17.0}, {0.0}, {1.0},{ 16.0}, {5.0}, {7.0}, {15.0}, {0.0}, {0.0}, {1.0}, {0.0}, {5.0}, {3.0}, {12.0}, {0.0},{0.0}}; 
		double[] history = new double[]{0.0, 4.0, 1.0 ,0.0, 0.0, 0.0, 0.0, 5.0, 8.0, 5.0 ,0.0 ,17.0 ,0.0 ,1.0 ,16.0 ,5.0 ,7.0 ,15.0 ,0.0 ,0.0 ,1.0 ,0.0 ,5.0 ,3.0 ,12.0 ,0.0 ,0.0};
		double[] result = singleLstmPrediction(x, t, 1, 7, 8, history);
		System.out.println(Arrays.toString(result));
	}
}
