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

    @Override
    public void init() throws ServletException {
        if(storage == null) {
            storage = new HashMap<>();
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MultipartMap map;
        try {
            map = new MultipartMap(req, this);
            JSONObject json = new JSONObject(map.getParameter("fileName"));
            List<Object> fileNames = json.getJSONArray("fileNames").toList();

            String url = req.getRequestURL().toString();
            String baseUrl = getBaseUrl(req) + req.getServletPath();
            
            String dicPath = url.substring(baseUrl.length() + 1);
            String[] params = dicPath.split("/");
            
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
                    break;
            }
            
            sendMessageToClient("Add success", resp);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String url = req.getRequestURL().toString();
            String baseUrl = getBaseUrl(req) + req.getServletPath();
            
            String dicPath = url.substring(baseUrl.length() + 1);
            String[] params = dicPath.split("/");
            
            CVPack pack = storage.get(Integer.parseInt(params[1]));
            
            if(pack == null) {
                sendMessageToClient("Empty list", resp);
                return;
            }
            
            HashMap<String, List<String>> resposeData = new HashMap<>();
            
            switch(params[0]) {
                case "suitable":
                    resposeData.put("fileNames", pack.suitableCV);
                    break;
                case "unsuitable":
                    resposeData.put("fileNames", pack.unsuitableCV);
                    break;
            }
            
            sendMessageToClient(new JSONObject(resposeData).toString(), resp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        MultipartMap map;
        try {
            map = new MultipartMap(req, this);
            File file = map.getFile("attachment");
            
            // Now do your thing with the obtained input.
            sendMessageToClient(convertPDFtoText(file), resp);
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
    
    class CVPack {
        private final ArrayList<String> suitableCV;
        private final ArrayList<String> unsuitableCV;
        
        public CVPack() {
            suitableCV = new ArrayList();
            unsuitableCV = new ArrayList();
        }
    }
}
