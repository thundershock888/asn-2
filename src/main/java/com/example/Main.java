/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
@Controller
@SpringBootApplication
public class Main {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
        //JDBC_DATABASE_URL=postgres://lkjisyxotaoyaw:023f2a00cf73c524f8f6a1522fe5fac25b13bf36ab2b1a0e265d32d0faafe3aa@ec2-34-230-115-172.compute-1.amazonaws.com:5432/d176k9gfud4l8f
    }

    @RequestMapping("/")
    String index(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rectangles (id serial, name varchar(20), color varchar(20), area int, len int, wid int)");
            ResultSet rs = stmt.executeQuery("SELECT * FROM rectangles");

            ArrayList<Record> output = new ArrayList<Record>();

            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                String color = rs.getString("color");
                int area = rs.getInt("area");
                int len = rs.getInt("len");
                int wid = rs.getInt("wid");


                Record record = new Record(id, name, color, area, len, wid);
                output.add(record);
            }

            model.put("records", output);
            return "index";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }

    @GetMapping(
            path = "/rectangle"
    )
    public String getRectangleForm(Map<String, Object> model){
        Rectangle rectangle = new Rectangle();  // creates new rect with empty fields
        model.put("rectangle", rectangle);
        return "rectangle";
    }
    @PostMapping(
            path = "/rectangle",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
    public String handleBrowserRectangleSubmit(Map<String, Object> model, Rectangle rectangle) throws Exception {
        // Save the rectangle data into the database
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rectangles (id serial, name varchar(20), color varchar(20), area int, len int, wid int)");
            String sql = "INSERT INTO rectangles (name, color, area, len, wid) VALUES ('" + rectangle.getName() + "','" + rectangle.getColor()+ "','" + rectangle.getArea()+ "','" + rectangle.getLen()+ "','" + rectangle.getWid()+ "')";
            stmt.executeUpdate(sql);
            System.out.println(rectangle.getId() + " " + rectangle.getName()); // print rectangle on console
            return "redirect:/rectangle/success";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }

    }

    @GetMapping("/rectangle/success")
    public String getRectangleSuccess(Map<String, Object> model){
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM rectangles");

            ArrayList<Record> output = new ArrayList<Record>();

            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                String color = rs.getString("color");
                int area = rs.getInt("area");
                int len = rs.getInt("len");
                int wid = rs.getInt("wid");


                Record record = new Record(id,name,color,area,len,wid);
                output.add(record);
            }

            model.put("records", output);
            return "success";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }

    }


    @GetMapping("/rectangle/delete/{pid}")
    public String deleteSpecificRectangle(Map<String, Object> model, @PathVariable String pid) {
       /* System.out.println(pid);

        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeQuery("delete FROM rectangles where id = " + pid);
            System.out.println("deleted rect");
            return "index";
        } catch (Exception e) {
            System.out.println("went into catch");
            model.put("message", e.getMessage());
            return "error";

        }*/
        String sql = "DELETE FROM rectangles WHERE id = ?";

        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1,Integer.parseInt(pid));
            pstmt.executeUpdate();
            return "redirect:/rectangle/success";
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return "error";
        }

    }

    @GetMapping("/rectangle/show/{pid}")
    public String getSpecificRectangle(Map<String, Object> model, @PathVariable String pid){
        System.out.println(pid);

        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM  rectangles where id = " + pid );
            System.out.println(rs);


            while (rs.next()) {


                String name = rs.getString("name");
                System.out.println(name);
                String id = rs.getString("id");
                System.out.println(id);
                String color = rs.getString("color");
                int area = rs.getInt("area");
                int len = rs.getInt("len");
                int wid = rs.getInt("wid");
                model.put("id", id);
                model.put("name", name);
                model.put("color", color);
                model.put("area", area);
                model.put("len", len);
                model.put("wid", wid);

            }


            return "show";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return new HikariDataSource();
        } else {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            return new HikariDataSource(config);
        }
    }

}
/*@Controller
@SpringBootApplication
public class Main {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    @RequestMapping("/")
    String index(Map<String, Object> model) {
        String name = "Bobby";
        model.put("name", name);
        return "index";
    }

    @GetMapping(
            path = "/rectangle"
    )
    public String getRectangleForm(Map<String, Object> model){
        Rectangle rectangle = new Rectangle();  // creates new rectangle object with empty fname and lname
        model.put("rectangle", rectangle);
        return "rectangle";
    }

    @PostMapping(
            path = "/rectangle",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE}
    )
    public String handleBrowserrectangleSubmit(Map<String, Object> model, Rectangle rectangle) throws Exception {
        // Save the rectangle data into the database
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS rectangles (id serial, name varchar(20), color varchar(20), area int, len int, wid int)");
            String sql = "INSERT INTO rectangles (name, color, area, len, wid) VALUES ('" + rectangle.getId() + "','" + rectangle.getName() + "','" + rectangle.getColor()+ "','" + rectangle.getArea()+ "','" + rectangle.getLen()+ "','" + rectangle.getWid()+ "')";
            stmt.executeUpdate(sql);
            System.out.println(rectangle.getId() + " " + rectangle.getName()); // print rectangle on console
            return "redirect:/rectangle/success";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }

    }

    @GetMapping("/rectangle/success")
    public String getrectangleSuccess(Map<String, Object> model){
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM rectangles");

            ArrayList<String> output = new ArrayList<String>();
            while (rs.next()) {
                String name = rs.getString("name");
                String id = rs.getString("id");
                String color = rs.getString("color");
                int area = rs.getInt("area");
                int len = rs.getInt("len");
                int wid = rs.getInt("wid");

                output.add(id + ", " + name + ", " + color + ", " + Integer.toString(area) + ", " + Integer.toString(len) + ", " + Integer.toString(wid));
            }

            model.put("records", output);
            return "success";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }

    }

    // Method 1: uses Path variable to specify rectangle
    @GetMapping("/rectangle/read/{pid}")
    public String getSpecificrectangle(Map<String, Object> model, @PathVariable String pid){
        System.out.println(pid);
        //
        // query DB : SELECT * FROM people WHERE id={pid}
        model.put("id", pid);
        return "readrectangle";
    }

    // Method 2: uses query string to specify rectangle
    @GetMapping("/rectangle/read")
    public String getSpecificrectangle2(Map<String, Object> model, @RequestParam String pid){
        System.out.println(pid);
        //
        // query DB : SELECT * FROM people WHERE id={pid}
        model.put("id", pid);
        return "readrectangle";
    }
    @RequestMapping("/db")
    String db(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
            ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

            ArrayList<String> output = new ArrayList<String>();
            while (rs.next()) {
                output.add("Read from DB: " + rs.getTimestamp("tick"));
            }

            model.put("records", output);
            return "db";
        } catch (Exception e) {
            model.put("message", e.getMessage());
            return "error";
        }
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return new HikariDataSource();
        } else {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            return new HikariDataSource(config);
        }
    }

}*/
