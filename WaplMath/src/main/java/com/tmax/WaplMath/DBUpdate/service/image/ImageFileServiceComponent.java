package com.tmax.WaplMath.DBUpdate.service.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;
import com.tmax.WaplMath.Recommend.model.problem.ProblemImage;

import lombok.RequiredArgsConstructor;



@Service
@Transactional
@RequiredArgsConstructor
public class ImageFileServiceComponent {
	

private final ProblemImageService imageService;
	
private String dirPath = File.separator + "data" + File.separator + "imgsrc";

	/**
	 * 이미지 파일 JsonObject to String 반환  JsonObject:( key:이미지 파일 이름/value:Base64인코딩 된 이미지 {key1:value1,key2:value2})
	 */
	public String getImgByProbIDServiceComponent(Long probId){
		JsonObject jsonObject = new JsonObject();
		
		List<ProblemImage> imgList = imageService.findByProbId(probId);
		// Entity Domain 차이로 인한 change 21.06.08
		List<String> srcList = imgList.stream().map(m->m.getSrc()).collect(Collectors.toList());
		for( String src : srcList ){
			String imgFileBase64ToString = getImgFileServiceComponent(probId, src);
			jsonObject.addProperty(src, imgFileBase64ToString);
		}
		
		return jsonObject.toString();
	}
	/**
	 * 이미지 파일 base64 인코딩 후 String 으로 반환
	 */
	private String getImgFileServiceComponent(Long probId, String src){
		final Integer BUFFER_SIZE = 3 * 1024;
		FileInputStream fis = null;
		StringBuffer sb = null;
		
		try {
			fis = new FileInputStream(dirPath + File.separator + Long.toString(probId) + File.separator + src);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
		
		byte[] buf = new byte[BUFFER_SIZE];
		int len = 0;
		sb = new StringBuffer();
		
		try {
			while( (len = fis.read(buf))!=-1 ){
				if( len == BUFFER_SIZE ){
					sb.append(Base64.getEncoder().encodeToString(buf));
				}
				else{
					byte[] temp = new byte[len];
					System.arraycopy(buf, 0, temp, 0, len);
					sb.append(Base64.getEncoder().encodeToString(temp));
				}
			}
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();		
			return null;
		}
		
		return sb.toString();
	}
	
	
	
}
