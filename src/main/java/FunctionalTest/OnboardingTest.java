// package FunctionalTest;
//
// import static org.junit.Assert.*;
// import static com.jayway.restassured.RestAssured.*;
// import static com.jayway.restassured.matcher.RestAssuredMatchers.*;
// import static org.hamcrest.Matchers.*;
//
//
// import org.junit.Before;
// import org.junit.Test;
//
// public class OnboardingTest {
//
// @Before
// public void setUp() throws Exception {
// }
//
// @Test
// public void testOnboardingNewUser() {
// fail("Not yet implemented");
// }
//
// @Test
// public void testAutenticateUser() {
// fail("Not yet implemented");
// }
//
// @Test
// public void testGetUserCredential() {
//
// given().contentType("application/json").
// param("username", "edson").
// param("password", "just_a_test").
// param("email", "edson.philippe@jilbrius.com").
// when().
// post("http://localhost:8080/AssembleeChretienneAPI/webapi/onboarding").
// then().
// body(containsString("OK"));
// }
//
//
//
// }
