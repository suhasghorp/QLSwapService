package com.qlservices;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class SwapResourceTest {
    //"{\"ID\": \"001\", \"swapType\": \"PAYER\", \"nominal\": 48000000.0, \"startDate\": \"2018-03-14\", \"maturityDate\": \"2028-03-14\", \"fixedLegFrequency\": \"ANNUAL\", \"fixedLegCalendar\": \"TARGET\", \"fixedLegConvention\": \"MODIFIEDFOLLOWING\", \"fixedLegDateGenerationRule\": \"BACKWARD\", \"fixedLegRate\": 0.02, \"fixedLegDayCount\": \"ACTUAL360\", \"floatingLegFrequency\": \"QUARTERLY\", \"floatingLegCalendar\": \"TARGET\", \"floatingLegConvention\": \"MODIFIEDFOLLOWING\", \"floatingLegDateGenerationRule\": \"BACKWARD\", \"floatingLegSpread\": 0.0007, \"floatingLegDayCount\": \"ACTUAL360\"}"

    @Test
    public void testSwapEndpoint() {
        given()
                .body("{\"ID\": \"738641\", \"swapType\": \"PAYER\", \"nominal\": 595000.0, \"startDate\": \"03-06-2012\", \"maturityDate\": \"03-06-2042\", " +
                        "\"fixedLegRate\": 0.028037, \"floatingLegSpread\": 0.0, \"convention\" : \"USD\", \"fullResults\" : true}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post("/price/vanillaswap")
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"738641\",\"npv\":-212916.439423,\"fair rate\":0.010195,\"DV01\":-1401.889967,\"Bucketed DV01\":{\"1D\":0.0,\"2D\":0.0,\"1W\":-2.987873,\"3M\":0.0,\"6M\":0.0,\"9M\":0.0,\"12M\":0.0,\"15M\":0.0,\"18M\":0.0,\"21M\":0.0,\"2Y\":0.0,\"3Y\":0.309666,\"4Y\":-0.353209,\"5Y\":-0.048164,\"6Y\":0.044511,\"7Y\":-0.169094,\"8Y\":-0.090342,\"9Y\":-0.07634,\"10Y\":-0.161813,\"12Y\":-0.417094,\"15Y\":-0.780877,\"20Y\":849.936438,\"25Y\":342.925524,\"30Y\":0.0,\"40Y\":0.0,\"50Y\":0.0,\"60Y\":0.0}}"));

        given()
                .body("{\"ID\": \"19199\", \"swapType\": \"RECEIVER\", \"nominal\": 1000000000.0, \"startDate\": \"07-11-2008\", \"maturityDate\": \"07-11-2023\", " +
                        "\"fixedLegRate\": 0.0478, \"floatingLegSpread\": 0.0, \"convention\" : \"USD\", \"fullResults\" : false}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post("/price/vanillaswap")
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"19199\",\"npv\":135926970.909356,\"fair rate\":0.002376}"));
        given()
                .body("{\"ID\": \"74839\", \"swapType\": \"RECEIVER\", \"nominal\": 250000000.0, \"startDate\": \"12-23-2009\", \"maturityDate\": \"12-23-2024\", " +
                        "\"fixedLegRate\": 0.0447, \"floatingLegSpread\": 0.0, \"convention\" : \"USD\", \"fullResults\" : true}")
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .when().post("/price/vanillaswap")
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"74839\",\"npv\":47145254.948482,\"fair rate\":0.002737,\"DV01\":-111964.368582,\"Bucketed DV01\":{\"1D\":0.0,\"2D\":0.0,\"1W\":421.75644,\"3M\":-0.010235,\"6M\":-0.006014,\"9M\":-0.004238,\"12M\":-0.002606,\"15M\":0.0,\"18M\":0.002908,\"21M\":0.006547,\"2Y\":-5.288493,\"3Y\":-76.611253,\"4Y\":-81203.578388,\"5Y\":-24416.512348,\"6Y\":0.0,\"7Y\":0.0,\"8Y\":0.0,\"9Y\":0.0,\"10Y\":0.0,\"12Y\":0.0,\"15Y\":0.0,\"20Y\":0.0,\"25Y\":0.0,\"30Y\":0.0,\"40Y\":0.0,\"50Y\":0.0,\"60Y\":0.0}}"));
    }

}