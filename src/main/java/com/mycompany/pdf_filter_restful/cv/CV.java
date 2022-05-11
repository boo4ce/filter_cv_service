/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdf_filter_restful.cv;

import com.google.gson.Gson;
import com.mycompany.pdf_filter_restful.MultipartMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.ParseException;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
@WebServlet(name = "CV", urlPatterns = {"/CV/*"})
@MultipartConfig
public class CV extends HttpServlet{
    private static HashMap<Integer, CVPack> storage;
    private static HashMap<Integer, String> db;
    
    @Override
    public void init() throws ServletException {
        if(storage == null) {
            storage = new HashMap<>();
        }
        
        if(db == null) {
            db = new HashMap<>();
        }
        
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MultipartMap map;
        try {
            map = new MultipartMap(req, this);
            JSONObject json = new JSONObject(map.getParameter("fileName"));
            List<Object> fileNames = json.getJSONArray("fileNames").toList();

            String[] params = getParams(req);
            
            Integer id = Integer.parseInt(params[1]);
            CVPack pack = storage.get(id);
            if(pack == null) {
                pack = new CVPack();
                storage.put(id, pack);
            }
            
            switch(params[0]) {
                case "suitable":
                    for(Object fileName : fileNames) {
                        pack.suitableCV.add((String)fileName);
                    }
                    break;
                case "unsuitable":
                    for(Object fileName : fileNames) {
                        pack.unsuitableCV.add((String)fileName);
                    }
                case "gettextscan":
                    
                    break;
            }
            
            sendMessageToClient("Add success", resp);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            String[] params = getParams(req);
            Integer id = Integer.parseInt(params[1]);
            
            String filePath = db.get(id);
            if(filePath == null) {
                resp.setStatus(400);
                return;
            }

            File file = new File(filePath);
            if(!file.exists()) {
                resp.setStatus(500);
                return;
            }
            
            resp.setContentType("application/json");

            switch(params[0]) {
                case "gettextscan":
                    JSONObject respJson = new JSONObject();
                    respJson.put("content", convertPDFtoText(file));
                    respJson.put("status", "convert success");

                    sendMessageToClient(respJson.toString(), resp);
                    break;
                case "downloadfile":
                    resp.setHeader("Content-disposition", "attachment; filename=" + file.getName());
                    
                    try(InputStream in = new FileInputStream(file);
                            OutputStream out = resp.getOutputStream()) {
                        
                        byte[] buffer = new byte[1024];
                        
                        int numBytesRead;
                        while ((numBytesRead = in.read(buffer)) > 0) {
                            out.write(buffer, 0, numBytesRead);
                        }
                    }
                    break;
                case "metadata":
                    String[] words = convertPDFtoText(file).split("[ \n]");
                    Metadata metadata = new Metadata();
                    for(String s : words) {
                        if(s.matches(".+@gmail.com")) {
                            metadata.email = s;
                            break;
                        }
                    }
                    
                    sendMessageToClient(new Gson().toJson(metadata), resp);
                default:
                    resp.setStatus(415);
                    break;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            resp.setStatus(400);
        } catch (IOException ex) {
            resp.setStatus(500);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            String[] params = getParams(req);
            switch(params[0]) {
                case "uploadfile":
                    MultipartMap map = new MultipartMap(req, this);
                    File file = map.getFile("file_upload");

                    Integer id = db.size();
                    db.put(db.size(), file.getAbsolutePath());

                    resp.setStatus(200);
                    JSONObject respJson = new JSONObject();
                    respJson.put("id", id);
                    respJson.put("status", "upload success");

                    sendMessageToClient(respJson.toString(), resp);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private String convertPDFtoText(final File file) {
        if(file == null || file.length() == 0) return "";
        String parsedText = "";
        
        try(PDDocument document = PDDocument.load(file)) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            pdfTextStripper.setStartPage(1);
            pdfTextStripper.setEndPage(5);
            parsedText = pdfTextStripper.getText(document);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return parsedText;
    }
    
    protected void sendMessageToClient(String message, HttpServletResponse _resp) {
        try ( PrintWriter out = _resp.getWriter()) {
            out.write(message);
            /* TODO output your page here. You may use following sample code. */
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();

        String baseUrl = scheme + "://" + host + ((("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) ? "" : ":" + port) + contextPath;
        return baseUrl;
    }
    
    private String[] getParams(HttpServletRequest request) throws IndexOutOfBoundsException {
        String url = request.getRequestURL().toString();
        String baseUrl = getBaseUrl(request) + request.getServletPath();

        String dicPath = url.substring(baseUrl.length() + 1);
        return dicPath.split("/");
    }
    
    class CVPack {
        private final ArrayList<String> suitableCV;
        private final ArrayList<String> unsuitableCV;
        
        public CVPack() {
            suitableCV = new ArrayList();
            unsuitableCV = new ArrayList();
        }
    }
}
