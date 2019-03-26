package com.medex.mdap.sm.exportpk.business;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.medex.mdap.base.msg.ObjectRestResponse;
import com.medex.mdap.util.PrimaryKeyUtil;

@Service
public class ExportPkBusiness {
	@Autowired
	private PrimaryKeyUtil pkutil;
	//导出主键pk
	public ObjectRestResponse getpk (String json,HttpServletResponse response)   throws Exception{
		String pk_group="";
		int count=0;
		if (json==null||json.equals("")) {
			throw new Exception("请输入导出条数和机构编码");
		}
		JsonObject jsonObject =  new JsonParser().parse(json).getAsJsonObject();
		if (jsonObject.get("pk_group")!=null&&jsonObject.get("pk_group").getAsString().length()==4) {
			 pk_group= jsonObject.get("pk_group").getAsString();//机构编码
		}else {
			throw new Exception("请输入机构编码或机构编码格式不正确");
		}
		if (jsonObject.get("count")!=null) {
			 count= jsonObject.get("count").getAsInt();//导出条数
		}else {
			throw new Exception("请输入导出条数");
		}
		// 第一步，创建一个webbook，对应一个Excel文件  
		HSSFWorkbook wb = new HSSFWorkbook();  
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
		HSSFSheet sheet = wb.createSheet("主键");  
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
		HSSFRow row = sheet.createRow((int) 0);  
		// 第四步，创建单元格，并设置值表头 设置表头居中  
		HSSFCellStyle style = wb.createCellStyle();  
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式  
		HSSFFont font = wb.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		HSSFCell cell = row.createCell((short) 0);  
		cell.setCellValue("主键");  
		cell.setCellStyle(style);  
		String[] pk = pkutil.getpks(pk_group,count);
		for (int i = 0; i < pk.length; i++) {
			row = sheet.createRow(i+1); 
			row.createCell((short) 0).setCellValue(pk[i].toString());  
		}
		// 第六步，将文件存到指定位置  
		try  
		{  
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition", "attachment;filename=" + "主键");
			response.flushBuffer();
			wb.write(response.getOutputStream());
		}  
		catch (Exception e)  
		{  
			e.printStackTrace();  
		}  
		return null;
	}
}
