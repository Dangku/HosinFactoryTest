package com.android.factorytest;



import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class Compass extends BaseActivity implements SensorEventListener{
	
	 public static final String TAG = "MSensor";
	    float[] accelermeter_value;
	    Sensor accelerometerSensor;
	    private CompassView mPointer;
	    SensorManager mSensorManager;
	    private TextView mTextview_data;
	    
	    
	    Sensor magneticSensor;
	    float[] magntiude_value;
	    protected final Handler mHandler = new Handler();  
	    
	    //���ټ����ݡ�
		private float[] mGData = new float[3];
		//���������ݡ�
		private float[] mMData = new float[3];
		//��ת����(rotation matrix)�����ڼ����ֻ���������ڵ�������ϵ����ת�Ƕȡ������ֻ������Ǻ��Ż������ŵġ�
		//��ӳ�����ֻ�������ڷ�λ�á���������������������������û�������ǵĻ��������ڼ��ټƺ��������ٶ�Ҳ���ԣ�ֻ�Ǿ��Ȳ���ǿ���ˡ�
		private float[] mR = new float[16];
		//��б����(inclination matrix)�����ڼ����ֻ���λ�������ֻ���ͷ�����ڳ��ϻ��ǳ����������һ��ƽ���ϵĶ��������ĸ�������Եġ�
		private float[] mI = new float[16];
		//������յ���ת�Ƕ�����
		private float[] mOrientation = new float[3];
		private float yaw = 0.0f;
		private AccelerateInterpolator mInterpolator;// �����ӿ�ʼ���������仯����һ�����ٵĹ���,����һ����������
		private float mDirection;// ��ǰ���㷽��  
	    private float mTargetDirection;// Ŀ�긡�㷽��  
	    private final float MAX_ROATE_DEGREE = 1.0f;// �����תһ�ܣ���360��  
	    private boolean mStopDrawing;// �Ƿ�ָֹͣ������ת�ı�־λ  
	    private boolean upeast , upwest,faceeast ,facewest =false;

		
	    public Compass() {
	        mSensorManager = null;
	        magneticSensor = null;
	        accelerometerSensor = null;
	        
	    }
	    
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
	        super.onCreate(savedInstanceState);
	       
	        mTextview_data = (TextView)findViewById(R.id.xyz);
	        mPointer = (CompassView)findViewById(R.id.compass_image);
	        mPointer.setImageResource(R.drawable.compass);
	       
	        mInterpolator = new AccelerateInterpolator();// ʵ�������ٶ�������  
	        mDirection = 0.0f;// ��ʼ����ʼ����  
	        mTargetDirection = 0.0f;// ��ʼ��Ŀ�귽��  
	        mStopDrawing = true;  
	        initsensor();
	        
	        
	        mSuccess.setEnabled(false);
	        setRetestBtnVisible(false);
		}
	
	
    private void initsensor() {
        Log.i("MSensor", "initsensor");
        mSensorManager = (SensorManager)getSystemService("sensor");
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
	
	
    public void onPause() {
        Log.i("MSensor", "onPause");
        super.onPause();
        mStopDrawing = true;  
        mSensorManager.unregisterListener(this, magneticSensor);
        mSensorManager.unregisterListener(this, accelerometerSensor);
    }
    
    public void onResume() {
        Log.i("MSensor", "onResume");
        super.onResume();
        mSensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        mStopDrawing = false;  
        mHandler.postDelayed(mCompassViewUpdater, 20);// 20����ִ��һ�θ���ָ����ͼƬ��ת  
    }

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
			int type = event.sensor.getType();    
			if (type == Sensor.TYPE_ACCELEROMETER) {       
				for (int i = 0; i < 3; i++)     {
					mGData[i] = event.values[i];
					Log.d(TAG, "mGData[i] = " + mGData[i]);
				}
				
				// �ж�ͨ����ť��״̬
	            if(event.values[SensorManager.DATA_Z]>9){
	            	if(yaw >170){
	                	upeast = true;
	                	Log.d("shihongwei", "upeast = true;");
	                }if(yaw<-170){
	                	upwest =true;
	                	Log.d("shihongwei", "upwest =true;");
	                }
				}
	            if(event.values[SensorManager.DATA_Z]<2){
	            	if(yaw >170){
	                	faceeast = true;
	                	Log.d("shihongwei", "faceeast =true;");
	                }
	            	if(yaw<-170){
	                	facewest =true;
	                	Log.d("shihongwei", "facewest =true;");
	                }
				}
	            
	            if(upeast && upwest && faceeast && facewest){
	            	Log.d("shihongwei", "okButton.setEnabled(true);");
	            	mSuccess.setEnabled(true);
	            }
				
			} 
			else if (type == Sensor.TYPE_MAGNETIC_FIELD) {      
				for (int i = 0; i < 3; i++)     
					mMData[i] = event.values[i];
				
			} else {        
						// we should not be here.       
						return;      
			}      
			//��һ����Ŀ���ǻ�ȡmR��mI�������� 
			if(!SensorManager.getRotationMatrix(mR, mI, mGData, mMData)){
				Log.d(TAG, "failure");
				return ;
			}else{
				for (int i = 0; i < 16; i++)     {
					Log.d(TAG, "mR[i] = " + mR[i]);
				}
			}
			//�þ���mR��ȡ�ֻ�����ת�Ƕȡ�     
			SensorManager.getOrientation(mR, mOrientation);
			//�þ���mI��ȡ�ֻ��ڶ��������ĸ������ϵļнǡ�   
			float incl = SensorManager.getInclination(mI);      
			
			final float rad2deg = (float)(180.0f / Math.PI);      
			// ����ת�� ��Z����ת�Ƕ�
			yaw =  (mOrientation[0] * rad2deg);
			// ��β�̽� ��X����ת�Ƕ�
			float pitch = (mOrientation[1] * rad2deg);
			// ����ת�� ��Y����ת�Ƕ�
			float roll = (mOrientation[2] * rad2deg);
			
			float dincl = (incl * rad2deg);
			
			Log.d("Compass", "yaw: " + yaw +
					"  pitch: " + pitch +
					"  roll: " + roll + 
					"  incl: " + dincl);
			
			mTextview_data.setText("X degree =" + yaw + "\nY degree = " + pitch + "\nZ degree = " + roll );
			//add
			float direction = yaw * -1.0f;  
            mTargetDirection = normalizeDegree(direction);// ��ֵ��ȫ�ֱ�������ָ������ת  
            
            
}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		 Log.i("MSensor", "onAccuracyChanged");

	}
	
	 // ����Ǹ���ָ������ת���̣߳�handler�����ʹ�ã�ÿ20�����ⷽ��仯ֵ����Ӧ����ָ������ת  
    protected Runnable mCompassViewUpdater = new Runnable() {  
        @Override  
        public void run() {  
            if (mPointer != null && !mStopDrawing) {  
                if (mDirection != mTargetDirection) {  
  
                    // calculate the short routine  
                    float to = mTargetDirection;  
                    if (to - mDirection > 180) {  
                        to -= 360;  
                    } else if (to - mDirection < -180) {  
                        to += 360;  
                    }  
  
                    // limit the max speed to MAX_ROTATE_DEGREE  
                    float distance = to - mDirection;  
                    if (Math.abs(distance) > MAX_ROATE_DEGREE) {  
                        distance = distance > 0 ? MAX_ROATE_DEGREE  
                                : (-1.0f * MAX_ROATE_DEGREE);  
                    }  
  
                    // need to slow down if the distance is short  
                    mDirection = normalizeDegree(mDirection  
                            + ((to - mDirection) * mInterpolator  
                                    .getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f  
                                            : 0.3f)));// ����һ�����ٶ���ȥ��תͼƬ����ϸ��  
                    mPointer.updateDirection(mDirection);// ����ָ������ת  
                }  
               
                mHandler.postDelayed(mCompassViewUpdater, 20);// 20���׺�����ִ���Լ����ȶ�ʱ����  
            }  
        }  
    };  
    
    // �������򴫸�����ȡ��ֵ  
    private float normalizeDegree(float degree) {  
        return (degree + 720) % 360;  
    }  


	@Override
	void setLayout() {
		// TODO Auto-generated method stub
		 setContentView(R.layout.compass);
	}

	@Override
	void retestOnClick() {
		// TODO Auto-generated method stub
		
	}
	
}
