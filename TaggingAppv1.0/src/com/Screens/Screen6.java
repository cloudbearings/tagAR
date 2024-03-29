/**
 *  
 *tagAR is a tagging application developed for the Android Platform
 *Copyright (C) 2012  Mustafa Neguib, MN Tech Solutions
 *  
 *This file is part of tagAR.
 *
 *tagAR is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *the Free Software Foundation, either version 3 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *You can contact the developer/company at the following:
 *
 *Phone: 00923224138957
 *Website: www.mntechsolutions.net
 *Email: support@mntechsolutions.net , mustafaneguib@mntechsolutions.net
 *
 */

/*
 * @author: Mustafa Neguib
 * @company: MN Tech Solutions
 * @applicationName: tagAR
 * @appType: This app is an augmented reality app which allows the user to tag locations
 * @version: 1.0 
 * @description: This activity is the core feature of this application. This is where the tags are shown,
 * and new tags are made.
 */

package com.Screens;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.Camera.CameraPreview;
import com.data.Tag;
import com.provider.Provider;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class Screen6 extends Activity implements SensorEventListener,OnTouchListener, OnClickListener{

	
	private Camera mCamera;
	private Context context;
    private Dialog dialogPostTag;
    private Dialog dialogInfoTag;
    private Dialog dialogYourInfoTag;
    
	private static ProgressDialog dialog;
	
	private	float xCurr=0.0f;
	private SensorManager mSensorManager;
	private static Tag  tag[];
	private static  List<Tag> tags=new ArrayList<Tag>();
	private CustomView customView;
	
	private int height=0;
	private int width=0;
	
	private float xTouch=0.0f;
	private float yTouch=0.0f;
		
	private EditText commentText;
	private Button submitButton;
	private Button cancelButton;
	
	private Button closeButtonDialog;
	private TextView commentTextDialog;
	private TextView locationTextDialog;
	private TextView distanceTextDialog;
	
	private TextView yourLatitudeTextDialog;
	private TextView yourLongitudeTextDialog;
	private TextView accuracyTextDialog;
	private Button closeYourButtonDialog;

	
	
	private static ProgressDialog progressDialog;
	private static ArrayList<String>userData;
	private static Double latitude;
	private static Double longitude;
	private static Float accuracy;
		
	private static String commentStatic;
	
	private Handler handler = new Handler();
	private static String toastMessage;

	private static Provider locationProvider;
	
	
	/**********onCreate function**********/

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        //if i use getApplicationContext() then i get NULL when i try to call the function dialog.show() function. when i use this as the application context i get no such error
        //this link gave me the hint to what the fix might be
        //http://stackoverflow.com/questions/1561803/android-progressdialog-show-crashes-with-getapplicationcontext
        //also in the test app i had used the mContext=this; where mContext is the same variable as context
        //context=getApplicationContext();
        context=this;
       		    
		mCamera=CameraPreview.getCameraInstance();
		
		
		/**
         * the following source gave me a hint of how to show the camera preview and the customview at the same time.
         * You could use a FrameLayout and put first your surfaceView as a child and then some other view with no/transparent background.
		 * A FrameLayout draws its children in the order of adding.
         * http://stackoverflow.com/questions/10180273/how-to-add-the-camera-as-canvas-background 
         * 
         */

		
		// Now also create a view which contains the camera preview...
		CameraPreview   cameraView = new CameraPreview( this,mCamera );
		// ...and add it, wrapping the full screen size.
		
		customView=new CustomView(this);

		 
 		FrameLayout fl=new FrameLayout(this);
 		fl.setLayoutParams(new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ));
 		fl.addView(cameraView);
 		fl.addView(customView);
 		fl.setOnTouchListener(this);
 		
 		/**
 		 * Change(Addition) in version 0.3
 		 * 
 		 * In the following code i am creating layouts and creating new ImageView objects which i will use to create new
 		 * icons and buttons of the refresh button, tagit button and the cross hair.
 		 * I do not need to draw these images in onDraw function using the canvas object now.
 		 * I was having problem in making the tagging screen scalable for screens of different sizes.
 		 * Then i thought of designing a built-in supported layout which will scale itself as it is supposed to.
 		 * 
 		 * Note: I have tested this only on Samsung Galaxy Ace GT-S5830 which has screen resolution of 320 by 480 pixles
 		 * which is supposed to be in the category of normal screen (http://developer.android.com/guide/practices/screens_support.html)
 		 * I also have tested this feature on the Android Emulator of different screen sizes, but i can not be 100 percent sure if the feature
 		 * works properly, unless i test on an actual device.
 		 * 
 		 */
 		
 		LinearLayout linearCrossHair=new LinearLayout(this);
	        
        ImageView crossHair=new ImageView(this);
        crossHair.setImageResource(R.drawable.crosshair);
        crossHair.setLayoutParams(new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ));
        linearCrossHair.setGravity(Gravity.CENTER);
        linearCrossHair.setLayoutParams(new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ));
        linearCrossHair.addView(crossHair);
        
 		
        LinearLayout linearBottom=new LinearLayout(this);
        LinearLayout linearRefresh=new LinearLayout(this);
        LinearLayout linearTagIt=new LinearLayout(this);
        
        
        
        ImageView refresh=new ImageView(this);
        refresh.setId(1988198888);//id am using a custom id so that it does not collide with any system defined id
        refresh.setImageResource(R.drawable.refreshbutton);
        refresh.setLayoutParams(new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));

        refresh.setOnClickListener(this);
        
        ImageView tagit=new ImageView(this);
        tagit.setId(2012201212);//id am using a custom id so that it does not collide with any system defined id
        tagit.setImageResource(R.drawable.tagit);
        tagit.setLayoutParams(new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
        tagit.setPadding(0, 0, 0, 10);
        
        tagit.setOnClickListener(this);
                
        linearBottom.setOrientation(LinearLayout.HORIZONTAL);
        linearBottom.setGravity(Gravity.BOTTOM);
        linearBottom.setLayoutParams(new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ));
       
        linearRefresh.setLayoutParams(new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ));
        linearRefresh.addView(refresh);
        linearRefresh.setGravity(Gravity.LEFT);
        
        linearTagIt.setLayoutParams(new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT ));
        linearTagIt.addView(tagit);
        linearTagIt.setGravity(Gravity.RIGHT);
        
        linearBottom.addView(linearRefresh);
        linearBottom.addView(linearTagIt);
 		
 		
 		fl.addView(linearCrossHair);
 		fl.addView(linearBottom);
			
 		
 		setContentView(fl);
		
		//I am setting up the dialog box to be shown when the user tags a location.
    	//use either of the two following functions. i must get the context correct.
    		//Dialog dialog = new Dialog(mContext);
			dialogPostTag = new Dialog(context);
			dialogPostTag.setContentView(R.layout.dialog);
            dialogPostTag.setTitle("Tag It");
            dialogPostTag.setCancelable(false);

						
			//set up button
			submitButton = (Button) dialogPostTag.findViewById(R.id.submitButton);
			submitButton.setOnClickListener(this);
			cancelButton= (Button) dialogPostTag.findViewById(R.id.cancelButton);
			cancelButton.setOnClickListener(this);
			
			commentText=(EditText)dialogPostTag.findViewById(R.id.commentText);

			
			dialogInfoTag = new Dialog(context);
			dialogInfoTag.setContentView(R.layout.dialog1);
			dialogInfoTag.setTitle("Tag Information");
			dialogInfoTag.setCancelable(false);
			
			closeButtonDialog=(Button)dialogInfoTag.findViewById(R.id.close);
			closeButtonDialog.setOnClickListener(this);
			
			
			dialogYourInfoTag = new Dialog(context);
			dialogYourInfoTag.setContentView(R.layout.dialog2);
			dialogYourInfoTag.setTitle("Your Location Information");
			dialogYourInfoTag.setCancelable(false);
			
			yourLatitudeTextDialog=(TextView)dialogYourInfoTag.findViewById(R.id.latitudeTextDialog);
            yourLongitudeTextDialog=(TextView)dialogYourInfoTag.findViewById(R.id.longitudeTextDialog);
            accuracyTextDialog=(TextView)dialogYourInfoTag.findViewById(R.id.accuracyTextDialog);
			
			
			closeYourButtonDialog=(Button)dialogYourInfoTag.findViewById(R.id.closeYourDialog);
			closeYourButtonDialog.setOnClickListener(this);
			
			
			 Bundle bundle=getIntent().getExtras();
		        
		     userData=bundle.getStringArrayList("userData");
		     latitude=bundle.getDouble("latitude");
		     longitude=bundle.getDouble("longitude");
		     accuracy=bundle.getFloat("accuracy");
		     
		 	locationProvider=new Provider();
			
		
		/**
		 * check for internet connection. if the internet connection is not enabled then ask the user to enable it.
		 * 
		 * http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
		 * 
		 * I will now get the nearest tags from the server
		 **/
		
        ConnectivityManager connectivityManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkStatus= connectivityManager.getActiveNetworkInfo();
        
		
		if(networkStatus!=null)
		{
			progressDialog = ProgressDialog.show(this, "","Please wait...Getting nearest locations to you...", true);
			progressDialog.setCancelable(true);
			new Thread() {

				public void run() {
					tags.clear();// i am clearing the list of tags because i want to get a fresh list of tags from the server.
											
					//	sleep(10000);
						
						
						//source for reading and writing to a url. http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
			        	//i am posting the data via the POST method
			        	 
			        	 try {
							
							
							//i am encoding the values into base64 encoding to provide a certain amount of security.
			        		//i do not want to send the data as plain text over the airwaves
			        		//i have chosen base64 instead of other encoding schemes such as md5 has, because i want 
			        		//a 2 way encryption scheme which can be encoded and also be decoded because on the server side
			        		//i need to decode the data to be able to perform operations
			        		 
			        		String latitudeString;
			        		String longitudeString;
			        		
							latitudeString=Base64.encodeToString(latitude.toString().getBytes(),Base64.DEFAULT);
							longitudeString=Base64.encodeToString(longitude.toString().getBytes(),Base64.DEFAULT);
																		
							 byte [] decodedBytes;
							 String data;
				             String decodedString;
				             StringBuffer stringComplete=new StringBuffer();
				             
				             URL url = new URL("http://worldofpakistan.net/tagar/getnearesttags.php");
				             URLConnection connection = url.openConnection();
				             connection.setDoOutput(true);
				            
				             OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				             out.write("latitude="+latitudeString+"&longitude="+longitudeString);//i am sending data via the post method
				             out.close();
				
				             BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				             		             
				             while ((data = in.readLine()) != null) 
				             {
				            	 stringComplete.append(data);
	
				             }//end while
						        			        	             
				             in.close();
				             
				             decodedBytes=Base64.decode(stringComplete.toString(),Base64.DEFAULT);
				             decodedString=new String(decodedBytes);
				            // Toast.makeText(this, decodedString, 1000).show();
				            
				             //Log.d("tagAR", decodedString);
				             
				             int pos=decodedString.indexOf("],",0);
				             int pos1=0;
				             
			            	 //Log.d("pos", ((Integer)pos).toString());
			            	 //Log.d("pos1", ((Integer)pos1).toString());
			            	 
				             String num;
				             Integer numOfItems;
				             try
				             {
				            	 num=decodedString.substring(2, pos);//i am starting from 2 because [[1 to get 1 i will have to place the pointer just before 1, which lies at 2. at 0 lies the first [, at 1 lies the second [, and at 2 lies the 1(or the first character of the number in the string 
				            	 numOfItems=Integer.parseInt(num);
				             }//end if
				             catch(Exception e)
				             {
				            	 numOfItems=-3;
				             }//end else
				             
				             
				             if(numOfItems==1)
				             {
				            	 
						            progressDialog.dismiss();
						             
				            	 
				             }//end if
				             else if(-3==numOfItems)
				             {
				            	 progressDialog.dismiss();
				             }//end else if
				             else
				             {
				            	 pos=pos+3;
				            	 pos1=decodedString.indexOf("],",pos);
				            	 num=decodedString.substring(pos, pos1);
					             Integer numOfRows=Integer.parseInt(num);
					             pos=pos1+3;
					             
					             String sub="";
				         		
				         	      tag=new Tag[numOfRows];
			         	        
					         	  Float bearing=0.0f;
					         	  Location l;
					         	  Location l1;
					         	   
				         	   
				         	   
					             for(int i=0;i<numOfRows;i++)
					             {
					            	 	tag[i]=new Tag();
						            	 pos1=decodedString.indexOf("]", pos);
						            	//Log.d("pos", ((Integer)pos).toString());
						            	//Log.d("pos1", ((Integer)pos1).toString());
						            	 sub=decodedString.substring(pos, pos1);
						            	//Log.d("id", sub);
						            	 pos=pos1+3;
						            	 tag[i].setId(Integer.parseInt(sub));
				            	 		
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("distance", sub);
							             pos=pos1+3;
							             tag[i].setDistance(Double.parseDouble(sub));
							             
							             pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("latitude", sub);
							             pos=pos1+3;
				            	 		 tag[i].setLatitude(Double.parseDouble(sub));
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("longitude", sub);
							             pos=pos1+3;
				            	 		 tag[i].setLongitude(Double.parseDouble(sub));
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	 //Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							             //Log.d("direction", sub);
							             pos=pos1+3;
							             tag[i].setX(Float.parseFloat(sub));//save the direction/bearing.
							            /**
							             * Over here i am calculating the bearing of the two different geo-location coordinate points.
							             * This is basically the angle at which the other point is form my current location. I need this bearing
							             * because i need to in which direction i have to face to be able to know the actual direction.
							             * Initially i was taking the x value of the accelerometer but that was the wrong technique as there would be cases
							             * where i would not be looking at the tagged coordinate point, but would instead be looking away from it, as 
							             * the value that i would be using is the one where i am looking away from the tagged location when i tagged that location.    
							             *i found the bearingTo function here http://developer.android.com/reference/android/location/Location.html and got a hint from 
							             *here to what should be the parameter in the Location constructor when i create a Location object. The hint was to use any string instead
							             *of the provider (GPS or network) as i will be using my own latitude and longitude values.
							             *http://stackoverflow.com/questions/4945697/creating-android-location-object 
							             */
							             
							            l=new Location(" ");
							     		l.setLatitude(latitude);
							     		l.setLongitude(longitude);
							     		
							     		l1=new Location(" ");
							     		l1.setLatitude(tag[i].getLatitude());
							     		l1.setLongitude(tag[i].getLongitude());
							     		
							     		bearing=l.bearingTo(l1);
							     		
							     		if(bearing<0)
							     		{
							     			bearing=(-1)*bearing;
							     			bearing=180+(180-bearing);
							     		}//end if
							             
							     		/**
							     		 * 
							     		 * if the distance between the user's current location and the retrieved location values is less than 0.8km then use the 
							     		 * camera's direction value, else use the bearing. the value 0.8km is not a special value. i have taken this because 
							     		 * my error of accuracy of the network provider is around 0.8km. this value may be different depending on your mobile device and
							     		 * internet connection being used on the device. By using the network provider instead of the gps i get quite a bit of error in 
							     		 * accuracy.
							     		 */
							     		
							     		 if(tag[i].getDistance()<0.8)
 							             {
 							            	 tag[i].setX(tag[i].getX());
	 				            	 		 tag[i].setY(-1);
	 				            	 		 tag[i].setZ(84);
 							             }
 							             else
 							             {
	 				            	 		 tag[i].setX(bearing);
	 				            	 		 tag[i].setY(-1);
	 				            	 		 tag[i].setZ(84);
 							             }//end else
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("comment", sub);
							             pos=pos1+3;
				            	 		 tag[i].setComment(sub);
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("locality", sub);
							             pos=pos1+3;
				            	 		 tag[i].setLocality(sub);
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("city", sub);
							             pos=pos1+3;
				            	 		 tag[i].setCity(sub);
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	 //Log.d("pos", ((Integer)pos).toString());
							             //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("country", sub);
							             pos=pos1+3;
				            	 		 tag[i].setCountry(sub);
				            	 		  
				            	 		 tags.add(tag[i]);
				            	 		
						            	
						            	 //Log.d("i",((Integer)i).toString() );
						            	 
						             
					             }//end for
					           
					             progressDialog.dismiss();
					            
				            	 
				             }//end else
				             
				            		
				} catch (UnsupportedEncodingException e) {
					
					//e.printStackTrace();
				} catch (MalformedURLException e) {
					
					//e.printStackTrace();
				} catch (IOException e) {
					
					//e.printStackTrace();
				}
				
				}
				
			}.start();
					 
			
			 accuracyTextDialog.setText(accuracy.toString()+" meters");
			 yourLatitudeTextDialog.setText(latitude.toString());
			 yourLongitudeTextDialog.setText(longitude.toString());
			 dialogYourInfoTag.show();
			
			
		}//end if
		else
		{
		Toast.makeText(this, "No Internet connection detected. Please enable your internet connection.", 1000).show();
		startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);//open up the systems screen which allows me to set the internet settings

		}//end else
		
		
	}

	/**********onResume function**********/

	protected void onResume() {
		super.onResume();


		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
		 
				
	}

	
	/**********onPause function**********/
	@Override
	protected void onPause() {
		super.onPause();
		
		locationProvider.stopUpdates();//stop getting updates. Stop getting the updates as soon as i have the data. I do not want to waste the battery on looking for updates all the time.

        mSensorManager.unregisterListener(this);
        Intent intent =new Intent("com.TagScreen5");
		intent.putStringArrayListExtra("userData", userData);
		startActivity(intent);                                              
		finish();

	}

	/**********onDestroy function**********/

	protected void onDestroy()
	{
		super.onDestroy();
	
	}



	/**********onRestart function**********/

	protected void onRestart()
	{
		super.onRestart();

	}


	
	
	
	
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int arg1) {
		
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		


		 if(event.sensor.getType()==Sensor.TYPE_ORIENTATION)
			{
				 xCurr=event.values[0];//x coordinate
				 
				//String msg="Orientation: x: "+xCurr+" y: "+yCurr+" z: "+zCurr;
				
				//Log.d("GuiTest", msg);
				
				float x;//=tags.get(0).getX();
				//float y=tags.get(0).getY();
				
				int i=0;
				//when ever the device is moved, the difference values of all of the tags are recalculated. It is these difference values which is actually the difference between the actual point of the tag and the x value of the accelerometer. if the 
				//difference is within a certain value then show the tag, else do not show the tag.
				while(null!=tags && i<tags.size())
				{
					x=tags.get(i).getX();
					tags.get(i).setDiffX(xCurr-x);
					
			
					i++;
				}//end while
				
				customView.invalidate();	
				
												
			}//end if
		
		
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {

		/**
		 * the values of width, and height have been swapped in the landscape view. the width is the y axis, while the height is x axis 
		 */

		
		xTouch= event.getX();
		yTouch= event.getY();
		
		float xTouch= event.getX();
		//float yTouch= event.getY();
		//float xRel=0.0f;
		//float yRel=0.0f;

		/**
		 *What i have done here is that i have manually calculated the relative positions of the buttons of the whole
		 *screen on the screen. Then i am using such relative values to know if the location i touched is within relative values.  
		 *By this i have made the touch input compatible for all screen sizes. Again we can not be so sure that complete compatibility 
		 *has been achieved until wee test this app on other mobile devices. 
		 */

		
		//yRel=yTouch/width;
		//xRel=xTouch/height;
		
		
		//Toast.makeText(this, "x: "+((Float)(xTouch)).toString()+" y: "+((Float)(yTouch)).toString(),1000).show();
		//Toast.makeText(this, "height: "+((Integer)(height)).toString()+" width: "+((Integer)(width)).toString(),1000).show();
		
		float x1=0.0f, diff=0.0f;
		
			
			int i=0;
			int spaceBetweenTags=0;
			
			while(null!=tags && i<tags.size())
			{
				
				
				//Toast.makeText(this, "width: "+((Integer)(width)).toString()+"height: "+((Integer)(height)).toString(),1000).show();
			
				if(true==tags.get(i).isShownOnScreen())
				{//only test for those tags which are being shown on the screen
					
					/**
					 * Changes in Version 0.4 (Addition)
					 * I am now testing only for those tags which are being shown. Before this fix those tags were also
					 * being selected which were not shown, but were close by. To fix that issue i am now checking if the tag
					 * is being shown. If it is being shown check if that is the tag which has been touched, else do not do any checking.
					 * 
					 */
					
					tags.get(i).resetToIdentity();
					tags.get(i).translation((height/2),-(height/2), 0);
					tags.get(i).translation((-tags.get(i).getDiffX()*3)+50+spaceBetweenTags,0, 0);
					tags.get(i).updateXYZ();
					x1=tags.get(i).getVector()[0];
					tags.get(i).resetVector();
					diff=0.0f;
					
					
					//Toast.makeText(this, ((Float)x1).toString(), 1000).show();
					//Toast.makeText(this, ((Float)xTouch).toString(), 1000).show();
					
					
					
					if(xTouch>x1)
					{
						diff=xTouch-x1;
						
					}//end if
					else
					{
						diff=x1-xTouch;
						
					}//end else
					
					
					/**
					 * 
					 * I have increased the window from 8 to 25 because when we press the tags, then our fingers tend not to be precise
					 * exactly, and after some experimenting i found this value to be the most suitable value. After some testing on the mobile 
					 * device available (Samsung Galaxy Ace GT-S5830), and the Android emulator it seemed like that when the tag is touched the correct dialog related to the tag
					 * is opened with the correct information. More testing on other mobile devices will need to be done in order to be completely
					 * sure that this feature works properly.  
					 * 
					 */
					
					if(diff<=25 )
					{	
						
						commentTextDialog=(TextView)dialogInfoTag.findViewById(R.id.commentTextDialog);
						locationTextDialog=(TextView)dialogInfoTag.findViewById(R.id.locationTextDialog);
						commentTextDialog.setText(tags.get(i).getComment());
						locationTextDialog.setText(tags.get(i).getLocality()+", "+tags.get(i).getCity()+", "+tags.get(i).getCountry());
						distanceTextDialog=(TextView)dialogInfoTag.findViewById(R.id.distanceTextDialog);
						distanceTextDialog.setText(((Double)tags.get(i).getDistance()).toString()+"km away");
						dialogInfoTag.show();
						
						
						
						break;
						
					}//end if
						
					spaceBetweenTags=spaceBetweenTags+50;
					
				}//end if
				
				i++;
				
			}//end while
		
		
		return false;
	}

	
	
	@Override
	public void onClick(View view) {
		
		int id=view.getId();
		String comment;
		
		if(R.id.submitButton==id)
		{//the tagIT information has been submitted
			comment=commentText.getText().toString();
		
			
			//comment=comment.replace(" ", "");
			
			if((comment.equals(" ") || comment.equals("")))
			{//if the name contains just space or no value at all then do not allow to continue.
			
				Toast.makeText(this, "Please enter the comment field. Do not leave it empty.", 1000).show();
				
			}//end if
			else
			{
				commentText.setText("");
								
				//Toast.makeText(this,comment, 1000).show();
				
		        
		        commentStatic=comment;
		        //Toast.makeText(this, commentStatic, 1000).show();
		        
		        		        				
				dialogPostTag.dismiss();
				
				
				/**
				 * check for internet connection. if the internet connection is not enabled then ask the user to enable it.
				 * 
				 * http://stackoverflow.com/questions/4238921/android-detect-whether-there-is-an-internet-connection-available
				 * 
				 * I will now get the nearest tags from the server
				 **/
				
		        ConnectivityManager connectivityManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		        NetworkInfo networkStatus= connectivityManager.getActiveNetworkInfo();
		        
				
				if(networkStatus!=null)
				{
					progressDialog = ProgressDialog.show(this, "","Please wait...", true);
					progressDialog.setCancelable(true);
					
					new Thread() {

						public void run() {

							try{
			
								
							//	sleep(10000);
								
								
								//source for reading and writing to a url. http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
					        	//i am posting the data via the POST method
					        	 
					        	 try {
									
									
									//i am encoding the values into base64 encoding to provide a certain amount of security.
					        		//i do not want to send the data as plain text over the airwaves
					        		//i have chosen base64 instead of other encoding schemes such as md5 has, because i want 
					        		//a 2 way encryption scheme which can be encoded and also be decoded because on the server side
					        		//i need to decode the data to be able to perform operations
					        		 
					        		String latitudeString;
					        		String longitudeString;
					        		String userId;
					        		String commentText;
					        		String directionString;
					        	
					        		
									latitudeString=Base64.encodeToString(latitude.toString().getBytes(),Base64.DEFAULT);
									longitudeString=Base64.encodeToString(longitude.toString().getBytes(),Base64.DEFAULT);
									userId=Base64.encodeToString(userData.get(0).getBytes(),Base64.DEFAULT);
									commentText=Base64.encodeToString(commentStatic.getBytes(),Base64.DEFAULT);
									directionString=Base64.encodeToString(((Float)xCurr).toString().getBytes(),Base64.DEFAULT);
									
									 String data;
						             StringBuffer stringComplete=new StringBuffer();
						             
						             URL url = new URL("http://worldofpakistan.net/tagar/posttag.php");
						             URLConnection connection = url.openConnection();
						             connection.setDoOutput(true);
						            
						             OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
						             out.write("latitude="+latitudeString+"&longitude="+longitudeString+"&comment="+commentText+"&userId="+userId+"&direction="+directionString);//i am sending data via the post method
						             out.close();
						
						             BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						             		             
						             while ((data = in.readLine()) != null) 
						             {
						            	 stringComplete.append(data);
						            	 
						             }//end while
								      
						             
						             //Log.d("tagAR", stringComplete.toString());
						             
						             in.close();
						             
						             
						             
						             
	             ///////////////////////////////////////////////////////////////////////////
						             
			             
			             
	             new Thread() {

	 				public void run() {
	 					tags.clear();
	 											
	 					//	sleep(10000);
	 						
	 						
	 						//source for reading and writing to a url. http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
	 			        	//i am posting the data via the POST method
	 			        	 
	 			        	 try {
	 							
	 							
	 							/**i am encoding the values into base64 encoding to provide a certain amount of security.
	 			        		*i do not want to send the data as plain text over the airwaves
	 			        		*i have chosen base64 instead of other encoding schemes such as md5 has, because i want 
	 			        		*a 2 way encryption scheme which can be encoded and also be decoded because on the server side
	 			        		*i need to decode the data to be able to perform operations
	 			        		*/
	 			        		 
	 			        		String latitudeString;
	 			        		String longitudeString;
	 			        		
	 							latitudeString=Base64.encodeToString(latitude.toString().getBytes(),Base64.DEFAULT);
	 							longitudeString=Base64.encodeToString(longitude.toString().getBytes(),Base64.DEFAULT);
	 																		
	 							 byte [] decodedBytes;
	 							 String data;
	 				             String decodedString;
	 				             StringBuffer stringComplete=new StringBuffer();
	 				             
	 				             URL url = new URL("http://worldofpakistan.net/tagar/getnearesttags.php");
	 				             URLConnection connection = url.openConnection();
	 				             connection.setDoOutput(true);
	 				            
	 				             OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
	 				             out.write("latitude="+latitudeString+"&longitude="+longitudeString);//i am sending data via the post method
	 				             out.close();
	 				
	 				             BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	 				             		             
	 				             while ((data = in.readLine()) != null) 
	 				             {
	 				            	 stringComplete.append(data);
	 	
	 				             }//end while
	 						        			        	             
	 				             in.close();
	 				             
	 				             decodedBytes=Base64.decode(stringComplete.toString(),Base64.DEFAULT);
	 				             decodedString=new String(decodedBytes);
	 				            // Toast.makeText(this, decodedString, 1000).show();
	 				            
	 				           //  Log.d("tagAR", decodedString);
	 				             
	 				             int pos=decodedString.indexOf("],",0);
	 				             int pos1=0;
	 				             
	 			            	 //Log.d("pos", ((Integer)pos).toString());
	 			            	 //Log.d("pos1", ((Integer)pos1).toString());
	 			            	 
	 				             String num=decodedString.substring(2, pos);//i am starting from 2 because [[1 to get 1 i will have to place the pointer just before 1, which lies at 2. at 0 lies the first [, at 1 lies the second [, and at 2 lies the 1(or the first character of the number in the string 
	 				             //Log.d("num", num);
	 				             Integer numOfItems=Integer.parseInt(num);//get the number of rows, i.e. number of tags
	 				             
	 				             
	 				             if(numOfItems==1)
	 				             {
	 				            	 
	 						            progressDialog.dismiss();
	 						             
	 				            	 
	 				             }//end if
	 				             else
	 				             {
	 				            	 pos=pos+3;
	 				            	 pos1=decodedString.indexOf("],",pos);
	 				            	 num=decodedString.substring(pos, pos1);
	 					             Integer numOfRows=Integer.parseInt(num);
	 					             pos=pos1+3;
	 					             
	 					             String sub="";
	 				         		
	 				         	      tag=new Tag[numOfRows];
	 			         	        
	 					         	  Float bearing=0.0f;
	 					         	  Location l;
	 					         	  Location l1;
	 					         	   
	 				         	   
	 				         	   
	 					             for(int i=0;i<numOfRows;i++)
	 					             {
	 					            	 	tag[i]=new Tag();
	 						            	 pos1=decodedString.indexOf("]", pos);
	 						            	//Log.d("pos", ((Integer)pos).toString());
	 						            	//Log.d("pos1", ((Integer)pos1).toString());
	 						            	 sub=decodedString.substring(pos, pos1);
	 						            	//Log.d("id", sub);
	 						            	 pos=pos1+3;
	 						            	 tag[i].setId(Integer.parseInt(sub));
	 				            	 		
	 				            	 		 pos1=decodedString.indexOf("]", pos);
	 		        	 	            	//Log.d("pos", ((Integer)pos).toString());
	 							            //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							            //Log.d("distance", sub);
	 							             pos=pos1+3;
	 							             tag[i].setDistance(Double.parseDouble(sub));
	 							             
	 							             pos1=decodedString.indexOf("]", pos);
	 		        	 	            	//Log.d("pos", ((Integer)pos).toString());
	 							            //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							            //Log.d("latitude", sub);
	 							             pos=pos1+3;
	 				            	 		 tag[i].setLatitude(Double.parseDouble(sub));
	 				            	 		 
	 				            	 		 pos1=decodedString.indexOf("]", pos);
	 		        	 	            	//Log.d("pos", ((Integer)pos).toString());
	 							            //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							            //Log.d("longitude", sub);
	 							             pos=pos1+3;
	 				            	 		 tag[i].setLongitude(Double.parseDouble(sub));
	 				            	 		 
	 				            	 		 pos1=decodedString.indexOf("]", pos);
	 		        	 	            	 //Log.d("pos", ((Integer)pos).toString());
	 							            //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							             //Log.d("direction", sub);
	 							             pos=pos1+3;
	 							             tag[i].setX(Float.parseFloat(sub));
	 							             
	 							            /**
	 							             * Over here i am calculating the bearing of the two different geo-location coordinate points.
	 							             * This is basically the angle at which the other point is form my current location. I need this bearing
	 							             * because i need to in which direction i have to face to be able to know the actual direction.
	 							             * Initially i was taking the x value of the accelerometer but that was the wrong technique as there would be cases
	 							             * where i would not be looking at the tagged coordinate point, but would instead be looking away from it, as 
	 							             * the value that i would be using is the one where i am looking away from the tagged location when i tagged that location.    
	 							             *i found the bearingTo function here http://developer.android.com/reference/android/location/Location.html and got a hint from 
	 							             *here to what should be the parameter in the Location constructor when i create a Location object. The hint was to use any string instead
	 							             *of the provider (GPS or network) as i will be using my own latitude and longitude values.
	 							             *http://stackoverflow.com/questions/4945697/creating-android-location-object 
	 							             */
	 							             
	 							            l=new Location(" ");
	 							     		l.setLatitude(latitude);
	 							     		l.setLongitude(longitude);
	 							     		
	 							     		l1=new Location(" ");
	 							     		l1.setLatitude(tag[i].getLatitude());
	 							     		l1.setLongitude(tag[i].getLongitude());
	 							     		
	 							     		bearing=l.bearingTo(l1);
	 							     		
	 							     		/**
								     		 * 
								     		 * if the distance between the user's current location and the retrieved location values is less than 0.8km then use the 
								     		 * camera's direction value, else use the bearing. the value 0.8km is not a special value. i have taken this because 
								     		 * my error of accuracy of the network provider is around 0.8km. this value may be different depending on your mobile device and
								     		 * internet connection being used on the device. By using the network provider instead of the gps i get quite a bit of error in 
								     		 * accuracy.
								     		 */
	 							     		
	 							     		if(bearing<0)
	 							     		{
	 							     			bearing=(-1)*bearing;
	 							     			bearing=180+(180-bearing);
	 							     		}//end if
	 							     		
	 							             if(tag[i].getDistance()<0.8)
	 							             {
	 							            	 tag[i].setX(tag[i].getX());
		 				            	 		 tag[i].setY(-1);
		 				            	 		 tag[i].setZ(84);
	 							             }
	 							             else
	 							             {
		 				            	 		 tag[i].setX(bearing);
		 				            	 		 tag[i].setY(-1);
		 				            	 		 tag[i].setZ(84);
	 							             }//end else
	 				            	 		 
	 				            	 		 pos1=decodedString.indexOf("]", pos);
	 		        	 	            	//Log.d("pos", ((Integer)pos).toString());
	 							            //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							            //Log.d("comment", sub);
	 							             pos=pos1+3;
	 				            	 		 tag[i].setComment(sub);
	 				            	 		 
	 				            	 		 pos1=decodedString.indexOf("]", pos);
	 		        	 	            	//Log.d("pos", ((Integer)pos).toString());
	 							            //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							            //Log.d("locality", sub);
	 							             pos=pos1+3;
	 				            	 		 tag[i].setLocality(sub);
	 				            	 		 
	 				            	 		 pos1=decodedString.indexOf("]", pos);
	 		        	 	            	//Log.d("pos", ((Integer)pos).toString());
	 							            //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							            //Log.d("city", sub);
	 							             pos=pos1+3;
	 				            	 		 tag[i].setCity(sub);
	 				            	 		 
	 				            	 		 pos1=decodedString.indexOf("]", pos);
	 		        	 	            	 //Log.d("pos", ((Integer)pos).toString());
	 							             //Log.d("pos1", ((Integer)pos1).toString());
	 							             sub=decodedString.substring(pos, pos1);
	 							            //Log.d("country", sub);
	 							             pos=pos1+3;
	 				            	 		 tag[i].setCountry(sub);
	 				            	 		  
	 				            	 		 tags.add(tag[i]);
	 				            	 		
	 						            	
	 						            	 //Log.d("i",((Integer)i).toString() );
	 						            	 
	 						             
	 					             }//end for
	 					           
	 					             //progressDialog.dismiss();
	 					            
	 				            	 
	 				             }//end else
			 				             
			 				            		
			 				} catch (UnsupportedEncodingException e) {
			 					
			 					//e.printStackTrace();
			 				} catch (MalformedURLException e) {
			 					
			 					//e.printStackTrace();
			 				} catch (IOException e) {
			 					
			 					//e.printStackTrace();
			 				}
			 				
			 				}
			 				
			 			}.start();
			             
			             
						             
						             progressDialog.dismiss();
						            
						            toastMessage="The tag has been posted.";
						            showToast(); 
						             
						             
									} catch (UnsupportedEncodingException e) {
										
										//e.printStackTrace();
									} catch (MalformedURLException e) {
										
										//e.printStackTrace();
									} catch (IOException e) {
										
										//e.printStackTrace();
									}
												
			
							} catch (Exception e) {
			
							//Log.e("tag", e.getMessage());
			
							}
							
							
					
						
						}
						
					}.start();
							 

				}//end if
				else
				{
					Toast.makeText(this, "No Internet connection detected. Please enable your internet connection.", 1000).show();
					startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);//open up the systems screen which allows me to set the internet settings

				}//end else
				
				
				
				
			}//end else
			
			
			
		}//end if
		else if(R.id.cancelButton==id)
		{
			commentText.setText("");
			dialogPostTag.dismiss();
			
		}//end else if
		else if(R.id.closeYourDialog==id)
		{
			this.dialogYourInfoTag.dismiss();
		}//end if
		else if(R.id.close==id)
		{
			dialogInfoTag.dismiss();
		}//end else if
		else if(1988198888==id)
		{//refresh button has been pressed
			//id am using a custom id so that it does not collide with any system defined id
				 ConnectivityManager connectivityManager =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	             NetworkInfo networkStatus= connectivityManager.getActiveNetworkInfo();
	
		     		locationProvider.registerLocationListeners((LocationManager)getSystemService(LOCATION_SERVICE));
		
					
		        	if(networkStatus!=null)
		        	{
		        			                
		                 
		        		boolean isNetworkProviderEnabled=  locationProvider.getLocationManager().isProviderEnabled (LocationManager.NETWORK_PROVIDER);
		        		boolean isGPSEnabled=  locationProvider.getLocationManager().isProviderEnabled (LocationManager.GPS_PROVIDER);
		        		
		        		if(isNetworkProviderEnabled && isGPSEnabled)
		        		{//network provider is on.
		        			
		            		dialog = ProgressDialog.show(this, "","Please wait...Getting your geo-location coordinates...", true);
		            		dialog.setCancelable(true);
		            		
		
		            		new Thread() {
		
		            			public void run() {
		
		            			try{
		
		            				//sleep(10000);//sleep for 10 seconds
		            				
		            				while(null==locationProvider.getCurrentLocation())
		            				{
		            					
		            				}//end while
		
		            			} catch (Exception e) {
		
		            			//	Log.e("tag", e.getMessage());
		
		            			}
		            				dialog.dismiss();  
		            				latitude=locationProvider.getCurrentLocation().getLatitude();
		            				longitude=locationProvider.getCurrentLocation().getLongitude();
		            				accuracy=locationProvider.getCurrentLocation().getAccuracy();
		            				// Log.d("app",((Float)locationProvider.getCurrentLocation().getAccuracy()).toString());
		            				//Log.d("app", latitude.toString());
		            				//Log.d("app", longitude.toString());
		            				
		            				locationProvider.stopUpdates();//stop getting updates. Stop getting the updates as soon as i have the data. I do not want to waste the battery on looking for updates all the time.
		            			
		            			}
		            			
		            			}.start();
		            		
		            		
		            			
		        		
		        		}//end if
		        		else
		        		{//both of the providers must be enabled
		        			Toast.makeText(this, "Please enable the wireless network(for location based services) and GPS. You need to enable both of the settings to be able to use this app.", 1000).show();
		    				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);//open up the system screen which allows me to enable the GPS
		    				
		        		}//end else
		        		
		        		
		        		 
		        	}//end if
		        	else
		        	{
			        	Toast.makeText(this, "No Internet connection detected. Please enable your internet connection.", 1000).show();
			        	startActivityForResult(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS), 0);//open up the systems screen which allows me to set the internet settings
		
		        	}//end else
					
		    			    		
		    		
			
		}//end else if
		else if(2012201212==id)
		{//tagit button has been pressed
			//id am using a custom id so that it does not collide with any system defined id
			 dialogPostTag.show();

			
		}//end else if
		else
		{
		}//end else
		
	}

	
	/**
	 * 
	 * source for showing Toast notification to the ui thread from a thread.
	 * http://www.codeproject.com/Articles/109735/Toast-A-User-Notification
	 * 
	 * I need to pass a handler which can send messages to the ui thread.
	 * 
	 */
	
	  public void showToast() {
		          handler.post(new Runnable() {
		               public void run() {
		                   Toast.makeText(getApplicationContext(),
		                           toastMessage, Toast.LENGTH_SHORT).show();
		               }
		           });
		      }
	
	  
	  /**********onKeyDown function*********/
		public boolean onKeyDown(int keyCode, KeyEvent event)
		{//this function will work with all the Android versions 
			//i am overriding the back button and the home button



		
			if (keyCode == KeyEvent.KEYCODE_BACK) 
			{


				Intent intent =new Intent("com.TagScreen5");
				intent.putStringArrayListExtra("userData", userData);
				startActivity(intent);                                              
				finish();

			}//end if
			else if(keyCode==KeyEvent.KEYCODE_MENU)
			{
				

        		progressDialog = ProgressDialog.show(this, "","Please wait...Getting nearest locations to you...", true);
    			progressDialog.setCancelable(true);
    			new Thread() {

    				public void run() {
    					tags.clear();// i am clearing the list of tags because i want to get a fresh list of tags from the server.
    											
    					//	sleep(10000);
    						
						
						//source for reading and writing to a url. http://docs.oracle.com/javase/tutorial/networking/urls/readingWriting.html
			        	//i am posting the data via the POST method
			        	 
			        	 try {
							
							
							//i am encoding the values into base64 encoding to provide a certain amount of security.
			        		//i do not want to send the data as plain text over the airwaves
			        		//i have chosen base64 instead of other encoding schemes such as md5 has, because i want 
			        		//a 2 way encryption scheme which can be encoded and also be decoded because on the server side
			        		//i need to decode the data to be able to perform operations
			        		 
			        		String latitudeString;
			        		String longitudeString;
			        		
							latitudeString=Base64.encodeToString(latitude.toString().getBytes(),Base64.DEFAULT);
							longitudeString=Base64.encodeToString(longitude.toString().getBytes(),Base64.DEFAULT);
																		
							 byte [] decodedBytes;
							 String data;
				             String decodedString;
				             StringBuffer stringComplete=new StringBuffer();
				             
				             URL url = new URL("http://worldofpakistan.net/tagar/getnearesttags.php");
				             URLConnection connection = url.openConnection();
				             connection.setDoOutput(true);
				            
				             OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				             out.write("latitude="+latitudeString+"&longitude="+longitudeString);//i am sending data via the post method
				             out.close();
				
				             BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				             		             
				             while ((data = in.readLine()) != null) 
				             {
				            	 stringComplete.append(data);
	
				             }//end while
						        			        	             
				             in.close();
				             
				             decodedBytes=Base64.decode(stringComplete.toString(),Base64.DEFAULT);
				             decodedString=new String(decodedBytes);
				            // Toast.makeText(this, decodedString, 1000).show();
				            
				             //Log.d("tagAR", decodedString);
				             
				             int pos=decodedString.indexOf("],",0);
				             int pos1=0;
				             
			            	 //Log.d("pos", ((Integer)pos).toString());
			            	 //Log.d("pos1", ((Integer)pos1).toString());
			            	 
				             String num;
				             Integer numOfItems;
				             try
				             {
				            	 num=decodedString.substring(2, pos);//i am starting from 2 because [[1 to get 1 i will have to place the pointer just before 1, which lies at 2. at 0 lies the first [, at 1 lies the second [, and at 2 lies the 1(or the first character of the number in the string 
				            	 numOfItems=Integer.parseInt(num);
				             }//end if
				             catch(Exception e)
				             {
				            	 numOfItems=-3;
				             }//end else
				             
				             
				             if(numOfItems==1)
				             {
				            	 
						            progressDialog.dismiss();
						             
				            	 
				             }//end if
				             else if(-3==numOfItems)
				             {
				            	 progressDialog.dismiss();
				             }//end else if
				             else
				             {
				            	 pos=pos+3;
				            	 pos1=decodedString.indexOf("],",pos);
				            	 num=decodedString.substring(pos, pos1);
					             Integer numOfRows=Integer.parseInt(num);
					             pos=pos1+3;
					             
					             String sub="";
				         		
				         	      tag=new Tag[numOfRows];
			         	        
					         	  Float bearing=0.0f;
					         	  Location l;
					         	  Location l1;
					         	   
				         	   
				         	   
					             for(int i=0;i<numOfRows;i++)
					             {
					            	 	tag[i]=new Tag();
						            	 pos1=decodedString.indexOf("]", pos);
						            	//Log.d("pos", ((Integer)pos).toString());
						            	//Log.d("pos1", ((Integer)pos1).toString());
						            	 sub=decodedString.substring(pos, pos1);
						            	//Log.d("id", sub);
						            	 pos=pos1+3;
						            	 tag[i].setId(Integer.parseInt(sub));
				            	 		
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("distance", sub);
							             pos=pos1+3;
							             tag[i].setDistance(Double.parseDouble(sub));
							             
							             pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("latitude", sub);
							             pos=pos1+3;
				            	 		 tag[i].setLatitude(Double.parseDouble(sub));
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("longitude", sub);
							             pos=pos1+3;
				            	 		 tag[i].setLongitude(Double.parseDouble(sub));
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	 //Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							             //Log.d("direction", sub);
							             pos=pos1+3;
							             tag[i].setX(Float.parseFloat(sub));//save the direction/bearing.
							            /**
							             * Over here i am calculating the bearing of the two different geo-location coordinate points.
							             * This is basically the angle at which the other point is form my current location. I need this bearing
							             * because i need to in which direction i have to face to be able to know the actual direction.
							             * Initially i was taking the x value of the accelerometer but that was the wrong technique as there would be cases
							             * where i would not be looking at the tagged coordinate point, but would instead be looking away from it, as 
							             * the value that i would be using is the one where i am looking away from the tagged location when i tagged that location.    
							             *i found the bearingTo function here http://developer.android.com/reference/android/location/Location.html and got a hint from 
							             *here to what should be the parameter in the Location constructor when i create a Location object. The hint was to use any string instead
							             *of the provider (GPS or network) as i will be using my own latitude and longitude values.
							             *http://stackoverflow.com/questions/4945697/creating-android-location-object 
							             */
							             
							            l=new Location(" ");
							     		l.setLatitude(latitude);
							     		l.setLongitude(longitude);
							     		
							     		l1=new Location(" ");
							     		l1.setLatitude(tag[i].getLatitude());
							     		l1.setLongitude(tag[i].getLongitude());
							     		
							     		bearing=l.bearingTo(l1);
							     		
							     		if(bearing<0)
							     		{
							     			bearing=(-1)*bearing;
							     			bearing=180+(180-bearing);
							     		}//end if
							             
							     		/**
							     		 * 
							     		 * if the distance between the user's current location and the retrieved location values is less than 0.8km then use the 
							     		 * camera's direction value, else use the bearing. the value 0.8km is not a special value. i have taken this because 
							     		 * my error of accuracy of the network provider is around 0.8km. this value may be different depending on your mobile device and
							     		 * internet connection being used on the device. By using the network provider instead of the gps i get quite a bit of error in 
							     		 * accuracy.
							     		 */
							     		
							     		 if(tag[i].getDistance()<0.8)
 							             {
 							            	 tag[i].setX(tag[i].getX());
	 				            	 		 tag[i].setY(-1);
	 				            	 		 tag[i].setZ(84);
 							             }
 							             else
 							             {
	 				            	 		 tag[i].setX(bearing);
	 				            	 		 tag[i].setY(-1);
	 				            	 		 tag[i].setZ(84);
 							             }//end else
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("comment", sub);
							             pos=pos1+3;
				            	 		 tag[i].setComment(sub);
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("locality", sub);
							             pos=pos1+3;
				            	 		 tag[i].setLocality(sub);
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	//Log.d("pos", ((Integer)pos).toString());
							            //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("city", sub);
							             pos=pos1+3;
				            	 		 tag[i].setCity(sub);
				            	 		 
				            	 		 pos1=decodedString.indexOf("]", pos);
		        	 	            	 //Log.d("pos", ((Integer)pos).toString());
							             //Log.d("pos1", ((Integer)pos1).toString());
							             sub=decodedString.substring(pos, pos1);
							            //Log.d("country", sub);
							             pos=pos1+3;
				            	 		 tag[i].setCountry(sub);
				            	 		  
				            	 		 tags.add(tag[i]);
				            	 		
						            	
						            	 //Log.d("i",((Integer)i).toString() );
						            	 
						             
					             }//end for
					           
					             progressDialog.dismiss();
					            
				            	 
				             }//end else
    				             
    				            		
    				} catch (UnsupportedEncodingException e) {
    					
    					//e.printStackTrace();
    				} catch (MalformedURLException e) {
    					
    					//e.printStackTrace();
    				} catch (IOException e) {
    					
    					//e.printStackTrace();
    				}
    				
    				}
    				
    			}.start();
    					 
    			
				
				
				 accuracyTextDialog.setText(accuracy.toString()+" meters");
				 yourLatitudeTextDialog.setText(latitude.toString());
				 yourLongitudeTextDialog.setText(longitude.toString());
				 dialogYourInfoTag.show();

				
			}//end else if


			return super.onKeyDown(keyCode, event);
		}

	
		
/**
 *  
 * @author mustafa
 *
 *This is my CustomView class which allows me to draw the drawables and animate them.
 */

		public class CustomView extends View {

	    	
	    	private Matrix matrix = new Matrix();
	    	private Bitmap tag;
	    	private Bitmap tagit;
	    	private Bitmap crossHair;
	    	private Bitmap refreshButton;
			private Resources res = this.getResources();
			private Paint paint=new Paint();

	    	 
	    	public CustomView(Context context) {
	    		super(context);
	    		// TODO Auto-generated constructor stub
	    		//matrix.reset();
	    		
	    		paint.setStyle(Paint.Style.FILL);
	    		paint.setAntiAlias(true);
	    		paint.setColor(Color.BLACK);
	    		tag = BitmapFactory.decodeResource(res, R.drawable.tag);
	    		tagit = BitmapFactory.decodeResource(res, R.drawable.tagit);
	  			crossHair=BitmapFactory.decodeResource(res, R.drawable.crosshair);
	  			refreshButton=BitmapFactory.decodeResource(res, R.drawable.refreshbutton);
	    	}

	    	

	    	@Override
	    	protected void onDraw(Canvas canvas) {
	    		super.onDraw(canvas);
	    		
	    		    		 
	    		
	    		/**
	    		 * 
	    		 * source for translating 
	    		 * http://www.java2s.com/Code/Android/2D-Graphics/TranslateImage.htm
	    		 * 
	    		 * The matrix allows us to apply the translation to the bitmap
	    		 * 
	    		 * a 3 by 3 matrix is defined when the Matrix object is created.
	    		 * http://developer.android.com/reference/android/graphics/Matrix.html
	    		 * 
	    		 * 1 0 0
	    		 * 0 1 0
	    		 * 0 0 1
	    		 * 
	    		 * I am using canvas and drawables to show the the graphics. I am not using OpenGL ES due to compatibility issues and
	    		 * device fragmentation. I have to yet to this new system of graphics, though i have tested it oon the Android emulator 
	    		 * with different screen sizes and shows the graphics. Also i am using the width and height of the screen to draw the graphics.
	    		 * This allows the graphics to be scaled down according to the screen size.
	    		 * 
	    		 * Change from version 0.2 to version 0.3
	    		 * 
	    		 * I am now only drawing the tags in the onDraw method as i need to draw them in real time. I am drawing the other icons and buttons
	    		 * in the onCreate function using FrameLayouts and LinearLayouts to be able to position them and scale them according to the screen size.
	    		 */
	    		
		    		    		
	    		
	    		int i=0;
	    		int spaceBetweenTags=0;
	    		
				while(null!=tags && i<tags.size())
				{
					matrix.reset();
				
					 if(tags.get(i).getDiffX()>=-20 && tags.get(i).getDiffX()<=20)
					 {
						 //canvas.save();
						 matrix.setTranslate(height/2,height/2);//i am drawing the tag at the center of the screen
						 //matrix.setTranslate(0,140);
						 
						// matrix.preTranslate(-tags.get(i).getDiffX(),0);
						 //matrix.preTranslate(5,0);
			    		 matrix.preTranslate(((-tags.get(i).getDiffX())*3)+50+spaceBetweenTags,0);
			         	 //canvas.drawText(tags.get(i).getTitle(),(-tags.get(i).getDiffX())+(height/2),130, paint);
			         	 //canvas.drawText(tags.get(i).getComment().substring(0, (tags.get(i).getComment().length()/2))+"...",((-tags.get(i).getDiffX())*3)+50+(height/2),130, paint);
			    		 canvas.drawText(tags.get(i).getComment().substring(0, (tags.get(i).getComment().length()/2))+"...",((-tags.get(i).getDiffX())*3)+50+spaceBetweenTags+(height/2),height/2,paint);
			             canvas.drawBitmap(tag, matrix, paint);
						 //canvas.restore();
			         	
			             tags.get(i).setShownOnScreen(true);//the tag is shown
						 //Log.d("tags"+((Integer)i).toString(),((Float)((-tags.get(i).getDiffX()))).toString());
			         	 //Log.d("tags with addition"+((Integer)i).toString(),((Float)((-tags.get(i).getDiffX())+(height/2))).toString());
					 
			             spaceBetweenTags=spaceBetweenTags+50;
					 }//end if
					 else
					 {//if the tag is not shown
						 
						 /**
						  *Changes in Version 0.4 (Addition) 
						  * 
						  */
					 
						 tags.get(i).setShownOnScreen(false);
					 }//end else
					 
					i++;
					
				}//end while
	     		 
				//invalidate();//this function will call the onDraw again. In OpenGL ES the equivalent is the statement glSurface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

				
	    	}



			@Override
			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				super.onSizeChanged(w, h, oldw, oldh);
				
				width=w;
				height=h;
				
			}
	    	

	    }
	
}


