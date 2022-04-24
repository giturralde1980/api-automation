package Salesforce_SelenimAcademy_Credentials;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class Lead {
    protected static String ACCESS_TOKEN = "";
    protected static String INSTANCE_URL = "";

    @BeforeTest
    public void getCredentials(){
        RestAssured.baseURI = "https://login.salesforce.com/services/oauth2/";

        String respuesta = given()
                .queryParam("grant_type", "password")
                .queryParam("client_id", "3MVG9cHH2bfKACZYj3S3as4gJ9.9.zVTytlu8Q61HwPUN.NtPEwJqbFOp4pEy5gm6fsezrxm_WMW9YkQAcPW3")
                .queryParam("client_secret", "696513F287215272F6EC9E2398C94873CF596E8CF92CC9F9F096ED4FE1C8A37F")
                .queryParam("username", "seleniumcurso@gmail.com")
                .queryParam("password", "holahola123PkC9nQP5ZkNgQahPfnQgWWHc")
                .when()
                .post("token")
                .then().assertThat().statusCode(200)
                .extract().asString();

        System.out.println(respuesta);

        JsonPath js = new JsonPath(respuesta);

        ACCESS_TOKEN = js.get("access_token");
        INSTANCE_URL = js.get("instance_url");

        RestAssured.baseURI = INSTANCE_URL;
        System.out.println("Access token: --> " + ACCESS_TOKEN);
        System.out.println("Instance Url: --> " + INSTANCE_URL);
    }

    @Test
    public void newleadConvertionTest(){

        //String leadName = "Emiliano G. C.";
        //String leadCompany = "Manchester Bakery";
       // Lead newLead = new Lead(leadName, leadCompany);
        Dtos.Lead newLead = new Dtos.Lead("iturralde coppola", "iturralde SA");
//create the lead
        Response response =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .body(newLead)
                        .when()
                        .post("/services/data/v51.0/sobjects/Lead")
                        .then()
                        .assertThat()
                        .statusCode(201)
                        .log().all()
                        .extract().response();

        JsonPath jsonPath = response.jsonPath();

        String leadId = jsonPath.get("id");
        boolean status = jsonPath.get("success");

        Assert.assertTrue(leadId.startsWith("00Q"), "El id deberia comenzar con 00Q");
        Assert.assertTrue(status, "Error: el status deberia ser true");


        //update the lead...
        Response updatedLeadResponse =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .body("{\"Title\":\"QA Engineer\"}")
                        .when()
                        .patch("/services/data/v51.0/sobjects/Lead/" + leadId)
                        .then()
                        .assertThat()
                        .statusCode(204)
                        .log().all()
                        .extract().response();


//convert the lead ( Rating = Hot )
        Response convertedLeadResponse =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .body("{\"Rating\":\"Hot\"}")
                        .when()
                        .patch("/services/data/v51.0/sobjects/Lead/" + leadId)
                        .then()
                        .assertThat()
                        .statusCode(204)
                        .log().all()
                        .extract().response();

        Assert.assertEquals(convertedLeadResponse.getStatusCode(), 204, "Error: el status deberia ser 204");

        //get the converted lead...
        Response getConvertedLeadResponse =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .when()
                        .get("/services/data/v51.0/sobjects/Lead/" + leadId)
                        .then()
                        .assertThat()
                        .statusCode(200)
                        .log().all()
                        .extract().response();

        JsonPath leadInfoJsonPath = getConvertedLeadResponse.jsonPath();
        String convertedLeadId = leadInfoJsonPath.get("Id");
        String convertedLeadName = leadInfoJsonPath.get("LastName");
        String convertedAccountId = leadInfoJsonPath.get("ConvertedAccountId");
        String convertedContactId = leadInfoJsonPath.get("ConvertedContactId");

        Assert.assertEquals(convertedLeadId, leadId, "Error: id del lead equivocada");

        Assert.assertEquals(convertedLeadId, leadId, "Error: id del lead equivocada");
       // Assert.assertEquals(convertedLeadName, newLead.getCompany(), "Error: id del lead equivocada");
        Assert.assertTrue(convertedAccountId.startsWith("001"), "Error: id de account equivocada");
        Assert.assertTrue(convertedContactId.startsWith("003"), "Error: id de contact equivocada");

        System.out.println("********* CONVERTED ACCOUNT *************");

//get the new account related to the lead...
        Response getConvertedAccountResponse =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .when()
                        .get("/services/data/v51.0/sobjects/Account/" + convertedAccountId)
                        .then()
                        .assertThat()
                        .statusCode(200)
                        .log().all()
                        .extract().response();


        JsonPath accountJsonPath = getConvertedAccountResponse.jsonPath();

        String accountName = accountJsonPath.get("Name");
        Assert.assertEquals(accountName, newLead.getCompany(), "Error: el account name deberia coincidir con el lead company");

        String accountRating = accountJsonPath.get("Rating");
        Assert.assertEquals(accountRating, "Hot", "Error: el account rating deberia ser Hot");

        System.out.println("********* CONVERTED CONTACT *************");

//get the new account related to the lead...
        Response getConvertedContactResponse =
                given()
                        .header("Content-type", "application/json")
                        .header("Authorization", "Bearer " + ACCESS_TOKEN)
                        .when()
                        .get("/services/data/v51.0/sobjects/Contact/" + convertedContactId)
                        .then()
                        .assertThat()
                        .statusCode(200)
                        .log().all()
                        .extract().response();


        JsonPath contactJsonPath = getConvertedContactResponse.jsonPath();

        String contactName = contactJsonPath.get("Name");
        Assert.assertEquals(contactName, newLead.getLastname(), "Error: el contact name deberia coincidir con el lead name");

        String accountTitle = contactJsonPath.get("Title");
        Assert.assertEquals(accountTitle, "QA Engineer", "Error: el contact title deberia ser QA Manager");


/*
String newLeadResp =
given()
.header("Content-type", "application/json")
.header("Authorization", "Bearer " + ACCESS_TOKEN)
.body("{\"LastName\":\"My Selenium Lead\", \"Status\": \"Open\", \"Company\": \"This is a company\"}")
.when()
.post("/services/data/v51.0/sobjects/Lead")
.then().assertThat().statusCode(201)
.extract().asString();

System.out.println("Respuesta: " + newLeadResp);

JsonPath leadInfo = new JsonPath(newLeadResp);
String leadId = leadInfo.getString("id");
String status = leadInfo.getString("success");
*/
    }


}
