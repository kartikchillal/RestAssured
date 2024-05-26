package org.example;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class EcommerceAPITest {
    public static void main(String[] args) {
        RequestSpecification req = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com").setContentType(ContentType.JSON).build();
        ResponseSpecification res = new ResponseSpecBuilder().expectStatusCode(200).expectContentType(ContentType.JSON).build();



        //Login API
        LoginRequest loginRequest=new LoginRequest();
        loginRequest.setUserEmail("kartikchillalk@gmail.com");
        loginRequest.setUserPassword("KkCc9@KkCc9@");

        //inorder to bypass HTTPS and SSL certifications use relaxedHTTPSValidation method
        RequestSpecification reqLogin = given().relaxedHTTPSValidation().log().all().spec(req).body(loginRequest);

        LoginResponse loginResponse = reqLogin.when().post("/api/ecom/auth/login")
                .then().log().all().assertThat().spec(res).extract().response().as(LoginResponse.class);

        String token = loginResponse.getToken();
        String userId = loginResponse.getUserId();

        System.out.println(token);
        System.out.println(userId);


        //CreateProduct API
        RequestSpecification addProductBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com").addHeader("Authorization", token).build();
        ResponseSpecification addProductBaseRes= new ResponseSpecBuilder().expectStatusCode(201).expectContentType(ContentType.JSON).build();

        RequestSpecification reqAddProduct = given().relaxedHTTPSValidation().log().all().spec(addProductBaseReq).param("productName", "black-white")
                .param("productAddedBy", userId)
                .param("productCategory", "fashion")
                .param("productSubCategory", "shirts")
                .param("productPrice", 11500)
                .param("productDescription", "Adidas Originals")
                .param("productFor", "women")
                .multiPart("productImage", new File("C://Users//kartik//OneDrive//Pictures//wallpaper.jpg"));

        String addProductresponse = reqAddProduct.when().post("/api/ecom/product/add-product")
                .then().log().all().spec(addProductBaseRes).extract().response().asString();

        JsonPath js=new JsonPath(addProductresponse);
        String productId = js.get("productId");
        String message = js.get("message");
        System.out.println(productId);
        System.out.println(message);


        //Create Order using POGO class
        RequestSpecification createOrderBaseReq = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com").setContentType(ContentType.JSON).addHeader("Authorization", token).build();
        ResponseSpecification createOrderBaseRes = new ResponseSpecBuilder().expectStatusCode(201).expectContentType(ContentType.JSON).build();

        OrderDetail orderDetail=new OrderDetail();
        orderDetail.setCountry("India");
        orderDetail.setProductOrderedId(productId);

        List<OrderDetail> orderDetailList=new ArrayList<>();
        orderDetailList.add(orderDetail);

        Orders orders=new Orders();
        orders.setOrders(orderDetailList);

        RequestSpecification createOrderReq = given().relaxedHTTPSValidation().log().all().spec(createOrderBaseReq).body(orders);

        String responseAddOrder = createOrderReq.when().post("/api/ecom/order/create-order")
                .then().log().all().spec(createOrderBaseRes).extract().response().asString();


        //Delete Product
        RequestSpecification deleteProductBaseReq= new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com").addHeader("Authorization",token).build();
        ResponseSpecification deleteProductBaseRes= new ResponseSpecBuilder().expectStatusCode(200).expectContentType(ContentType.JSON).build();

        RequestSpecification deleteProdReq = given().relaxedHTTPSValidation().log().all().spec(deleteProductBaseReq).pathParam("productId", productId);
        String deleteResponse = deleteProdReq.when().delete("/api/ecom/product/delete-product/{productId}")
                .then().log().all().spec(deleteProductBaseRes).extract().response().asString();
        JsonPath js1=new JsonPath(deleteResponse);
        Assert.assertEquals("Product Deleted Successfully",js1.get("message"));

    }
}
