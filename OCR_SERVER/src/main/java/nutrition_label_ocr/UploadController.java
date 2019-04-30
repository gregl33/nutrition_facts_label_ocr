package nutrition_label_ocr;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nutrition_label_ocr.Ocr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UploadController {


    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @ResponseBody
    @RequestMapping(value = "/uploadToOcr", produces = MediaType.APPLICATION_JSON_VALUE ) 
    public Ocr singleFileUpload(HttpServletRequest request, HttpServletResponse response,@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws IOException {

    	String WebPath = "src/main/resources/img/";
    	String imgname = file.getOriginalFilename();
		String fileNameWithOutExt = FilenameUtils.removeExtension(imgname);
		

		
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH_mm_ss");
		String time = dateFormat.format(now);
		
		String newFilePath = WebPath + fileNameWithOutExt + "__" + time + "/";
		
    	  File directory = new File(newFilePath);
    	    if (!directory.exists()){
    	        directory.mkdir();
    	    }
    	    
    	    
    	    
            byte[] bytes = file.getBytes();
            Path path = Paths.get(newFilePath + imgname);
            Files.write(path, bytes);
        
            Ocr newOcr = new Ocr(imgname,newFilePath);
            

//            
//            File file_ = new File(WebPath + imgname); 
//            
//            if(file_.delete()) 
//            { 
//                System.out.println("File deleted successfully"); 
//            } 
//            else
//            { 
//                System.out.println("Failed to delete the file"); 
//            } 


        return newOcr; //"redirect:/uploadStatus";
    }


}


