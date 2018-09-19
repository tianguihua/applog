package cn.eeepay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setCharacterEncoding("utf-8");
		String savePath = this.getInitParameter("path");
		String message = null;
		boolean finsh=true;
		try{
			if(ServletFileUpload.isMultipartContent(request)){
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(1024 * 100);
				factory.setRepository((File)request.getServletContext().getAttribute("javax.servlet.context.tempdir"));
				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setFileSizeMax(1024 * 1024);
 				upload.setSizeMax(1024 * 1024 * 10);
				List<FileItem> list = upload.parseRequest(request);
				String source=null;
				String appType=null;
				File file=null;
				for (FileItem item : list) {
					if (item.isFormField()) {
						if("source".equals(item.getFieldName()))
							source=item.getString();
						if("appType".equals(item.getFieldName()))
							appType=item.getString();
					}
				}
				if(source!=null||appType!=null){
					savePath=savePath+source+"/"+appType;
					file=new File(savePath);
					if(!file.exists()){
						message="提交参数错误";
						finsh=false;
					}else{
						for (FileItem item : list) {
							if (!item.isFormField()) {
								String filename = item.getName();
								if (filename == null || filename.trim().equals("")) {
									continue;
								}
								filename = filename.substring(filename.lastIndexOf("\\") + 1);
								writeFile(item.getInputStream(),new File(file,filename));
								item.delete();
							}
						}
						System.out.println("文件上传成功");
						message = "文件上传成功！";
					}
				}
			}else{
				message="提交方式错误";
				finsh=false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		PrintWriter write=response.getWriter();
		write.write("{\"success\":");
		write.write(new Boolean(finsh).toString());
		write.write(",\"message\":\"");
		write.write(message);
		write.write("\"}");
		write.flush();
		write.close();
	}
	
	public void writeFile(InputStream in,File file) throws IOException{
		FileOutputStream out = new FileOutputStream(file);
		byte buffer[] = new byte[1024];
		int len = 0;
		while ((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
	}
}