package com.jinyi.odatademo.controller;

import com.jinyi.odatademo.odata.DynamicEdmProvider;
import com.jinyi.odatademo.odata.DynamicEntityProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/odata")
public class ODataController {

    @Autowired
    private DynamicEdmProvider edmProvider;

    @Autowired
    private DynamicEntityProcessor entityProcessor;

    @RequestMapping("/**")
    public void odata(HttpServletRequest request, HttpServletResponse response) {
        try {
            OData odata = OData.newInstance();
            ServiceMetadata edm = odata.createServiceMetadata(edmProvider, null);
            
            ODataHttpHandler handler = odata.createHandler(edm);
            handler.register(entityProcessor);
            
            handler.process(request, response);
        } catch (Exception e) {
            response.setStatus(500);
            try {
                response.getWriter().write("OData service error: " + e.getMessage());
            } catch (Exception ex) {
                // ignore
            }
        }
    }
}