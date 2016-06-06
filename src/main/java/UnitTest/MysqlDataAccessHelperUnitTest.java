// package UnitTest;
//
// import static org.junit.Assert.*;
// import org.junit.Assert;
//
// import org.junit.Test;
// import DAL.MysqlDataAccessHelper;
//
// public class MysqlDataAccessHelperUnitTest {
//
// @Test
// public void testPreparestatement_Parenthesestwo() {
// MysqlDataAccessHelper myql= MysqlDataAccessHelper.getInstance();
// String s=myql.Preparestatement_Parentheses(2);
// assertEquals("(?,?)",s);
// }
//
// @Test
// public void testPreparestatement_Parenthesesfive() {
// MysqlDataAccessHelper myql= MysqlDataAccessHelper.getInstance();
// String s=myql.Preparestatement_Parentheses(5);
// assertEquals("(?,?,?,?,?)",s);
// }
//
// @Test
// public void testPreparestatement_Parentheseszero() {
// MysqlDataAccessHelper myql= MysqlDataAccessHelper.getInstance();
// String s=myql.Preparestatement_Parentheses(0);
// assertEquals("()",s);
// }
//
// @Test
// public void testPreparestatement_Parenthesesone() {
// MysqlDataAccessHelper myql= MysqlDataAccessHelper.getInstance();
// String s=myql.Preparestatement_Parentheses(1);
// assertEquals("(?)",s);
// }
//
// @Test
// public void testPrepareCallInputString() {
// MysqlDataAccessHelper myql= MysqlDataAccessHelper.getInstance();
// String s=myql.PrepareCallInputString(2, "TestProcedure");
// assertEquals("{call TestProcedure(?,?)}",s);
// }
//
// }
