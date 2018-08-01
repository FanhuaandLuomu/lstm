package shenjing;

import java.util.Random;
import static shenjing.Matrix.subMtrx;
import static shenjing.Matrix.mulMtrx;
import static shenjing.Matrix.addMtrx;
import static shenjing.Matrix.tranposeMtrx;

import static java.lang.Math.pow;

public class Lstm {
    public Lstm() {
    }

    //ÿ�����볤�ȣ���������Ԫ�ĸ���
    public int inputWidth;
    //����ÿ���������Ԫ��Ҫ�ݹ����Ĵ���
    public int stateWidth;
    //ѧϰ�ʣ�[0,1]
    public double learningRate;
    //ʱ��
    public int times;
    //��ʱ�̵ĵ�Ԫ״̬����c
    public double[][][] cVecs;
    //��ʱ�̵��������h
    public double[][][] hVecs;
    //��ʱ�̵�����������f
    public double[][][] fVecs;
    //��ʱ�̵�������i
    public double[][][] iVecs;
    //��ʱ�̵������o
    public double[][][] oVecs;
    //��ʱ�̵ļ�ʱ״̬ct
    public double[][][] ctVecs;

    //��ʱ�̵���������
    public double[][][] deltaVecsH;
    //��ʱ�̵�����������
    public double[][][] deltaVecsO;
    //��ʱ�̵������������
    public double[][][] deltaVecsI;
    //��ʱ�̵������������
    public double[][][] deltaVecsF;
    //��ʱ�̼�ʱ��������
    public double[][][] deltaVecsCt;

    //������Ȩ�ؾ���Wfh,Wfx,bf
    public double[][] Wfh, Wfx, bf;
    //������Ȩ�ؾ���Wih,Wix,bi
    public double[][] Wih, Wix, bi;
    //�����Ȩ�ؾ���Woh,Wox,bo
    public double[][] Woh, Wox, bo;
    //��Ԫ״̬Ȩ�ؾ���Wch,Wcx,bc
    public double[][] Wch, Wcx, bc;

    //������Ȩ���ݶȾ���
    public double[][] WfhGrad,WfxGrad,bfGrad;
    //������Ȩ���ݶȾ���
    public double[][] WihGrad,WixGrad,biGrad;
    //�����Ȩ���ݶȾ���
    public double[][] WohGrad,WoxGrad,boGrad;
    //��Ԫ״̬Ȩ���ݶȾ���
    public double[][] WchGrad,WcxGrad,bcGrad;

    //Ҫǰ�����Ĵ���
    private int forwardTimes;

    /**
     * ====================public����================================
     **/
    public Lstm(int inputWidth, int stateWidth, double learningRate,int forwardTimes) {
        this.inputWidth = inputWidth;
        this.stateWidth = stateWidth;
        this.learningRate = learningRate;
        this.times = 0;
        this.forwardTimes=forwardTimes;

        cVecs=new double[this.forwardTimes +1][][];
        hVecs=new double[this.forwardTimes +1][][];
        fVecs=new double[this.forwardTimes +1][][];
        iVecs=new double[this.forwardTimes +1][][];
        oVecs=new double[this.forwardTimes +1][][];
        ctVecs=new double[this.forwardTimes +1][][];

        //������ʼ��
        this.cVecs[0] = initStateVec();
        this.hVecs[0] = initStateVec();
        this.fVecs[0] = initStateVec();
        this.iVecs[0] = initStateVec();
        this.oVecs[0] = initStateVec();
        this.ctVecs[0] = initStateVec();
        //Ȩ�ؾ����ʼ��
        double[][][] res = initWeightMatrix();
        Wfh = res[0];
        Wfx = res[1];
        bf = res[2];
        res = initWeightMatrix();
        Wih = res[0];
        Wix = res[1];
        bi = res[2];
        res = initWeightMatrix();
        Woh = res[0];
        Wox = res[1];
        bo = res[2];
        res = initWeightMatrix();
        Wch = res[0];
        Wcx = res[1];
        bc = res[2];

    }

    /**
     * ǰ������㷨
     */
    public void forward(double[][] x)  throws IllegalArgumentException{
        this.times+=1;
        //���������ŵ�ֵ
        double[][] fg=calculateGate(x,this.Wfx,this.Wfh,this.bf,SigmoidActivator.getInstance());
        fVecs[times]=fg;
        //���������ŵ�ֵ
        double[][] ig=calculateGate(x,this.Wix,this.Wih,this.bi,SigmoidActivator.getInstance());
        iVecs[times]=ig;
        //��������ŵ�ֵ
        double[][] og=calculateGate(x,this.Wox,this.Woh,this.bo,SigmoidActivator.getInstance());
        oVecs[times]=og;
        //���㼴ʱ״̬
        double[][] ct=calculateGate(x,this.Wcx,this.Wch,this.bc,TanhActivator.getInstance());
        ctVecs[times]=ct;
        //
        double[][] c= addMtrx(mulMtrx(fg,cVecs[times-1]),mulMtrx(ig,ct));
        cVecs[times]=c;
        //���
        double[][] h= mulMtrx(og,TanhActivator.getInstance().forward(c));
        hVecs[times]=h;
    }

    /**
     * ���򴫲�����
     */
    public void backward(double[][] x, double[][]deltaH,Activator activator){
        //����delta
        calculateDelta(deltaH,activator);
        //�����ݶ�
        calculateGradient(x);
    }


    /**
     * ��ո���ʱ̬�洢����
     */
    public void resetStates(){
        this.times = 0;
        cVecs=new double[this.forwardTimes +1][][];
        hVecs=new double[this.forwardTimes +1][][];
        fVecs=new double[this.forwardTimes +1][][];
        iVecs=new double[this.forwardTimes +1][][];
        oVecs=new double[this.forwardTimes +1][][];
        ctVecs=new double[this.forwardTimes +1][][];

        //������ʼ��
        this.cVecs[0] = initStateVec();
        this.hVecs[0] = initStateVec();
        this.fVecs[0] = initStateVec();
        this.iVecs[0] = initStateVec();
        this.oVecs[0] = initStateVec();
        this.ctVecs[0] = initStateVec();
    }

    /**
     * �����ݶ��½�����Ȩ��
     */
    public void update(){
        this.Wfh=subMtrx(Wfh,mulMtrx(this.learningRate,this.WfhGrad));
        this.Wfx=subMtrx(Wfx,mulMtrx(this.learningRate,this.WfxGrad));
        this.bf=subMtrx(bf,mulMtrx(this.learningRate,this.bfGrad));

        this.Wih=subMtrx(Wih,mulMtrx(this.learningRate,this.WihGrad));
        this.Wix=subMtrx(Wix,mulMtrx(this.learningRate,this.WixGrad));
        this.bi=subMtrx(bi,mulMtrx(this.learningRate,this.biGrad));

        this.Woh=subMtrx(Woh,mulMtrx(this.learningRate,this.WohGrad));
        this.Wox=subMtrx(Wox,mulMtrx(this.learningRate,this.WoxGrad));
        this.bo=subMtrx(bo,mulMtrx(this.learningRate,this.boGrad));

        this.Wch=subMtrx(Wch,mulMtrx(this.learningRate,this.WchGrad));
        this.Wcx=subMtrx(Wcx,mulMtrx(this.learningRate,this.WcxGrad));
        this.bc=subMtrx(bc,mulMtrx(this.learningRate,this.bcGrad));
    }

    /**
     * ========================private����===========================
     **/

    /**
     * ��ʼ������״̬������
     */
    private double[][] initStateVec() {
        double[][] vec = new double[this.stateWidth][1];
        return vec;
    }

    /**
     * ��ʼ��Ȩ�ؾ���
     */
    private double[][][] initWeightMatrix() {
        double max = pow(10, -4);
        double min = (-1) * max;
        double[][] Wh = new double[this.stateWidth][this.stateWidth];
        double[][] Wx = new double[this.stateWidth][this.inputWidth];
        double[][][] res = new double[3][][];
        int i = 0, j = 0;
        for (i = 0; i < this.stateWidth; i++) {
            for (j = 0; j < this.stateWidth; j++) {
                Wh[i][j] = min + ((max - min) * new Random().nextDouble());
            }
        }
        for (i = 0; i < this.stateWidth; i++) {
            for (j = 0; j < this.inputWidth; j++) {
                Wx[i][j] = min + ((max - min) * new Random().nextDouble());
            }
        }

        res[0] = Wh;
        res[1] = Wx;
        res[2] = new double[this.stateWidth][1];
        return res;
    }

    /**
     *��������ŵ�ֵ
     */
    private double[][] calculateGate(double[][] x ,double[][] Wx, double[][] Wh, double[][] b,Activator activator){
        //��ȡ�ϴε�LSTM���
        double[][] h=this.hVecs[this.times-1];
        double[][] net= addMtrx(addMtrx( mulMtrx(Wh,h), mulMtrx(Wx,x)),b);
        double[][] gate=activator.forward(net);
        return gate;
    }

    /**
     *���������
     */
    private void calculateDelta(double[][]deltaH,Activator activator){
        //��ʼ������ʱ�̵������
        deltaVecsH= initDelta();
        deltaVecsO= initDelta();
        deltaVecsI= initDelta();
        deltaVecsF = initDelta();
        deltaVecsCt= initDelta();
        //�������һ�㴫�������ĵ�ǰʱ�̵������
        deltaVecsH[deltaVecsH.length-1]=deltaH;
        //��������ÿ��ʱ�̵������
        for(int k=this.times;k>=1;k--){
            calculateDeltaK(k);
        }
    }

    /**
     *����kʱ�̵�deltaH����dealta f i o ct��k-1ʱ��deltaH
     */
    private void calculateDeltaK(int k){
        double[][] ig=iVecs[k];
        double[][] og=oVecs[k];
        double[][] fg=fVecs[k];
        double[][] ct=ctVecs[k];
        double[][] c=cVecs[k];
        double[][] cPrev=cVecs[k-1];
        double[][] tanhC=TanhActivator.getInstance().forward(c);
        double[][] deltaK=deltaVecsH[k];

        double[][] deltaO,deltaF,deltaI,deltaCt,deltaPrevH;
        double[][] kog,tcc;
        kog= mulMtrx(deltaK,og);
        tcc= subMtrx(1, mulMtrx(tanhC,tanhC));
        deltaO= mulMtrx(mulMtrx(deltaK,tanhC),SigmoidActivator.getInstance().backward(og));
        deltaF= mulMtrx(mulMtrx(mulMtrx(kog,tcc),cPrev),SigmoidActivator.getInstance().backward(fg));
        deltaI= mulMtrx(mulMtrx(mulMtrx(kog,tcc),ct),SigmoidActivator.getInstance().backward(ig));
        deltaCt= mulMtrx(mulMtrx(mulMtrx(kog,tcc),ig),SigmoidActivator.getInstance().backward(ct));
        deltaPrevH=
                tranposeMtrx(
                        addMtrx(
                                addMtrx(
                                        mulMtrx(tranposeMtrx(deltaO),this.Woh),
                                        mulMtrx(tranposeMtrx(deltaI),this.Wih)
                                ),
                                addMtrx(
                                        mulMtrx(tranposeMtrx(deltaF),this.Wfh),
                                        mulMtrx(tranposeMtrx(deltaCt),this.Wch)
                                )
                        )
                );
        //����ȫ��deltaֵ
        deltaVecsH[k-1]=deltaPrevH;
        deltaVecsF[k]=deltaF;
        deltaVecsI[k]=deltaI;
        deltaVecsO[k]=deltaO;
        deltaVecsCt[k]=deltaCt;
    }

    /**
     * ��ʼ�������
     */
    private double[][][] initDelta(){
        double[][][] deltas=new double[times+1][][];
        for(int i=0;i<times+1;i++){
            deltas[i]=new double[this.stateWidth][1];
        }
        return deltas;
    }

    /**
     * �����ݶ�
     */
    private void calculateGradient(double[][] x){
        // ��ʼ��������Ȩ���ݶȾ����ƫ����
        double[][][] res=initWeightGradientMatrix();
        WfhGrad=res[0];
        WfxGrad=res[1];
        bfGrad=res[2];
        
        res=initWeightGradientMatrix();
        WihGrad=res[0];
        WixGrad=res[1];
        biGrad=res[2];

        res=initWeightGradientMatrix();
        WohGrad=res[0];
        WoxGrad=res[1];
        boGrad=res[2];

        res=initWeightGradientMatrix();
        WchGrad=res[0];
        WcxGrad=res[1];
        bcGrad=res[2];

        //�������һ�����h��Ȩ���ݶ�calc_gradient_mat
        for(int t=times;t>0;t--){	
            double[][] fG,bfG,
                    iG,biG,
                    oG,boG,
                    cG,bcG;
            double[][] traspsdPrvH= tranposeMtrx(hVecs[t-1]);
            fG= mulMtrx(this.deltaVecsF[t], traspsdPrvH);
            bfG=this.deltaVecsF[t];

            iG= mulMtrx(this.deltaVecsI[t],traspsdPrvH);
            biG=this.deltaVecsI[t];

            oG= mulMtrx(this.deltaVecsO[t],traspsdPrvH);
            boG=this.deltaVecsO[t];

            cG= mulMtrx(this.deltaVecsCt[t],traspsdPrvH);
            bcG=this.deltaVecsCt[t];

            //ʵ���ݶ��Ǹ���ʱ���ݶ�֮��
            WfhGrad= addMtrx(this.WfhGrad,fG);
            bfGrad= addMtrx(this.bfGrad,bfG);

            WihGrad= addMtrx(this.WihGrad,iG);
            biGrad= addMtrx(this.biGrad,biG);

            WohGrad= addMtrx(this.WohGrad,oG);
            boGrad= addMtrx(this.boGrad,boG);

            WchGrad= addMtrx(this.WchGrad,cG);
            bcGrad= addMtrx(this.bcGrad,bcG);

        }

        //����Ա�������x��Ȩ���ݶ�
        double[][] xt= tranposeMtrx(x);
        this.WfxGrad= mulMtrx(this.deltaVecsF[deltaVecsF.length-1],xt);
        this.WixGrad= mulMtrx(this.deltaVecsI[deltaVecsI.length-1],xt);
        this.WoxGrad= mulMtrx(this.deltaVecsO[deltaVecsO.length-1],xt);
        this.WcxGrad= mulMtrx(this.deltaVecsCt[deltaVecsCt.length-1],xt);
    }

    /**
     * ��ʼ���ݶ�Ȩ�ؾ���
     */
    private double[][][] initWeightGradientMatrix() {
        double[][] Wh = new double[this.stateWidth][this.stateWidth];
        double[][] Wx = new double[this.stateWidth][this.inputWidth];
        double[][][] res = new double[3][][];
        res[0] = Wh;
        res[1] = Wx;
        res[2] = new double[this.stateWidth][1];
        return res;
    }

}
