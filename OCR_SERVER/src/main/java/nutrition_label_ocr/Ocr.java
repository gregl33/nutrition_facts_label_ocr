package nutrition_label_ocr;



import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;


public class Ocr {

    public Ocr(String imgname, String filePath) {
    	imgSrcPath = filePath + imgname;
    	 fileName_ = imgname;
    	 fileToSaveImgs = filePath;
    	 
    	this.ocr_process();//String fileName,String imgSrcPath)
    }
    
    
	
    static {
    	nu.pattern.OpenCV.loadShared();
    }
 
    
    
    
    
    
    private String fileName_ = "";
    private String fileToSaveImgs = "";
    
    private List<Nutrition> Energy = new ArrayList<>();
    private Nutrition serving_type = new Nutrition();
    private Nutrition fat = new Nutrition();
    private Nutrition of_which_saturates = new Nutrition();
    private Nutrition carbohydrate = new Nutrition();
    private Nutrition of_which_sugars = new Nutrition();
    private Nutrition protein = new Nutrition();
    private Nutrition salt = new Nutrition();

    

	public List<Nutrition> getEnergy() {
		return Energy;
	}


	public Nutrition getServing_type() {
		return serving_type;
	}


	public Nutrition getFat() {
		return fat;
	}


	public Nutrition getOf_which_saturates() {
		return of_which_saturates;
	}


	public Nutrition getCarbohydrate() {
		return carbohydrate;
	}


	public Nutrition getOf_which_sugars() {
		return of_which_sugars;
	}


	public Nutrition getProtein() {
		return protein;
	}


	public Nutrition getSalt() {
		return salt;
	}





	private static String imgSrcPath = "";
    
	
	private void MatchKeyValue(String key_value) {
		
		
		System.out.println("* MatchKeyValue: " + key_value);

		
	
		
	    String[] key_arr;
	    key_arr = new String[] { "energy", "fat", "of which saturates",
	    		"carbohydrate", "of which sugars", "protein", "salt","per"
	    		};
		
	    
	    List<String> input_txt = new ArrayList<>();
	    key_value  = key_value.toLowerCase();
	    key_value = key_value.replaceAll("[-+^:\\[,\\]*'$£#Ω≈ç`!@€?><±§~√\\\";|{&}_-]","");
	    key_value = key_value.replaceAll("(?=[a-zA-Z0-9]*\\.$)\\.|(?=^\\.[a-zA-Z0-9]*)\\.","");
		
//		System.out.println("** MatchKeyValue tr:" + key_value);

		
		Pattern p = Pattern.compile("[^\\s*].*[^\\s*]");
		Matcher m = p.matcher(key_value);
		
		while (m.find()) {
			input_txt.add(m.group());
		}
		
		double highest_match = 0;
		String highest_match_txt = "";

		String key = "";
		String value = "";
		
		String temp = "";//input_txt.toString().;

		
		for (String string : input_txt) {
			
			System.out.println("");
			temp += string + " ";
			for (String match_key : key_arr) {

				if(!string.isEmpty() && string != " ") {
			
					JaroWinklerDistance t = new JaroWinklerDistance();
					double match = t.apply(string, match_key);
					
					System.out.println("*** Match: " + string + " & " + match_key + " = " + match);
		
					if(match > highest_match) {
						highest_match = match;
						key = match_key;
						highest_match_txt = string;
					}
				}
			}
		}
		
		System.out.println("**********************");

		System.out.println("** MatchKeyValue tr: " + temp);

		
//		String temp = input_txt.toString().;
		temp = temp.replace(highest_match_txt, "");
		
		value = temp.replaceAll("[oO]", "0");
		
		System.out.println("**********************");

		System.out.println("{"+key+":"+value+"}");
		
    		
		switch (key) {
		case "energy":
			if(this.Energy.isEmpty() || !this.Energy.isEmpty() && highest_match > this.Energy.get(0).getMatch_strength()) {				
				this.Energy.clear();
				
				String[] energies = value.split("[/]");

				for (String value_one : energies) {
					Nutrition ener = new Nutrition();
					ener = assignNutritionValue(value_one,ener);
					ener.setMatch_strength(highest_match);
					this.Energy.add(ener);
					
				}
			}
			break;
			
		case "fat":
			if(highest_match > this.fat.getMatch_strength()) {
				this.fat = assignNutritionValue(value,fat);
				this.fat.setMatch_strength(highest_match);
			}
			break;
			
		case "of which saturates":
			if(highest_match > this.of_which_saturates.getMatch_strength()) {
				this.of_which_saturates = assignNutritionValue(value,of_which_saturates);
				this.of_which_saturates.setMatch_strength(highest_match);
			}
			break;
			
		case "carbohydrate":
			if(highest_match > this.carbohydrate.getMatch_strength()) {
				this.carbohydrate = assignNutritionValue(value,carbohydrate);
				this.carbohydrate.setMatch_strength(highest_match);
			}
			break;
			
		case "of which sugars":
			if(highest_match > this.of_which_sugars.getMatch_strength()) {
				this.of_which_sugars = assignNutritionValue(value,of_which_sugars);
				this.of_which_sugars.setMatch_strength(highest_match);
			}
			break;
			
		case "protein":
			if(highest_match > this.protein.getMatch_strength()) {
				this.protein = assignNutritionValue(value,protein);
				this.protein.setMatch_strength(highest_match);
			}
			break;
			
		case "salt":
			if(highest_match > this.salt.getMatch_strength()) {
				this.salt = assignNutritionValue(value,salt);
				this.salt.setMatch_strength(highest_match);
			}
			break;

		case "per":
			if(highest_match > this.serving_type.getMatch_strength()) {
				this.serving_type.setValue(value);
				this.serving_type.setMatch_strength(highest_match);
			}
			break;
		default:
			break;
		}
		
		
	}
    
   
    private Nutrition assignNutritionValue(String value, Nutrition nutri) {
    
		nutri.setValue(getNumber(value));
		nutri.setUnit(getChars(value));

    	return nutri;
    }
    
	private static String getNumber(String text){
		Pattern p = Pattern.compile("[^\\s]*[^\\s]");
		Matcher m = p.matcher(text);
		if(m.find()) {
			text = m.group(0);
		}
		return text.replaceAll("[^0-9.]", "");
	}

	private static String getChars(String text){
		Pattern p = Pattern.compile("[^\\s]*[^\\s]");
		Matcher m = p.matcher(text);
		if(m.matches()) {
			text = m.group(0);
		}
		return text.replaceAll("[\\d.]", "");
	}
   
    private boolean contains(List<Integer> list, int name) {
	    for (Integer item : list) {
	        if (item.equals(name)) {
	            return true;
	        }
	    }
	    return false;
	}


	 private boolean contains(List<Double> list, double name) {
		    for (Double item : list) {
		        if (item.equals(name)) {
		            return true;
		        }
		    }
		    return false;
	}
	   
	   
	   
	   private static  List<Integer> peakFinder (List<Integer> arr){
		
		List<Integer> result = new ArrayList<Integer>();
		
		for(int i = 0; i < arr.size(); i++) {
			int val = arr.get(i);
			int next = i < arr.size()-1 ? arr.get(i+1) : arr.get(arr.size()-1);
			int prev = i >= 1 ? arr.get(i-1) : arr.get(i);
			if(next < val && prev < val || (i == 0 && next < val) || (i == arr.size()-1 && prev < val)) {
				result.add(val);
			}
		}

		return result;
		
	}
	   
	   
	   
	   
	   
		private String doOcr(Mat img){
			ITesseract instance = new Tesseract();
			
			//				Mat m = img.clone();
			
			int type = BufferedImage.TYPE_BYTE_GRAY;
			
			if ( img.channels() > 1 ) {
				type = BufferedImage.TYPE_3BYTE_BGR;
			}
			int bufferSize = img.channels()*img.cols()*img.rows();
			byte [] b = new byte[bufferSize];
			img.get(0,0,b); 
			BufferedImage image = new BufferedImage(img.cols(),img.rows(), type);
			final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			System.arraycopy(b, 0, targetPixels, 0, b.length); 
			
			
			instance.setDatapath("src/main/resources/tessdata/"); 
			instance.setLanguage("eng");
			
			try {
				return instance.doOCR(image);
			} catch(TesseractException e) {
				System.out.println(e);
			}
			return "";
				
		                        
		}
	   
	   
	   
		private  void showWaitDestroy(String winname, Mat img) {
			String fileNameWithOutExt = FilenameUtils.removeExtension(fileName_);	
			Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__"+winname+".jpg",img);
		}
	 
	   
	   
	   
	   
	   
	   
	   
	   
	    private double detectRotationAngle(Mat binaryImage) {
	     //Store line detections here
	     Mat lines = new Mat();
	     //Detect lines
	     Imgproc.HoughLinesP(binaryImage, lines, 1, Math.PI / 180, 100);

	     double angle = 0;

	     //This is only for debugging and to visualise the process of the straightening
	     Mat debugImage = binaryImage.clone();
	     Imgproc.cvtColor(debugImage, debugImage, Imgproc.COLOR_GRAY2BGR);

	     //Calculate the start and end point and the angle
	     for (int x = 0; x < lines.cols(); x++) {
	         double[] vec = lines.get(0, x);
	         double x1 = vec[0];
	         double y1 = vec[1];
	         double x2 = vec[2];
	         double y2 = vec[3];

	         Point start = new Point(x1, y1);
	         Point end = new Point(x2, y2);

	         //Draw line on the "debug" image for visualization
	         Imgproc.line(debugImage, start, end, new Scalar(255, 255, 0), 5);

	         //Calculate the angle we need
	         angle = calculateAngleFromPoints(start, end);
	     }

//	     Imgcodecs.imwrite("detectedLines.jpg", debugImage);

	     return angle;
	 }
	    
	     //From an end point and from a start point we can calculate the angle
	 private double calculateAngleFromPoints(Point start, Point end) {
	     double deltaX = end.x - start.x;
	     double deltaY = end.y - start.y;
	     return Math.atan2(deltaY, deltaX) * (180 / Math.PI);
	 }
	    
	   //Rotation is done here
	 private Mat rotateImage(Mat image, double angle) {
	     //Calculate image center
	     Point imgCenter = new Point(image.cols() / 2, image.rows() / 2);
	     //Get the rotation matrix
	     Mat rotMtx = Imgproc.getRotationMatrix2D(imgCenter, angle, 1.0);
	     //Calculate the bounding box for the new image after the rotation (without this it would be cropped)
	     Rect bbox = new RotatedRect(imgCenter, image.size(), angle).boundingRect();

	     //Rotate the image
	     Mat rotatedImage = image.clone();
	     Imgproc.warpAffine(image, rotatedImage, rotMtx, bbox.size());

	     return rotatedImage;
	 }  
	    
	   
	 
	 private List<Rect> mergeObj (List<Rect> elems_) {
		
		List<Rect> cluster_ = new ArrayList<Rect>();
		
	 	for (Rect rect : elems_) {
	 		
//	 		if(contains(final_list_2,rect.height)) {
	 		boolean matched = false;
	 	 	for (Rect cluster : cluster_) {

				 if(rect.x <= cluster.x + cluster.width && rect.x + rect.width >= cluster.x && rect.y <= cluster.y + cluster.height && rect.y + rect.height >= cluster.y){

					matched = true;
									
		  	 	 	int x  = Math.min( cluster.x,   rect.x   );
		            int y  = Math.min( cluster.y,  rect.y  );
		            int height = Math.abs(Math.max((rect.height+rect.y), (cluster.height+cluster.y))- y);
		            int width = Math.abs(Math.max((rect.width + rect.x), (cluster.width+cluster.x))- x);

		            cluster.x  = x;
		            cluster.y  = y;
		            cluster.height = height;
		            cluster.width = width;
		       	    
				 }
	 	 	} 
	 	 	if(!matched) {
	 	 		if(rect.height >= 5 && rect.width >= 5) {
	 	 			cluster_.add(rect);
	 	 		}
	 	 	}
//	 	}
	 	}
	 	
	 	return cluster_;
	 	
	 	
	}
	 
	 
	 
	 
	 
	 /*****************************************************************************/
	   
	    private void ocr_process(){
	    
		
		long startTime = System.nanoTime();   

		
		
//		System.load("/path/to/opencv/opencv_build/lib/libopencv_java320.dylib");

		
		

//		fileName_ = fileName;
//		hashMap compMap = hm;

		
		
//		String inputFile = path+fileName;

//		String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);	

//	       InputStream is = OCR.class.getResourceAsStream(fileName);
	        
			Imgcodecs.imread(imgSrcPath);
		
	         Mat src = Imgcodecs.imread(imgSrcPath);//Imgcodecs.imread(args[0]);

	         Imgproc.pyrDown(src, src);


	    Mat gray = new Mat();

	    
	    Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

	    
	    Mat blur = new Mat();
	    		
	    Imgproc.blur(gray, blur,new Size(5, 5)); //With kernel size depending upon image size
	    double mean_blur = Core.mean(blur).val[0];
	    System.out.println("mean_blur: " +mean_blur);
	    String light_dark = "";
	    boolean darkBackround = false;
	    if(mean_blur > 120) {
	    	light_dark = "light";
	    	darkBackround = false;
	    }else {
	    	light_dark = "dark";
	    	darkBackround = true;

	    }
	    
	    System.out.println("light_dark: " +light_dark);

	    		    		
	    		    		
	    showWaitDestroy("gray" , gray);
	    Mat bw_2 = new Mat();
	    Core.bitwise_not(gray, gray);
	    showWaitDestroy("bitwise_not" , gray);

	 
	    // Show binary image
	    Imgproc.threshold(gray, bw_2, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
	    showWaitDestroy("binary" , bw_2);

	    
	    if(darkBackround) {
	    	Core.bitwise_not(bw_2, bw_2);
	    	showWaitDestroy("bitwise_not_2" , bw_2);
		}
	    
	    
	 
	    Mat horizontal = bw_2.clone();
	    Mat vertical = bw_2.clone();
	    // Specify size on horizontal axis
	    
	    int horizontal_size = horizontal.cols() / 15;
	    // Create structure element for extracting horizontal lines through morphology operations
	    Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontal_size,1));
	    // Apply morphology operations
	    Imgproc.erode(horizontal, horizontal, horizontalStructure);
	    Imgproc.dilate(horizontal, horizontal, horizontalStructure);
	    // Show extracted horizontal lines
	    showWaitDestroy("horizontal" , horizontal);
	    
	    // Specify size on vertical axis
	    int vertical_size = vertical.rows() / 5;
	    // Create structure element for extracting vertical lines through morphology operations
	    Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,vertical_size));
	    // Apply morphology operations
	    Imgproc.erode(vertical, vertical, verticalStructure);
	    Imgproc.dilate(vertical, vertical, verticalStructure);
	    // Show extracted vertical lines
	    showWaitDestroy("vertical", vertical);

	    		
	    Mat testing = new Mat();

	    
	    Core.subtract(bw_2, horizontal, testing);

	    Core.subtract(testing, vertical, testing);

//		 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__testing.jpg",testing);

	    

	        int erosion_size = 2;
		
	    // Mat large = Imgcodecs.imread(inputFile),
		Mat	 rgb = new Mat(),
			 small =  new Mat(),
			 kernel =  new Mat(),
			 grad = new Mat(),
			 bw = new Mat(),
			 connected = new Mat(),
			 hierarchy  =  new Mat();//,
//			 rgb2 = new Mat();
	     List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		 List<Rect> elems = new ArrayList<Rect>();
//		 Set<Rect> setUnsorted = new HashSet<Rect>();
		 List<Rect> dupl= new ArrayList<Rect>();


		 
		 
		 
//		 Mat processed = preProcessForAngleDetection(testing);

		 
		 bw = testing;
//		 small = straightImage;
//	     Imgproc.pyrDown(large, rgb);
//	     Imgproc.cvtColor(large,small, Imgproc.COLOR_BGR2GRAY);
//	     Imgproc.cvtColor(straightImage,small, Imgproc.COLOR_BGR2GRAY);

//		 small = lines_removed;
//	     displayImage(small,"img_1","grey",new Size(small.size().width,small.size().height),false);
//		 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__grey.jpg",small);

//		  double rotationAngle = detectRotationAngle(testing);
//			
//		  Mat straightImage = rotateImage(src, rotationAngle);
//		  showWaitDestroy("straightImage" , straightImage);
	//  
//		 Imgproc.cvtColor(straightImage,small, Imgproc.COLOR_BGR2GRAY);
//	     Mat test = small.clone();
//	     Mat test2 = small.clone();
//	     Mat test3 = small.clone();
	//
//	     Mat OCR_IMG = small.clone();

//	     
//	     String ocr = doOcr(small);
//		 System.out.println("ocr: " + ocr);

	     
	     
//	     
//	     
//	     
//	     Mat test_lines = small.clone();
	//
//	     
//	     Mat cannyEdges = new Mat();
//	     Mat lines = new Mat();
	//    
	//
//	     
//	     
	     
	     
	//
//		 Imgproc.cvtColor(small,test,Imgproc.COLOR_GRAY2BGR);			 
//		 Imgproc.cvtColor(small,test2,Imgproc.COLOR_GRAY2BGR);			 
//		 Imgproc.cvtColor(small,test3,Imgproc.COLOR_GRAY2BGR);			 

	     
	     
//	     Size kernelSize_1 = new Size(3,3);
//	      kernel =  Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, kernelSize_1);
//	     
//	      
////	      small = testing;
//	      Imgproc.morphologyEx(small, grad, Imgproc.MORPH_GRADIENT, kernel);
	//
//	      displayImage(grad,"img_2","morphologyEx",new Size(grad.size().width,grad.size().height),false);
//	 	 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__morphologyEx.jpg",grad);
	//
//	      Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
//	      displayImage(bw,"img_3","threshold",new Size(bw.size().width,bw.size().height),false);
//	 	 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__threshold.jpg",bw);
	// 
//	      
//	 	 bw = testing;
	      
			Mat erosion_element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new  Size(erosion_size , erosion_size));
	        Imgproc.erode(bw, bw, erosion_element);
			 showWaitDestroy("erode_bw" , bw);

	        
	        Size kernelSize_2 = new Size(9,1);

	      kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, kernelSize_2);

	      Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE, kernel);
//	      displayImage(connected,"img_4","morphologyEx_2",new Size(connected.size().width,connected.size().height),false);
//	  	 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__morphologyEx_2.jpg",connected);
		 showWaitDestroy("morphologyEx_2" , connected);

	  	 
	  	 
	  	 
	  	 
//	  	 Mat lines = new Mat();
//	  	 
//	     Imgproc.HoughLinesP(connected, lines, 1, Math.PI / 180, 100);
	//
//	     double angle = 0;
//	     
//	     Mat debugImage = connected.clone();
//	     Imgproc.cvtColor(debugImage, debugImage, Imgproc.COLOR_GRAY2BGR);
//	     
//	     for (int x = 0; x < lines.cols(); x++) {
//	         double[] vec = lines.get(0, x);
//	         double x1 = vec[0];
//	         double y1 = vec[1];
//	         double x2 = vec[2];
//	         double y2 = vec[3];
	//
//	         Point start = new Point(x1, y1);
//	         Point end = new Point(x2, y2);
	//
//	         //Draw line on the "debug" image for visualization
//	         Imgproc.line(debugImage, start, end, new Scalar(255, 255, 0), 5);
	//
//	         //Calculate the angle we need
////	         angle = 
//	        		 
//	        double deltaX = end.x - start.x;
//	         double deltaY = end.y - start.y;
//	         angle = Math.atan2(deltaY, deltaX) * (180 / Math.PI);
//	         
////	         calculateAngleFromPoints(start, end);
//	         
//	         System.out.println("angle: " + angle);
//	     }
//	     
//	 	 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__lines.jpg",debugImage);
	//
//	 	 
//	 	 
//	 	 
//	 	 
//	 	 
//	     //Calculate image center
//	     Point imgCenter = new Point(connected.cols() / 2, connected.rows() / 2);
//	     //Get the rotation matrix
//	     Mat rotMtx = Imgproc.getRotationMatrix2D(imgCenter, angle, 1.0);
//	     //Calculate the bounding box for the new image after the rotation (without this it would be cropped)
//	     Rect bbox = new RotatedRect(imgCenter, connected.size(), angle).boundingRect();
	//
//	     //Rotate the image
//	     Mat rotatedImage = connected.clone();
//	     Imgproc.warpAffine(connected, rotatedImage, rotMtx, bbox.size());
//	     
//	 	 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__rotatedImage.jpg",rotatedImage);
	//
//	     
//	     
	     
	  	
//	      Imgproc.findContours(connected, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
			 List<Integer> all_heights = new ArrayList<Integer>();


			 List<Double> all_areas = new ArrayList<Double>();


			 
			 
			 
			 //https://gaborvecsei.wordpress.com/2016/08/29/straighten-image-with-opencv/
			 
			 double rotationAngle = detectRotationAngle(connected);
				
			  Mat straightImage = rotateImage(src, rotationAngle);
			  Mat straightImageConnected = rotateImage(connected, rotationAngle);

			  showWaitDestroy("straightImage" , straightImage);
		//
			 Imgproc.cvtColor(straightImage,small, Imgproc.COLOR_BGR2GRAY);
			 Imgproc.cvtColor(small,straightImage, Imgproc.COLOR_GRAY2BGR);

			 Mat test = straightImage.clone();
			 Mat test2 = straightImage.clone();
			 Mat test3 = straightImage.clone();
		
			 Mat OCR_IMG = straightImage.clone();

		  
//			 String ocr = doOcr(straightImage);
//			 System.out.println("ocr: " + ocr);
			 
//			 Imgproc.cvtColor(small,test,Imgproc.COLOR_GRAY2BGR);			 
//			 Imgproc.cvtColor(small,test2,Imgproc.COLOR_GRAY2BGR);			 
//			 Imgproc.cvtColor(small,test3,Imgproc.COLOR_GRAY2BGR);	
			 
			 
			 
		      Imgproc.findContours(straightImageConnected, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

			 
			 
	     //first sort of found objects 
	 	 for(int idx = 0; idx < contours.size();idx++){
	 	 	  Rect rect = Imgproc.boundingRect(contours.get(idx));
	 	 	  
	 	 	  if(rect.height > 20  && rect.height < 100) {
	 	 		  elems.add(rect);
	 	 	   	 	  
	 	 	  
	 	 	  if(!contains(all_heights,rect.height)) {
	 	  	 	all_heights.add(rect.height);

	 	 	  }
	 	 	  
	 	 	  if(!contains(all_areas,rect.area())) {
	 	 		all_areas.add(rect.area());
	 	 	  }

		 	 	Imgproc.rectangle(test, rect.br(),new Point( rect.br().x-rect.width ,rect.br().y-rect.height), new Scalar(0,0,255));
	 	 	  }
		 	 
	 	 }
	 	 
	 	 
	 	 
//			displayImage(test,"img_5","all ("+contours.size()+")",new Size(test.size().width,test.size().height),false);
//		    Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__allBoxes.jpg",test);
			  showWaitDestroy("allBoxes_filtered" , test);

		    
		    
		    
	   
		 
		    
		 Collections.sort(elems,new Comparator<Rect>() {
		    	
				public int compare(Rect o1, Rect o2) {

					   int result = Double.compare(o1.x,o2.x);
				    if ( result == 0 ) {
				 	   result = Double.compare(o1.y,o2.y);
				 	   if(result == 0) {
				 		  dupl.add(o2);
				 	   }
				    }
					   return result;
				}
			});
		 System.out.println("before: " + elems.size());

		 //removing duplicates 
		 for(Rect del:dupl) {
			 elems.remove(del);
		 }
		 System.out.println("after: "+elems.size());

	 	 
		 
	     Collections.sort(all_areas);
	     double sum_all_areas = 0;
	     for (int i = 0; i < all_areas.size(); i++) {
	    	 sum_all_areas += all_areas.get(i);
	     }
	     double mean_all_areas = sum_all_areas / all_areas.size();  
	     
	     System.out.println("mean_all_areas: " +mean_all_areas);
	     System.out.println("all_areas: " +all_areas.toString());

	     
	     

	     //***Remove Outliers Using Normal Distribution and S.D.
//	     Collections.sort(all_heights);
//	     double sum = 0;
//	     for (int i = 0; i < all_heights.size(); i++) {
//	         sum += all_heights.get(i);
//	     }
//	     double mean = sum / all_heights.size();         
//	     double standardDeviation = 0.0;     
//	     for(int num: all_heights) {
//	         standardDeviation += Math.pow(num - mean, 2);
//	     }
	//
//	     double sd = Math.sqrt(standardDeviation/all_heights.size());
	//
//	    				 List<Integer> final_list = new ArrayList<Integer>();
//	    				 List<Integer> final_list_2 = new ArrayList<Integer>();
	//
//	    			 	 for(int fl = 0; fl < all_heights.size();fl++){
//	    			 		if (all_heights.get(fl) > mean - 2 * sd) {
//	    			 			final_list.add(all_heights.get(fl));
//	    			 		}
//	    			 	 }
//	    			 	 
//	    			 	 for(int fl = 0; fl < final_list.size();fl++){
//	     			 		if (final_list.get(fl) < mean + 2 * sd) {
//	     			 			final_list_2.add(final_list.get(fl));
//	     			 		}
//	     			 	 }
	//
//	     
//	    			     System.out.println("sd: " + sd);
//	    			     System.out.println("mean: " + mean);
	//
//	    System.out.println("all_heights: " +all_heights.size() + " - " +all_heights.toString());
	//
//		 System.out.println("final_list_2: " +final_list_2.size() + " - " + final_list_2.toString());


		 
		 List<Rect> merged_temp = mergeObj(elems);
		 List<Rect> merged = mergeObj(merged_temp);
		 System.out.println("merged_temp_10: " +  merged_temp.size());
		 System.out.println("merged: " +  merged.size());
		 int ttt = 0;
		 while( !merged.equals(merged_temp) ) { 
			 merged_temp = merged;
			 merged = mergeObj(merged_temp);
			 System.out.println(ttt + ". merged_temp: " +  merged_temp.size() + " / merged: " +  merged.size());
			 ttt++;
		 } 
		 
		 System.out.println("final merged: " +  merged.size());

		 
		    
		    
//				//sort by x
			 Collections.sort(merged,new Comparator<Rect>() {
		    	
				public int compare(Rect o1, Rect o2) {

					   int result = Double.compare(o1.x,o2.x);
				    if ( result == 0 ) {
				 	   result = Double.compare(o1.y,o2.y);
				 	   
				    }
					   return result;
				}
			});
//		    
		 
//			//sort by y
//		 Collections.sort(merged,new Comparator<Rect>() {
	// 	
//			public int compare(Rect o1, Rect o2) {
	//
//				   int result = Double.compare(o1.y,o2.y);
//			    if ( result == 0 ) {
//			 	   result = Double.compare(o1.x,o2.x);
//			 	   
//			    }
//				   return result;
//			}
//		});
	 


			 for(int idx = 0; idx < merged.size();idx++){
		 		 
		 		 Imgproc.rectangle(test2, merged.get(idx).br(),new Point( merged.get(idx).br().x-merged.get(idx).width ,merged.get(idx).br().y-merged.get(idx).height), new Scalar(0,0,255));

		 		 
//	  	 	 	  System.out.println(idx + ". rect - x: " + merged.get(idx).x +", y: " +merged.get(idx).y + ", h: " + merged.get(idx).height +", w: " + merged.get(idx).width + ", A: " + merged.get(idx).area());
//	  	 	 	  System.out.println("*****************************************");

		 		  
			 }
			 
		 	 
//				displayImage(test2,"img_6","filtered ("+merged.size()+")",new Size(test2.size().width,test2.size().height),false);
//			    Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__allBoxes_filtered.jpg",test2);
				  showWaitDestroy("mergedBoxes" , test2);

		 	 
		    
				 List<List<Rect>> temp = new ArrayList<List<Rect>>();

				 
				 int counter = 0;
				 int maxCounter = merged.size();
				
				 while(counter < maxCounter) {
					 Rect rect = merged.get(counter);
					 List<Rect> t = new ArrayList<Rect>();

						 for (Iterator<Rect> rect_2_it = merged.iterator(); rect_2_it.hasNext();) {
							 Rect rect_2 = rect_2_it.next();
							 
//								 if((rect_2.y <= rect.y && (rect_2.y+rect_2.height) >= rect.y)) {
//									 t.add(rect_2);
//									 rect_2_it.remove();
//									 maxCounter = merged.size();
//								 }
							 	int rect_top = rect.y;
							 	int rect_bottom = rect.y + rect.height;			
							 	double middle_rect = rect.y + (rect.height/2);
								 
							 	
							 	if((rect_2.y + rect_2.height) >= middle_rect && rect_2.y <= middle_rect) {
							 		 t.add(rect_2);
									 rect_2_it.remove();
									 maxCounter = merged.size();
							 	}
							 	
//								 if((rect_2.y <= rect.y && (rect_2.y+rect_2.height) >= rect.y)) {
//									 t.add(rect_2);
//									 rect_2_it.remove();
//									 maxCounter = merged.size();
//								 }
						 }
						 counter++;
						 if(t.size() >= 2) {
							 temp.add(t);
							 counter = 0;
						 }else {
				  	 	 	 System.out.println("FALSE: " + t.toString());
						 }
							
						 
				 }
		   
	  	 	 	 System.out.println("merged: " + merged.size());

				 int tempSize = temp.size();
				 
				 List<List<Rect>> temp_2 = new ArrayList<List<Rect>>();

				 
				 for(int idx = 0; idx < tempSize;idx++){
			  	 		 
		  	 		 System.out.println("*****************************************");
		  	 	 	 System.out.println(idx+": " + temp.get(idx).toString());
		  	 	 	 
		  	 	 	Random rand = new Random();
					 int r = rand.nextInt(255);
					 int g = rand.nextInt(255);
					 int b = rand.nextInt(255);
					 Scalar colour = new Scalar(r,g,b);
					 
					 List<Integer> gaps = new ArrayList<Integer>();

					 

					 
					 
//					 Collections.sort(temp.get(idx),new Comparator<Rect>() {
//					    	
//							public int compare(Rect o1, Rect o2) {
	//
//								   int result = Double.compare(o1.x,o2.x);
////							    if ( result == 0 ) {
////							 	   result = Double.compare(o1.y,o2.y);
////							 	   
////							    }
//								   return result;
//							}
//						});
					 
					 
					 for(int idx2 = 0; idx2 < temp.get(idx).size();idx2++){
						 Rect rrr = temp.get(idx).get(idx2);
//						 Imgproc.rectangle(test3, rrr.br(),new Point( rrr.br().x-rrr.width ,rrr.br().y-rrr.height), colour);

						 if(idx2 + 1 < temp.get(idx).size()) {
							 int x_width = rrr.x + rrr.width;
							 Rect rrr_2 = temp.get(idx).get(idx2+1);
							 int x_2 = rrr_2.x;
							 int diff = x_2 - x_width;
							 gaps.add(diff);

						 }
					 }
//					 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "_"+idx+"__allBoxes_filtered_test3.jpg",test3);

					 
		  	 	 	 System.out.println("gaps: " + gaps.toString());

					 
		  	 	 	 
//		  	 	 	int[] gaps_arr = gaps.stream().mapToInt(i->i).toArray();
		  	 	 	//https://www.geeksforgeeks.org/find-a-peak-in-a-given-array/
		  	 	 	//https://www.youtube.com/watch?v=a7D77DdhlFc
//		  	 	 	 System.out.println("peak index: " + findPeak(gaps_arr,gaps_arr.length));

					 if(gaps.size() >= 2) {
					 List<Integer> peaks = peakFinder(gaps);

		  	 	 	 System.out.println("peaks: " + peaks.toString());
		  	 	 	 
//					 List<Rect> new_merged_temp = new ArrayList<Rect>();

					 
					 int maxx = temp.get(idx).size();
					 int counter_ = 0;
					 while (counter_ < maxx) {
//							 Rect rect = merged.get(counter);
							 Rect rrr = temp.get(idx).get(counter_);

							 for (Iterator<Rect> rect_2_it = temp.get(idx).listIterator(counter_); rect_2_it.hasNext();) {
								 Rect rrr_2 = rect_2_it.next();
								 int x_width = rrr.x + rrr.width;
								 if(!rrr.equals(rrr_2)) {
									 int x_2 = rrr_2.x;
									 int diff = x_2 - x_width;
									 System.out.println("diff: " + diff);
									 if(!peaks.contains(diff)) {
										 
										 int x  = rrr.x;//Math.min( rrr.x,   rrr_2.x   );
										 int y  = Math.min( rrr.y,  rrr_2.y  );
										 int height = Math.abs(Math.max((rrr_2.height+rrr_2.y), (rrr.height+rrr.y))- y);
										 int width = rrr.width + diff + rrr_2.width;
												 
												 
//												 Math.abs(Math.max((rrr_2.width + rrr_2.x), (rrr.width+rrr.x))- x);

										 rrr.x  = x;
										 rrr.y  = y;
										 rrr.height = height;
										 rrr.width = width;
										 rect_2_it.remove();
										 maxx = temp.get(idx).size();
										 
	 
									 }else {
										 break;
									 }
								 }
							 }
							 counter_++;
					 }
					 
		  	 	 	 System.out.println(idx+": " + temp.get(idx).toString());

		 
		  	 	 	 
		  	 	 	 
		  	 	 	 

					
				 }
				 
				 
				 
				 
				 
				 
				 }
				 
				 
				 System.out.println("*****************************************");
		  	 	 System.out.println("*****************************************");
				 String ocr_erode_bw = doOcr(bw);
				 System.out.println("ocr_erode_bw: " + ocr_erode_bw);
				 
				 
				 
				 
		  	 	 System.out.println("*****************************************");
		  	 	 System.out.println("*****************************************");
				 String ocr = doOcr(straightImage);
				 System.out.println("ocr: " + ocr);
				 
				 temp_2 = temp;
				 
				 
		  	 	 System.out.println("*****************************************");
		  	 	 System.out.println("*****************************************"); 
				 for(int idx = 0; idx < temp_2.size();idx++){
					 Random rand = new Random();
					 int r = rand.nextInt(255);
					 int g = rand.nextInt(255);
					 int b = rand.nextInt(255);
					 Scalar colour = new Scalar(r,g,b);
//			  	 	 System.out.println("*****************************************");
	//
			  	 	 System.out.println("");
//			  	 	 System.out.println(idx + ". ");

					 
			  	 	 String oneLine = "";
					 for(int idx2 = 0; idx2 < temp_2.get(idx).size();idx2++){
						 
				  	 	 System.out.print(" ");

						 Rect rrr = temp_2.get(idx).get(idx2);
						 Imgproc.rectangle(test3, rrr.br(),new Point( rrr.br().x-rrr.width ,rrr.br().y-rrr.height), colour);

						 int height_10 = (int) Math.round(rrr.height*0.2);
						 int width_10 = (int) Math.round(rrr.width*0.2);
						 rrr.height = rrr.height + height_10;
						 rrr.width = rrr.width + width_10;
						 rrr.x = Math.abs(rrr.x - (width_10/2));
						 rrr.y =  Math.abs(rrr.y - (height_10/2));
						 
						 if((rrr.x + rrr.width) > OCR_IMG.width()) {
//							 System.out.println(rrr + " - W: " + OCR_IMG.width());	 
							 rrr.width = (OCR_IMG.width() - rrr.x);
						 }
						 
						 if((rrr.y + rrr.height) > OCR_IMG.height()) {
//							 System.out.println(rrr + " - H: " + OCR_IMG.height());	 
							 rrr.height = (OCR_IMG.height() - rrr.y);
						 }

//						 Imgproc.rectangle(test3, rrr.br(),new Point( rrr.br().x-rrr.width ,rrr.br().y-rrr.height), colour);
//						 Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "_"+idx+"_"+idx2+"__allBoxes_filtered_test3.jpg",test3);
//						 
						 
	//
								Mat img = new Mat(OCR_IMG,rrr);
								String res = doOcr(img);
//					 			System.out.print(res);
//						 		oneLine
					 			oneLine += res + " ";

					 }
			 			MatchKeyValue(oneLine);

			 		  
				 }	 
			    
//				    Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__allBoxes_filtered_test3.jpg",test3);
					  showWaitDestroy("__allBoxes_filtered_test3" , test3);

			    
		  	 	 	  System.out.println("*****************************************");
		  	 	 	  System.out.println("temp: " + temp.size());
		  	 	 	  System.out.println("temp_2: " + temp_2.size());

		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  

			    
			    
			    
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		  	 	 	  
		    
//		 List<List<Rect>> arr_elems = new ArrayList< List<Rect>>();
	//
//		 List<Rect> top_first = new ArrayList<Rect>();
	//
//		 
//		 List<Rect> top_max = new ArrayList<Rect>();
	//
//			 int countArr = 0;		 
//	 	 for(int r = 0;r<elems.size();r++) {
//	 		 Rect rect = elems.get(r);
//	 		 if(r+1 <= elems.size()-1) {
//		 		 Rect rect2 = elems.get(r+1);
//		 		 if((rect2.x-rect.x)<=100) {
//		 			 if(!arr_elems.isEmpty() && arr_elems.size()-1==countArr) {
//		 				
//		 				addToMainList( arr_elems, top_max, countArr, rect);
	//
//		 				
//		 			 }else {
//		 				 List<Rect> col = new ArrayList<Rect>();
//		 				 col.add(rect);
//		 				arr_elems.add(col);
//		 			 }
//		 		 }else {
//		 			addToMainList( arr_elems, top_max, countArr, rect);
//		 			 countArr++;
//		 		 }
	//
//	 		 }else {
//	 			addToMainList( arr_elems, top_max, countArr, rect);
	//
//	 		 }
//	 		 
//	 		 
//	 		 
//	 		 System.out.println(rect);
//	 		 
	//
//		 		Imgproc.rectangle(rgb2, rect.br(),new Point( rect.br().x-rect.width ,rect.br().y-rect.height), new Scalar(0,0,255));
	//
	// 	
//	 	 }
	// 	
//	 	 
//	 		List<Text_Rect> text_rect_list = new ArrayList<Text_Rect>();
//	 		
	//
	// 	
	// 	
	// 	
	// 	
//				int idxList =0;
	//
//	 	for(int l = 0;l<arr_elems.size();l++) {
//	 		List<Rect> list = arr_elems.get(l);
//	 		 System.out.println("<_____________ List " +l+ "_____________>");
//	 			Rect max_elem = top_max.get(l);
//	 			
//	 			//sort by y
//	 			 Collections.sort(list,new Comparator<Rect>() {
//	 		    	
//	 				public int compare(Rect o1, Rect o2) {
	// 	
//	 					   int result = Double.compare(o1.y,o2.y);
//	 				    if ( result == 0 ) {
//	 				 	   result = Double.compare(o1.x,o2.x);
//	 				 	   
//	 				    }
//	 					   return result;
//	 				}
//	 			});
//	 		 for(Rect re:list) {
//	 			 //extend each elem to max size 
//				 if(!max_elem.equals(re)) {
//					  re.width = max_elem.width+20;
//					  re.x = max_elem.x-20;
	//
//				 }
	//
//				 
//				 
//				 Imgproc.rectangle(rgb2, re.br(),new Point( re.br().x-re.width ,re.br().y-re.height), new Scalar(0,255,0));
//	 			
//				 if(l==0) {
//					 Imgproc.rectangle(rgb2, new Point(re.x,re.y) , new Point( (rgb2.size().width)-re.x-10 ,re.y+re.height),  new Scalar(255, 0, 0));
//				 }
//				  
	//
//			 }
	//
//		 }
	// 	
	//
//	 	 
////			displayImage(test,"img_5","all ("+contours.size()+")",new Size(test.size().width,test.size().height),false);
////		    Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__allBoxes.jpg",test);
	//
//			displayImage(rgb2,"img_6","filtered ("+elems.size()+")",new Size(rgb2.size().width,rgb2.size().height),false);
	//
	//	
////	    Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__allBoxes.jpg",test);
//		Imgcodecs.imwrite(fileToSaveImgs + fileNameWithOutExt + "__filteredBoxes.jpg",rgb2);
		
		
		System.out.println("************* Elapsed time: " + ((double)(System.nanoTime() - startTime)/1000000000.0) + " seconds ************* ");

	}
	    
	    
	    
}
