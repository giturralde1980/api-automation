package Salesforce;
import Dtos.Account;
import Dtos.Contact;
import Dtos.Lead;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher.*;
import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class Salesforce {
    private String sfToken;
    private String accountId;
    private String instanceUrl;
    RequestSpecification requestSpecification;

    @DataProvider(name = "contact")
    public Object[][] dataProviderContact(){
        return new Object[][]{
                {new Contact("lastname")}
        };
    }


    @BeforeTest
    private void setUp()
    {
        RestAssured.baseURI ="https://login.salesforce.com/services/oauth2";
        requestSpecification = RestAssured.given()
                .header("Content-type","application/json")
                .header("Authorization" , "Bearer "+ sfToken);
    }

    @Test(priority = 0)
    private void getSFToken()
    {

        String usr="german.iturraldedev@gmail.com";
        Response resp =
        given()
                .header("Content-type","application/json")
                .queryParam("grant_type","password")
                .queryParam("client_id","3MVG9t0sl2P.pByrVQkOEHP0hF5RiqgvSnrFEuG_DkQkiJqulLcwpy_Ni_aAAjdKQoCvZdH1AEZWqdpsvRp2D")
                .queryParam("client_secret","1FAE247911F05BD016CD3C80B196A6F34329B7FF160B58D3793FC72682AA0B95")
                .queryParam("username",usr)
                .queryParam("password","Montevideo1980iwG79BhMsrDb1GRA1sNemWXgl")
                .when()
                .post("/token")
                .then()
                .log().all()
                .assertThat()
                .body("id", notNullValue())
                .body("access_token", isA(String.class))
                .extract()
                .response();
         sfToken = resp.jsonPath().getString("access_token");
         instanceUrl=resp.jsonPath().getString("instance_url");
    }

    @Test(dependsOnMethods = "getSFToken", priority = 1)
    private void TestInsertContact()
    {
        Contact json =new Contact("iturralde coppola");
        RestAssured.baseURI="https://germandev-dev-ed.my.salesforce.com";
                given().spec(requestSpecification)
                        .when()
                        .body(json)
                        .post("/services/data/v53.0/sobjects/Contact/")
                        .then()
                        .log().all()
                        .assertThat()
                        .statusCode(201);
    }



    @Test(dependsOnMethods = "getSFToken", priority = 2)
    private void TestInsertLead()
    {
        RestAssured.baseURI="https://germandev-dev-ed.my.salesforce.com";
        Lead lead = new Lead("coppola", "mellizos ltda");
        Response resp =
                given()
                        .header("Content-type","application/json")
                        .header("Authorization" , "Bearer "+ sfToken)
                        .when()
                        .body(lead)
                        .post("/services/data/v53.0/sobjects/Lead/")
                        .then()
                        .log().all()
                        .assertThat()
                        .statusCode(201)
                        .extract()
                        .response();
        this.accountId=resp.jsonPath().getString("id");
    }





    @Test(dependsOnMethods ={"getSFToken"}, priority = 3)
    private void TestUpdateLead()
    {
        RestAssured.baseURI="https://germandev-dev-ed.my.salesforce.com";
        //Dtos.Contact contact=new Dtos.Contact("test");
        String leadId="00Q7Q000000spJMUAY";
        String json=" {\n    \"city\": \"montevideo\",\n\"email\":  \"email@email.com\"\n                    }";
        Response resp =
                given()
                        .log().all()
                        .header("Content-type","application/json")
                        .header("Authorization" , "Bearer "+ sfToken)
                        .when()
                        .body(json)
                        .patch("/services/data/v53.0/sobjects/Lead/"+leadId)
                        .then()
                        .log().all()
                        .assertThat()
                        .statusCode(204)
                        .extract()
                        .response();
    }

    @Test(dependsOnMethods ={"getSFToken"}, priority = 3)
    private void TestConvertLead()
    {
        RestAssured.baseURI="https://germandev-dev-ed.my.salesforce.com";
        //Dtos.Contact contact=new Dtos.Contact("test");
        String leadId="00Q7Q000000spJMUAY";
        //raging deberia ser Hot
        String json=" {\n\"accountId\":null,\n\"accountRecord\":null,\n\"bypassAccountDedupeCheck\":null,\n\"bypassContactDedupeCheck\":null,\n\"contactId\":null,\n\"contactRecord\":null,\n\"convertedStatus\":null,\n\"doNotCreateOpportunity\":false,\n\"leadId\":null,\n\"opportunityId\":null,\n\"opportunityName\":null,\n\"opportunityRecord\":null,\n\"overwriteLeadSource\":false,\n\"ownerId\":null,\n\"relatedPersonAccountId\":null,\n\"relatedPersonAccountRecord\":null,\n\"sendNotificationEmail\":false\n}";
        Response resp =
                given()
                        .log().all()
                        .header("Content-type","application/json")
                        .header("Authorization" , "Bearer "+ sfToken)
                        .when()
                        .body(json)
                        .patch("")
                        .then()
                        .log().all()
                        .assertThat()
                        .statusCode(200)
                        .extract()
                        .response();
    }


    @Test(dependsOnMethods ={"getSFToken","TestInsertAccount"}, priority = 3)
    private void TestUpdateAccount()
    {
        RestAssured.baseURI="https://germandev-dev-ed.my.salesforce.com";
        //Dtos.Contact contact=new Dtos.Contact("test");
        Account a = new Account();
        a.setName("andres");
        a.setDescription("test");
        Response resp =
                given()
                        .log().all()
                        .header("Content-type","application/json")
                        .header("Authorization" , "Bearer "+ sfToken)
                        .when()
                        .body(a)
                        .patch("/services/data/v53.0/sobjects/Account/"+accountId)
                        .then()
                        .log().all()
                        .assertThat()
                        .statusCode(204)
                        .extract()
                        .response();
    }

    @Test(dependsOnMethods ={"getSFToken","TestInsertAccount"}, priority = 4)
    private void TestDeleteAccount()
    {
        RestAssured.baseURI="https://germandev-dev-ed.my.salesforce.com";
        Response resp =
                given()
                        .log().all()
                        .header("Content-type","application/json")
                        .header("Authorization" , "Bearer "+ sfToken)
                        .when()
                        .delete("/services/data/v53.0/sobjects/Account/"+accountId)
                        .then()
                        .log().all()
                        .assertThat()
                        .body(is(emptyOrNullString()))
                        .statusCode(204)
                        .extract()
                        .response();
        Assert.assertNotNull(resp);
    }
}
