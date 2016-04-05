package com.maxleap.las.bass.apiserver.resource;

import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.ResponseSpecification;
import com.maxleap.exception.LASException;
import com.maxleap.pandora.core.utils.LASObjectJsons;
import com.maxleap.platform.LASConstants;
import org.junit.*;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.expect;
import static com.maxleap.las.bass.apiserver.resource.ResourceTestHelper.headers;
import static com.maxleap.las.bass.apiserver.resource.ResourceTestHelper.path;



/**
 * User: qinpeng
 * Date: 14-6-19
 * Time: 11:36
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Theories.class)
public class LASSchemaResourceTest {
  private static Map<String, Object> header = new HashMap<>();

  static ResponseSpecBuilder builder = new ResponseSpecBuilder();
  static ResponseSpecification responseSpec;

  private static String className = "TestSchemaClass";
  private static String classNameWithKey = "TestSchemaClassWithKey";
  private String reservedClassName = "_Installation";

  @BeforeClass
  public static void before() {
    //build headers
    header = headers();
    header.put("Content-Type", MediaType.APPLICATION_JSON);

    //clear schema if exists
    Response deleteOne =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + classNameWithKey));
    deleteOne.then().spec(builder.expectStatusCode(200).build());
    System.out.println("before delete: " + deleteOne.as(Map.class) + "className: " + classNameWithKey);
    
    Response deleteTwo =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + classNameWithKey));
    deleteTwo.then().spec(builder.expectStatusCode(200).build());
    System.out.println("before delete: " + deleteTwo.as(Map.class) + "className: " + className);
  }

  @Test
  public void _10create() {
    Map schema = new HashMap<>();
    schema.put("className", className);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create class schema
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            post(path("/schemas/"));

    Map responseMsg = response.as(Map.class);
    System.out.println("_10create: " + responseMsg);

    Assert.assertNotNull(responseMsg.get(LASConstants.KEY_OBJECT_CREATED_AT));
    Assert.assertNotNull(responseMsg.get(LASConstants.KEY_OBJECT_ID));
    response.then().spec(responseSpec);

  }

  @Test
  public void _10createWithKeys() {
    Map schema = new HashMap<>();
    schema.put("className", classNameWithKey);

    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);

    HashMap<Object, Object> stringKey = new HashMap<>();
    stringKey.put("type", "String");
    keys.put("string", stringKey);

    HashMap<Object, Object> booleanKey = new HashMap<>();
    booleanKey.put("type", "Boolean");
    keys.put("boolean", booleanKey);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            post(path("/schemas/"));

    Map responseMsg = response.as(Map.class);
    System.out.println("_10create: " + responseMsg);

    Assert.assertNotNull(responseMsg.get(LASConstants.KEY_OBJECT_CREATED_AT));
    Assert.assertNotNull(responseMsg.get(LASConstants.KEY_OBJECT_ID));
    response.then().spec(responseSpec);

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            get(path("/schemas/" + classNameWithKey));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    Map keysR = (Map) result.get("keys");

    Assert.assertEquals(2, keysR.size());
    System.out.println(result);
  }

  @Test
  public void _10createWithReservedKeys() {
    Map schema = new HashMap<>();
    TestData testData = new TestData();
    int random = testData.random(2100000001);
    schema.put("className", className + random);

    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);

    HashMap<Object, Object> stringKey = new HashMap<>();
    stringKey.put("type", "String");
    keys.put("createdAt", stringKey);

    HashMap<Object, Object> booleanKey = new HashMap<>();
    booleanKey.put("type", "Boolean");
    keys.put("boolean", booleanKey);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            post(path("/schemas/"));

    Map responseMsg = response.as(Map.class);
    System.out.println("_10create: " + responseMsg);
    response.then().spec(responseSpec);

    Assert.assertNotNull(responseMsg.get("errorCode"));
    Assert.assertNotNull(responseMsg.get("errorMessage"));
    Assert.assertEquals(LASException.INVALID_KEY_NAME, responseMsg.get("errorCode"));
  }

  @Test
  public void _10createReservedClassName() {
    Map schema = new HashMap<>();
    schema.put("className", reservedClassName);


    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            post(path("/schemas/"));

    Map responseMsg = response.as(Map.class);
    System.out.println("_10createReservedClassName: " + responseMsg);

  }

  @Test
  public void _11createExisit() {
    Map schema = new HashMap<>();
    schema.put("className", className);

    /**
     * create exist.
     */
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            post(path("/schemas/"));
    response1.then().spec(builder.expectStatusCode(400).build());

    Map responseMsg1 = response1.as(Map.class);

    Assert.assertNotNull(responseMsg1.get("errorCode"));
    Assert.assertNotNull(responseMsg1.get("errorMessage"));
    Assert.assertTrue(responseMsg1.get("errorMessage").toString(), responseMsg1.get("errorMessage").equals("class is exist. [class name: TestSchemaClass]"));
  }

  @Test
  public void _12createInvalid() {
    Map schema = new HashMap<>();
    schema.put("className", "Class@@");

    Response response2 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            post(path("/schemas/"));
    response2.then().spec(builder.expectStatusCode(400).build());

    Map responseMsg2 = response2.as(Map.class);

    Assert.assertNotNull(responseMsg2.get("errorCode"));
    Assert.assertNotNull(responseMsg2.get("errorMessage"));
    Assert.assertTrue(responseMsg2.get("errorMessage").toString(), responseMsg2.get("errorMessage").equals("ClassName must be start with a letters, and letters, numbers, _, $ are the only valid characters"));
  }

  @Test
  public void _13createInvalidEmptyClassName() {
    Map schema = new HashMap<>();

    Response response2 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            post(path("/schemas/"));
    response2.then().spec(builder.expectStatusCode(400).build());

    Map responseMsg2 = response2.as(Map.class);

    Assert.assertNotNull(responseMsg2.get("errorCode"));
    Assert.assertNotNull(responseMsg2.get("errorMessage"));
    Assert.assertEquals(LASException.INVALID_CLASS_NAME, responseMsg2.get("errorCode"));
    System.out.println(responseMsg2);

  }

  /**
   * valid key.
   */
  @Test
  public void _20addKey() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);

    HashMap<Object, Object> stringKey = new HashMap<>();
    stringKey.put("type", "String");
    keys.put("string", stringKey);

    HashMap<Object, Object> booleanKey = new HashMap<>();
    booleanKey.put("type", "Boolean");
    keys.put("boolean", booleanKey);

    HashMap<Object, Object> objectKey = new HashMap<>();
    objectKey.put("type", "Object");
    keys.put("object", objectKey);

    HashMap<Object, Object> dateKey = new HashMap<>();
    dateKey.put("type", "Date");
    keys.put("date", dateKey);

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + className + "/addKey"));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    System.out.println(result);

  }

  /**
   * valid key.
   */
  @Test
  public void _20addReservedKey() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);

    HashMap<Object, Object> createdAtKey = new HashMap<>();
    createdAtKey.put("type", "Date");
    keys.put("createdAt", createdAtKey);
    HashMap<Object, Object> idKey = new HashMap<>();
    idKey.put("type", "Date");
    keys.put("_id", idKey);

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + className + "/addKey"));
    response1.then().spec(builder.expectStatusCode(400).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    System.out.println("_20addReservedKey: " + result);

  }

  /**
   * valid key.
   */
  @Test
  public void _20addKeyInReservedClass() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);

    HashMap<Object, Object> stringKey = new HashMap<>();
    stringKey.put("type", "String");
    keys.put("string", stringKey);

    HashMap<Object, Object> booleanKey = new HashMap<>();
    booleanKey.put("type", "Boolean");
    keys.put("boolean", booleanKey);

    HashMap<Object, Object> objectKey = new HashMap<>();
    objectKey.put("type", "Object");
    keys.put("object", objectKey);

    HashMap<Object, Object> dateKey = new HashMap<>();
    dateKey.put("type", "Date");
    keys.put("date", dateKey);

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + reservedClassName + "/addKey"));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    System.out.println(result);

  }

  /**
   * invalid key.
   */
  @Test
  public void _21addInvalidKey() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);
    HashMap<Object, Object> key = new HashMap<>();
    keys.put("!cc", key);
    key.put("type", "Boolean");
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + className + "/addKey"));
    response1.then().spec(builder.expectStatusCode(400).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(LASException.INVALID_KEY_NAME, result.get("errorCode"));
    System.out.println(result);
  }

  /**
   * invalid type.
   */
  @Test
  public void _22addInvalidTypeKey() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);
    HashMap<Object, Object> key = new HashMap<>();
    keys.put("cc", key);
    key.put("type", "da");
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + className + "/addKey"));
    response1.then().spec(builder.expectStatusCode(400).build());
    String string = response1.asString();

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.get("errorCode"), LASException.INTERNAL_SERVER_ERROR);
    System.out.println(result);
  }

  /**
   * invalid type.
   */
  @Test
  public void _23addInvalidTypeKeyPointer() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);
    HashMap<Object, Object> key = new HashMap<>();
    keys.put("cc", key);
    key.put("type", "Relation");
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + className + "/addKey"));
    response1.then().spec(builder.expectStatusCode(400).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.get("errorCode"), LASException.INVALID_TYPE);
    System.out.println(result);
  }

  /**
   * invalid key.
   */
  @Test
  public void _30delReservedKey() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);
    keys.put("channels", 1);
    keys.put("deviceToken", 1);
    keys.put("deviceType", 1);
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + reservedClassName + "/delKey"));
    response1.then().spec(builder.expectStatusCode(400).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(LASException.INVALID_KEY_NAME, result.get("errorCode"));

  }

  /**
   * invalid key.
   */
  @Test
  public void _301delKey() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);
    keys.put("object", 1);
    keys.put("boolean", 1);
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + reservedClassName + "/delKey"));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    System.out.println(result);

  }

  /**
   * invalid key.
   */
  @Test
  public void _30delKey() {
    Map schema = new HashMap<>();
    HashMap<Object, Object> keys = new HashMap<>();
    schema.put("keys", keys);
    keys.put("string", 1);
    keys.put("object", 1);
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(schema)).
            put(path("/schemas/" + className + "/delKey"));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map result = response1.as(Map.class);
    Assert.assertNotNull(result);
    System.out.println(result);

  }

  @Ignore
  public void _50findALL() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            get(path("/schemas/"));
    response1.then().spec(builder.expectStatusCode(200).build());

    List schema = response1.as(List.class);
    Assert.assertNotNull(schema);

    System.out.println(schema);

  }

  @Test
  public void _50find() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            get(path("/schemas/" + className));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map schema = response1.as(Map.class);
    Assert.assertNotNull(schema);
    Map keys = (Map) schema.get("keys");

    Assert.assertEquals(2, keys.size());
    System.out.println(schema);

  }

  @Test
  public void _50findReservedClass() {
    /**
     * create exist.
     */
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            get(path("/schemas/" + reservedClassName));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map schema = response1.as(Map.class);
    Assert.assertNotNull(schema);
    Map keys = (Map) schema.get("keys");

    System.out.println(schema);
    Assert.assertTrue(keys.size() >= 0);

  }

  @Test
  public void _60delete() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + className));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map responseMsg1 = response1.as(Map.class);
    Assert.assertNotNull(responseMsg1.get("number"));

    Response response2 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + classNameWithKey));
    response2.then().spec(builder.expectStatusCode(200).build());

    Map responseMsg2 = response2.as(Map.class);
    System.out.println("_60delete : " + responseMsg2.get("number"));
    Assert.assertNotNull(responseMsg2.get("number"));
  }

  @Test
  public void _60deleteReservedClass() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + reservedClassName));
    response1.then().spec(builder.expectStatusCode(400).build());

    Map responseMsg1 = response1.as(Map.class);

    Assert.assertEquals(LASException.UNAUTHORIZED, responseMsg1.get("errorCode"));
    Assert.assertNotNull(responseMsg1);
  }

}
