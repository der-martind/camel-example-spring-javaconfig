/*
 * Copyright (c): cbb 2016
 */
package org.apache.camel.example.spring.javaconfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.spi.BridgePropertyPlaceholderConfigurer;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author cbb
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {MyRouteConfig.class},
        // Since Camel 2.11.0
        loader = CamelSpringDelegatingTestContextLoader.class
    )
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@PropertySource("classpath:tproc.properties")
public class EnteringRouteTest extends CamelSpringTestSupport{


    @Value("${file.base}")
    private String file_base;
    
    @EndpointInject(uri = "mock:in")
    protected MockEndpoint in;

    @EndpointInject(uri = "mock:out")
    protected MockEndpoint out_lines;
    
    @EndpointInject(uri = "mock:done")
    protected MockEndpoint done;
    
    protected AbstractApplicationContext createApplicationContext() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("META-INF/spring/camel-context.xml");
        BridgePropertyPlaceholderConfigurer props = ctx.getBean(BridgePropertyPlaceholderConfigurer.class);
        return ctx;
    }
    

    @Test
    public void testWithNoLine() throws Exception{
        in.expectedMessageCount(0);
        out_lines.expectedMessageCount(0);
        done.expectedMessageCount(1);
        
        FileUtils.cleanDirectory(new File(file_base));
        
        FileUtils.touch(new File(file_base+"/in/my.csv"));
        
        Thread.sleep(7000);
        
        addMocks();
        
        in.assertIsSatisfied();
        out_lines.assertIsSatisfied();
        done.assertIsSatisfied();

    }

    @Test
    public void testWithOneLine() throws Exception{
        testWithNLines(1);
    }
    
    @Test
    public void testWithMultiLines() throws Exception{
        testWithNLines(10);
    }
        
     private void testWithNLines(int count) throws Exception{   
        in.expectedMessageCount(0);
        out_lines.expectedMessageCount(count);
        done.expectedMessageCount(count);
        
        FileUtils.cleanDirectory(new File(file_base));
        
        String line = FileUtils.readFileToString(new File("src/test/resources/line.valid.txt"));
        List<String> lines = new ArrayList<>();
        for (int i=0;i<count;i++) {
            lines.add(line);
        }
         System.out.println("lines: "+lines.size());
         
        FileUtils.writeLines(new File(file_base+"/in/my.csv"), lines);
        
        Thread.sleep(7000);
        
//        addMocks();
//        
//        in.assertIsSatisfied();
//        out_lines.assertIsSatisfied();
//        done.assertIsSatisfied();
    }


    private void addMocks() throws Exception {
                context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("file:/"+file_base+"/in?delete=true")
                        .to("mock:in");
                
                from("file:/"+file_base+"/out.lines?delete=true")
                        .to("mock:out");
                
                from("file:/"+file_base+"/done?delete=true")
                        .to("mock:done");
                
            }
        });

    }
}
