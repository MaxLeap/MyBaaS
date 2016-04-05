package com.maxleap.las.bass.apiserver.resource;

import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.ResponseSpecification;
import com.maxleap.exception.LASException;
import com.maxleap.las.sdk.MLQuery;
import com.maxleap.las.sdk.MLUpdate;
import com.maxleap.las.sdk.types.MLPointer;
import com.maxleap.pandora.core.utils.DateUtils;
import com.maxleap.pandora.core.utils.LASObjectJsons;
import com.maxleap.platform.LASConstants;
import org.junit.*;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.MediaType;
import java.util.*;

import static com.jayway.restassured.RestAssured.expect;
import static com.maxleap.las.bass.apiserver.resource.ResourceTestHelper.*;

/**
 * User: qinpeng
 * Date: 14-6-19
 * Time: 11:36
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Theories.class)
public class CloudDataResourceTest {
  private static Map<String, Object> header = new HashMap<>();

  static ResponseSpecBuilder builder = new ResponseSpecBuilder();
  static ResponseSpecification responseSpec;
  private static String objectId = "";
  private static String pointerId1 = "";
  private static String pointerId2 = "";
  private static String pointerId3 = "";
  private static String pointerFatherId = "";


  private static String className = "Ghost1";
  private static String pointerFatherClass = "Father";
  private static String pointerClass = "Pointer";

  @BeforeClass
  public static void before() {
    //build headers
    header = headers();
    header.put("Content-Type", MediaType.APPLICATION_JSON);
    responseSpec = builder.expectStatusCode(200).build();

    Map map = new HashMap();
    map.put("className", className);
    
    /**
     * create ClassSchema
     */
    expect().defaultParser(Parser.JSON).
        when().
        with().headers(header).
        with().body(LASObjectJsons.serialize(map)).
        post(path("/schemas/"));
    map.put("className", pointerFatherClass);
    expect().defaultParser(Parser.JSON).
        when().
        with().headers(header).
        with().body(LASObjectJsons.serialize(map)).
        post(path("/schemas/"));
    map.put("className", pointerClass);
    expect().defaultParser(Parser.JSON).
        when().
        with().headers(header).
        with().body(LASObjectJsons.serialize(map)).
        post(path("/schemas/"));

    pointerFatherId = createObject(pointerFatherClass, null, null);
    pointerId1 = createObject(pointerClass, pointerFatherClass, pointerFatherId);
    pointerId2 = createObject(pointerClass, pointerClass, pointerFatherId);
    pointerId3 = createObject(pointerClass, pointerClass, pointerFatherId);
    objectId = createObject(className, pointerClass, pointerId1);


  }

  @AfterClass
  public static void after() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + className));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map responseMsg1 = response1.as(Map.class);
    Assert.assertEquals(1, responseMsg1.get("number"));

    Response response2 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + pointerClass));
    response2.then().spec(builder.expectStatusCode(200).build());

    Map responseMsg2 = response2.as(Map.class);
    Assert.assertEquals(1, responseMsg2.get("number"));

    Response response3 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/schemas/" + pointerFatherClass));
    response3.then().spec(builder.expectStatusCode(200).build());

    Map responseMsg3 = response3.as(Map.class);
    Assert.assertEquals(1, responseMsg3.get("number"));
  }

  private static String createObject(String className, String pointerClass, String pointerId) {
    /**
     * create
     */
    Response pointerResponse1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(createData(pointerClass, pointerId))).
            post(path("/classes/" + className));
    pointerResponse1.then().spec(responseSpec);

    Map pointerResponse1Map = pointerResponse1.as(Map.class);

    Assert.assertNotNull(pointerResponse1Map.get(LASConstants.KEY_OBJECT_CREATED_AT));

    return objectId(pointerResponse1Map);
  }

  private static Map findObject(String className, String id) {
    /**
     * find
     */
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            get(path("/classes/" + className + "/" + id));
    response1.then().spec(builder.expectStatusCode(200).build());

    Map data = response1.as(Map.class);

    return data;
  }

  private static Map createData(String pointerClass, String pointerId) {
    TestData testData = new TestData();
    testData.string("name", "zhaojing").number().number("age", testData.random(100))
        .geoPoint().date().bytes().array();

    if (pointerId != null) {
      testData.pointer(pointerClass, pointerId);
      if (pointerClass.equals(pointerFatherClass)) {
        testData.pointer("pointerParent", pointerClass, pointerId);
      }
    }

    return testData.data();
  }

  @Test
  public void _10checkData() {
    Map object = findObject(className, objectId);
    System.out.println("_10checkData: " + object.size());
    System.out.println("_10checkData: " + object);
    Assert.assertEquals(11, object.size());
    DateUtils.parseDate(object.get(LASConstants.KEY_OBJECT_UPDATED_AT).toString());
    DateUtils.parseDate(object.get(LASConstants.KEY_OBJECT_CREATED_AT).toString());

    Map geoPoint = (Map) object.get("geoPoint");
    Assert.assertEquals(3, geoPoint.size());
    Assert.assertNotNull(geoPoint.get("__type"));
    Assert.assertNotNull(geoPoint.get("longitude"));
    Assert.assertNotNull(geoPoint.get("latitude"));

    Map bytes = (Map) object.get("bytes");
    Assert.assertEquals(2, bytes.size());
    Assert.assertNotNull(bytes.get("__type"));
    Assert.assertNotNull(bytes.get("base64"));

    Map pointer = (Map) object.get("pointer");
    Assert.assertEquals(3, pointer.size());
    Assert.assertNotNull(pointer.get("__type"));
    Assert.assertNotNull(pointer.get("className"));
    Object objectId1 = pointer.get("objectId");
    Assert.assertNotNull(objectId1);
    Assert.assertTrue(!(objectId1 instanceof Map));

    Map date = (Map) object.get("date");
    Assert.assertEquals(2, date.size());
    Assert.assertNotNull(date.get("__type"));
    Assert.assertNotNull(date.get("iso"));
  }

  @Test
  public void _10create() {

    for (int i = 0; i < 10; i++) {

      TestData testData = new TestData();
      testData.initBaseData();
      testData.pointer(pointerClass, pointerId1);
      testData.pointer("pointer2", pointerClass, pointerId1);
      testData.relation(pointerClass, pointerId1);

      responseSpec = builder.expectStatusCode(200).build();

      /**
       * create
       */
      Response response =
          expect().defaultParser(Parser.JSON).
              when().
              with().headers(header).
              with().body(LASObjectJsons.serialize(testData.data())).
              post(path("/classes/" + className));
      response.then().spec(responseSpec);

      Map responseMsg = response.as(Map.class);

      Assert.assertNotNull(responseMsg.get(LASConstants.KEY_OBJECT_CREATED_AT));
      String id = objectId(responseMsg);
      Assert.assertNotNull(id);

      if (i == 0) {
        if (objectId == null) {
          pointerFatherId = id;
        }
      }
    }

  }

  @Test
  public void _11createInvalidType() {

    Map map = new HashMap();

    Map pointer = new HashMap();
    pointer.put("__type", "Pointers");
    pointer.put("className", "TestOne");
    pointer.put("objectId", "537193b930048a95059a9a81");

    map.put("pointer", pointer);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            post(path("/classes/" + className));
    Map error = response.as(Map.class);
    System.out.println("_11createInvalidType: " + error);

    response.then().spec(responseSpec);
    Assert.assertEquals(LASException.INVALID_TYPE, errorCode(error));


  }

  @Test
  public void _11createInvalidPointer() {

    Map map = new HashMap();

    Map pointer = new HashMap();
    pointer.put("__type", "Pointer");
    pointer.put("className", "TestOne");

    map.put("pointer", pointer);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            post(path("/classes/" + className));

    Map error = response.as(Map.class);
    System.out.println("_11createInvalidPointer: " + error);
    response.then().spec(responseSpec);
    Assert.assertEquals(LASException.INVALID_TYPE, errorCode(error));

  }

  @Test
  public void _11createInvalidRelation() {

    Map map = new HashMap();

    Map pointer = new HashMap();
    pointer.put("__type", "Pointer");
    pointer.put("className", "TestOne");
    pointer.put("objectId", "537193b930048a95059a9a81");

    map.put("pointer", pointer);

    Map relation = new HashMap();
    relation.put("__op", "AddRelation");
    List list = new ArrayList<>();
    list.add(pointer);

    relation.put("objects", pointer);

    map.put("relation", relation);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            post(path("/classes/" + className));

    Map error = response.as(Map.class);
    System.out.println("_11createInvalidRelation: " + error);
    response.then().spec(responseSpec);
    Assert.assertEquals(LASException.INVALID_TYPE, errorCode(error));


  }

  @Test
  public void _11createRelationNull() {

    Map map = new HashMap();

    map.put("relation", null);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            post(path("/classes/" + className));
    response.then().spec(responseSpec);

    Map result = response.as(Map.class);

    String id = objectId(result);


  }

  @Test
  public void _11createInvalidGeoPoint() {

    Map map = new HashMap();

    Map geoPoint = new HashMap();
    geoPoint.put("__type", "GeoPoint");
    geoPoint.put("latitude", 18);

    map.put("geoPoint", geoPoint);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            post(path("/classes/" + className));

    Map error = response.as(Map.class);
    System.out.println("_11createInvalidGeoPoint: " + error);
    response.then().spec(responseSpec);
    Assert.assertEquals(LASException.INVALID_TYPE, errorCode(error));

  }

  @Test
  public void _12createNotMatchType() {

    Map map = new HashMap();

    Map geoPoint = new HashMap();
    geoPoint.put("latitude", 18);

    map.put("geoPoint", geoPoint);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            post(path("/classes/" + className));

    Map error = response.as(Map.class);
    System.out.println("_12createNotMatchType: " + error);
    response.then().spec(responseSpec);
    Assert.assertEquals(LASException.INCORRECT_TYPE, errorCode(error));


  }

  @Test
  public void _12createInvalidKey() {

    Map map = new HashMap();

    map.put("$1111", 111);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            post(path("/classes/" + className));

    Map error = response.as(Map.class);
    System.out.println("_12createInvalidKey: " + error);
    response.then().spec(responseSpec);
    Assert.assertEquals(LASException.INVALID_KEY_NAME, errorCode(error));


  }


  /**
   * valid key.
   */
  @Test
  public void _20update() {

    TestData testData = new TestData();
    testData.initBaseData().geoPoint("geoPoint2");
    Map map = testData.data();
    MLUpdate mlUpdate = MLUpdate.getUpdate();
    mlUpdate.setMany(map);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(mlUpdate.update())).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_20update: " + result);
    Assert.assertEquals(2, result.size());
    Assert.assertNotNull(result.get(LASConstants.KEY_OBJECT_UPDATED_AT));

    Map object = findObject(className, objectId);
    Assert.assertEquals(map.get("string"), object.get("string"));

    System.out.println("_20update: " + object);
    System.out.println("_20update: " + map);
    response.then().spec(responseSpec);

  }

  /**
   * valid key.
   */
  @Test
  public void _20updateRelationEmpty() {

    Map map = new HashMap();

    Map relation = new HashMap();
    relation.put("__op", "AddRelation");
    List list = new ArrayList<>();
    relation.put("objects", list);

    map.put("relation", relation);

    MLUpdate mlUpdate = MLUpdate.getUpdate();
    mlUpdate.addRelation("relation");
    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_20updateRelation: " + result);

    Map object = findObject(className, objectId);

    Map relation1 = (Map) object.get("relation");
    System.out.println("_20updateRelation: " + relation1);
    response.then().spec(responseSpec);
    Assert.assertEquals(pointerClass, relation1.get("className"));
    Assert.assertEquals("Relation", relation1.get("__type"));

  }

  @Test
  public void _20updateRelation() {

    Map map = new HashMap();

    Map pointer = new HashMap();
    pointer.put("__type", "Pointer");
    pointer.put("className", pointerClass);
    pointer.put("objectId", pointerId1);

    Map pointer1 = new HashMap();
    pointer1.put("__type", "Pointer");
    pointer1.put("className", pointerClass);
    pointer1.put("objectId", pointerId2);

    Map pointer2 = new HashMap();
    pointer2.put("__type", "Pointer");
    pointer2.put("className", pointerClass);
    pointer2.put("objectId", pointerId3);

    Map relation = new HashMap();
    relation.put("__op", "AddRelation");
    List list = new ArrayList<>();
    list.add(pointer);
    list.add(pointer1);
    list.add(pointer2);

    relation.put("objects", list);

    map.put("relation", relation);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_20updateRelation: " + result);

    Map object = findObject(className, objectId);

    Map relation1 = (Map) object.get("relation");
    System.out.println("_20updateRelation: " + relation1);
    response.then().spec(responseSpec);
    Assert.assertEquals(pointerClass, relation1.get("className"));
    Assert.assertEquals("Relation", relation1.get("__type"));

  }

  /**
   * valid key.
   */
  @Test
  public void _21removeRelation() {
    Map map = new HashMap();

    Map pointer = new HashMap();
    pointer.put("__type", "Pointer");
    pointer.put("className", className);
    pointer.put("objectId", pointerId3);

    Map relation = new HashMap();
    relation.put("__op", "RemoveRelation");
    List list = new ArrayList<>();
    list.add(pointer);

    relation.put("objects", list);

    map.put("relation", relation);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_21removeRelation: " + result);

    Map object = findObject(className, objectId);

    Map relation1 = (Map) object.get("relation");
    System.out.println("_21removeRelation: " + relation1);
    response.then().spec(responseSpec);
    Assert.assertEquals(pointerClass, relation1.get("className"));
    Assert.assertEquals("Relation", relation1.get("__type"));

  }

  @Test
  public void _21updateIncrement() {

    Map map = new HashMap();

    Map counter = new HashMap();
    counter.put("__op", "Increment");
    counter.put("amount", -15);

    map.put("number", counter);

    Map object = findObject(className, objectId);
    Number number = (Number) object.get("number");

    responseSpec = builder.expectStatusCode(200).build();
    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_21updateIncrement: " + object);
    System.out.println("_21updateIncrement: " + result);
    response.then().spec(responseSpec);

    Number number1 = (Number) result.get("number");

    Assert.assertEquals(number.intValue() - 15, number1.intValue());

  }

  @Test
  public void _21updateIncrementMuilt() {

    Map map = new HashMap();

    Map counter = new HashMap();
    counter.put("__op", "Increment");
    counter.put("amount", -15);

    Map counter2 = new HashMap();
    counter2.put("__op", "Increment");
    counter2.put("amount", -15);

    map.put("test.counter", counter2);
    map.put("number", counter);
    map.put("zhaojing", "fdafdafdafdas");

    Map object = findObject(className, objectId);
    Number number = (Number) object.get("number");

    responseSpec = builder.expectStatusCode(200).build();
    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_21updateIncrement: " + result);
    response.then().spec(responseSpec);

    Number number1 = (Number) result.get("number");
    Map testMap = (Map) result.get("test");
    Number counter1 = (Number) testMap.get("counter");

    Assert.assertEquals(number.intValue() - 15, number1.intValue());
    Assert.assertEquals(-15, counter1.intValue());

  }

  @Test
  public void _21updateAddUnique() {

    Map map = new HashMap();

    Map addUnique = new HashMap();
    addUnique.put("__op", "AddUnique");

    List list = new ArrayList();
    list.add(1311);
    list.add("Zhao Jing");
    list.add("Zhao Jing");

    addUnique.put("objects", list);

    map.put("array", addUnique);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_21updateAddUnique: " + result);

    Map object = findObject(className, objectId);
    System.out.println("_21updateAddUnique: " + object.get("array"));
    response.then().spec(responseSpec);
    Assert.assertEquals(14, ((List) object.get("array")).size());

  }

  @Test
  public void _21updateAdd() {

    Map map = new HashMap();

    Map addUnique = new HashMap();
    addUnique.put("__op", "Add");

    List list = new ArrayList();
    list.add(1311);
    list.add("Zhao Jing");
    list.add("Zhao Jing");
    Map map1 = new HashMap<>();
    map1.put("test", "test0001");
    map1.put("test1", "test0002");
    list.add(map1);

    addUnique.put("objects", list);

    map.put("array", addUnique);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_21updateAdd: " + result);

    Map object = findObject(className, objectId);
    System.out.println("_21updateAdd: " + object.get("array"));
    response.then().spec(responseSpec);
    Assert.assertEquals(14, ((List) object.get("array")).size());

  }

  @Test
  public void _22updateAddChildDoc() {

    Map map = new HashMap();

    Map addUnique = new HashMap();
    addUnique.put("__op", "Add");

    List list = new ArrayList();
    list.add(1311);
    list.add("Zhao Jing");
    list.add("Zhao Jing");

    addUnique.put("objects", list);

    map.put("array", addUnique);
    map.put("child.data1", "testaa");
    map.put("child.data2", "testaa");
    map.put("child.data43", "testaa");
    map.put("child.data4", "testaa");

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_21updateAdd: " + result);

    Map object = findObject(className, objectId);
    System.out.println("_21updateAdd: " + object.get("array"));
    response.then().spec(responseSpec);
    Assert.assertEquals(17, ((List) object.get("array")).size());


  }

  @Test
  public void _22updateRemove() {
    MLUpdate mlUpdate = MLUpdate.getUpdate();
    mlUpdate.arrayRemove("array", 1311, "Zhao Jing");

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(mlUpdate.update())).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_22updateRemove: " + result);

    Map object = findObject(className, objectId);
    System.out.println("_22updateRemove: " + object.get("array"));
    response.then().spec(responseSpec);
    Assert.assertEquals(11, ((List) object.get("array")).size());


  }

  @Test
  public void _23updateDelete() {
    MLUpdate mlUpdate = MLUpdate.getUpdate();
    mlUpdate.unset("pointer");
    mlUpdate.unset("array");

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(mlUpdate.update())).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);

    Map object = findObject(className, objectId);

    Assert.assertEquals(null, object.get("pointer"));
    Assert.assertEquals(null, object.get("array"));

    System.out.println("_23updateDelete: " + result);
    response.then().spec(responseSpec);


  }

  @Test
  public void _21updateAddUniqueInvalid() {

    Map map = new HashMap();

    Map addUnique = new HashMap();
    addUnique.put("__op", "AddUnique");

    addUnique.put("objects", "test");

    map.put("array", addUnique);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_21updateAddUniqueInvalid: " + result);
    response.then().spec(responseSpec);

    Assert.assertEquals(LASException.INVALID_TYPE, errorCode(result));

  }

  /**
   * valid key.
   */
  @Test
  public void _20updateInvalidType() {

    Map map = new HashMap();
    map.put("string", "1111");
    map.put("boolean", true);
    map.put("number", 55);

    Map pointer = new HashMap();
    pointer.put("__type", "Pointer");
    pointer.put("objectId", "537193b930048a95059a9a81");

    map.put("pointer", pointer);


    Map relation = new HashMap();
    relation.put("__op", "AddRelation");
    List list = new ArrayList<>();
    list.add(pointer);

    relation.put("objects", list);

    map.put("relation", relation);

    responseSpec = builder.expectStatusCode(400).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(map)).
            put(path("/classes/" + className + "/" + objectId));

    Map error = response.as(Map.class);
    System.out.println("_20updateInvalidType: " + error);
    response.then().spec(responseSpec);
    Assert.assertEquals(LASException.INVALID_TYPE, errorCode(error));

  }

  @Test
  public void _25updatePointer() {
    MLPointer mlPointer = new MLPointer(pointerId1, pointerClass);
    MLUpdate mlUpdate = MLUpdate.getUpdate();
    mlUpdate.set("pointer", mlPointer);

    responseSpec = builder.expectStatusCode(200).build();

    /**
     * create
     */
    Response response =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().body(LASObjectJsons.serialize(mlUpdate.update())).
            put(path("/classes/" + className + "/" + objectId));

    Map result = response.as(Map.class);
    System.out.println("_25updatePointer: " + result);


    Map object = findObject(className, objectId);
    Map pointer1 = (Map) object.get("pointer");

    System.out.println("_25updatePointer: " + pointer1);
    response.then().spec(responseSpec);

    Assert.assertEquals(mlPointer.get__type(), pointer1.get("__type"));
    Assert.assertEquals(mlPointer.getClassName(), pointer1.get("className"));
    Assert.assertEquals(mlPointer.getObjectId(), pointer1.get("objectId"));
  }

  @Test
  public void _30find() {
    /**
     * create exist.
     */
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            get(path("/classes/" + className));

    Map schema = response1.as(Map.class);
    Assert.assertNotNull(schema);
    System.out.println("_30find: " + schema);
    response1.then().spec(builder.expectStatusCode(200).build());

    List list = (List) schema.get("results");
    Assert.assertEquals(12, list.size());
    Assert.assertEquals(19, ((Map) list.get(0)).size());
  }

  @Test
  public void _30findLimit() {
    MLQuery mlQuery = MLQuery.instance();
    mlQuery.setLimit(4);
    mlQuery.setSkip(8);

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("limit", mlQuery.limit()).
            with().parameter("skip", mlQuery.skip()).
            get(path("/classes/" + className));

    Map schema = response1.as(Map.class);
    Assert.assertNotNull(schema);
    System.out.println("_30findLimit: " + schema);
    response1.then().spec(builder.expectStatusCode(200).build());

    List list = (List) schema.get("results");
    Assert.assertEquals(4, list.size());
  }

  @Test
  public void _30findRelationTo() {
    MLQuery mlQuery = MLQuery.instance();
    MLPointer pointer = new MLPointer(objectId, className);
    mlQuery.relatedTo("relation", pointer);
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("where", LASObjectJsons.serialize(mlQuery.query())).
            get(path("/classes/" + pointerClass));

    Map schema = response1.as(Map.class);
    Assert.assertNotNull(schema);
    System.out.println("_30findRelationTo: " + schema);
    response1.then().spec(builder.expectStatusCode(200).build());

    List list = (List) schema.get("results");
    Assert.assertEquals(2, list.size());

  }

  @Test
  public void _30findRelationToValid() {
    /**
     * create exist.
     */
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("where", "{\"$relatedTo\":{\"objec\":{\"__type\":\"Pointer\",\"className\":\"" + className + "\",\"objectId\":\"" + objectId + "\"},\"key\":\"relation\"}}").
            with().parameter("keys", "number, string").
            get(path("/classes/" + pointerClass));

    Map error = response1.as(Map.class);
    Assert.assertNotNull(error);
    System.out.println("_30findRelationToValid: " + error);
    response1.then().spec(builder.expectStatusCode(400).build());

    Assert.assertEquals(LASException.INVALID_QUERY, errorCode(error));

  }

  @Test
  public void _30findIncludeKeys() {
    MLQuery mlQuery = MLQuery.instance();
    mlQuery.addKey("number");
    mlQuery.addKey("date");
    mlQuery.addKey("string");

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", mlQuery.keys()).
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);
    System.out.println(results);

    List list = (List) results.get("results");
    Map map = (Map) list.get(5);

    System.out.println("_30findInclude: " + map);
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertEquals(6, map.size());
  }

  @Test
  public void _30findRelationToInclude() {
    /**
     * create exist.
     */
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("where", "{\"$relatedTo\":{\"object\":{\"__type\":\"Pointer\",\"className\":\"" + className + "\",\"objectId\":\"" + objectId + "\"},\"key\":\"relation\"}}").
            with().parameter("keys", "number, date").
            get(path("/classes/" + pointerClass));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);
    System.out.println("_30findRelationToInclude: " + results);

    List list = (List) results.get("results");
    Map map = (Map) list.get(0);

    System.out.println("_30findRelationToInclude: " + map);
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertEquals(5, map.size());
    Assert.assertEquals(2, list.size());
  }

  @Test
  public void _30findBasicQuery() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000}}").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findBasicQuery: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());


    Assert.assertTrue(list.size() >= 0);

  }

  @Test
  public void _30findDate() {
    String s = DateUtils.encodeDate(new Date());
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date").
            with().parameter("where", "{\"createdAt\": {\"$lte\": {\"__type\": \"Date\",\"iso\":\"" + s + "\"}}}").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findBasicQuery: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());


    Assert.assertTrue(list.size() == 12);
  }

  @Test
  public void _30findBasicQuery$and() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date").
            with().parameter("where", "{\"$and\": [{\"number\":{\"$gte\":0,\"$lte\":30000000}}, {\"objectId\": \"53f1f2357d84e419e7a40a4d\"}]}").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findBasicQuery$and: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());


    Assert.assertTrue(list.size() >= 0);
  }

  @Test
  public void _30findBasicQuery$ObjectId() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date").
            with().parameter("where", "{\"objectId\": \"" + objectId + "\"}").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findBasicQuery$ObjectId: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());


    Assert.assertTrue(list.size() == 1);
  }

  @Test
  public void _30findIn() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}}").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIn: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() >= 0);
  }

  @Test
  public void _30findInclude() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}}").
            with().parameter("include", "pointer").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findInclude: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() >= 1);

    Map map = (Map) ((Map) list.get(0)).get("pointer");
    Assert.assertEquals(14, map.size());
    Map date = (Map) map.get("date");
    Assert.assertEquals(2, date.size());

  }

  @Test
  public void _30findIncludeComplex() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer2, pointer").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}}").
            with().parameter("include", "pointer.pointerParent,pointer2").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIncludeComplex: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() >= 1);

    Map map = (Map) ((Map) list.get(0)).get("pointer");
    Assert.assertEquals(14, map.size());
    Map date = (Map) map.get("date");
    Assert.assertEquals(2, date.size());

    Map map1 = (Map) map.get("pointerParent");
    Assert.assertEquals(12, map1.size());
    Map date2 = (Map) map1.get("date");
    Assert.assertEquals(2, date2.size());

  }

  @Test
  public void _30findIncludeComplexByPointer() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, \"pointer\": " +
            "{\"__type\":\"Pointer\",\"className\":\"" + pointerClass + "\",\"objectId\":\"" + pointerId1 + "\"}}").
            with().parameter("include", "pointer, pointer.pointerParent").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIncludeComplexByPointer: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() >= 1);

    Map map = (Map) ((Map) list.get(0)).get("pointer");
    Assert.assertEquals(14, map.size());
    Map date = (Map) map.get("date");
    Assert.assertEquals(2, date.size());

    Map map1 = (Map) map.get("pointerParent");
    Assert.assertEquals(12, map1.size());
    Map date2 = (Map) map1.get("date");
    Assert.assertEquals(2, date2.size());

  }

  @Ignore
  public void _30findIncludeComplexWithGEO() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer, geoPoint").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, " +
            "\"geoPoint\": {\"$nearSphere\":{\"__type\": \"GeoPoint\", \"latitude\": 31.11339, \"longitude\": 121.10013},\"$maxDistance\": 500}," +
            "\"pointer\": {\"__type\":\"Pointer\",\"className\":\"" + pointerClass + "\",\"objectId\":\"" + pointerId1 + "\"}}").
            with().parameter("include", "pointer.pointerParent").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIncludeComplexByPointer: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() >= 1);

    Map map = (Map) ((Map) list.get(0)).get("pointer");
    Assert.assertEquals(14, map.size());
    Map date = (Map) map.get("date");
    Assert.assertEquals(2, date.size());

    Map map1 = (Map) map.get("pointerParent");
    Assert.assertEquals(12, map1.size());
    Map date2 = (Map) map1.get("date");
    Assert.assertEquals(2, date2.size());

  }

  @Ignore
  public void _30findIncludeComplexWithGEO1() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer, geoPoint").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, " +
            "\"geoPoint\": {\"$geoWithin\":{\"$box\":[{\"__type\": \"GeoPoint\", \"latitude\": 31.1100, \"longitude\": 121.100}, {\"__type\": \"GeoPoint\", \"latitude\": 31.11339, \"longitude\": 121.10013}]}}," +
            "\"pointer\": {\"__type\":\"Pointer\",\"className\":\"" + pointerClass + "\",\"objectId\":\"" + pointerId1 + "\"}}").
            with().parameter("include", "pointer.pointerParent, pointer").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIncludeComplexByPointer: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() >= 1);

    Map map = (Map) ((Map) list.get(0)).get("pointer");
    Assert.assertEquals(14, map.size());
    Map date = (Map) map.get("date");
    Assert.assertEquals(2, date.size());

    Map map1 = (Map) map.get("pointerParent");
    Assert.assertEquals(12, map1.size());
    Map date2 = (Map) map1.get("date");
    Assert.assertEquals(2, date2.size());

  }

  @Test
  public void _30findNotSelect() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, \"pointer\": " +
            "{\"__type\":\"Pointer\",\"className\":\"" + pointerClass + "\",\"objectId\":\"" + pointerId1 + "\"}, " +
            "\"hometown\":{\"$dontSelect\":{\"query\":{\"className\":\"" + pointerClass + "\",\"where\":{\"winPct\":{\"$gt\":0.5}}},\"key\":\"city\"}}}").
            with().parameter("include", "pointer.pointerParent, test").
            with().parameter("limit", "2").
            with().parameter("skip", "2").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIncludeComplexByPointer: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() == 2);

    Map map = (Map) ((Map) list.get(0)).get("pointer");
    Assert.assertEquals(14, map.size());
    Map date = (Map) map.get("date");
    Assert.assertEquals(2, date.size());

    Map map1 = (Map) map.get("pointerParent");
    Assert.assertEquals(12, map1.size());
    Map date2 = (Map) map1.get("date");
    Assert.assertEquals(2, date2.size());

  }

  @Test
  public void _30findSelect() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, \"pointer\": " +
            "{\"__type\":\"Pointer\",\"className\":\"" + pointerClass + "\",\"objectId\":\"" + pointerId1 + "\"}, " +
            "\"hometown\":{\"$select\":{\"query\":{\"className\":\"" + pointerClass + "\",\"where\":{\"winPct\":{\"$gt\":0.5}}},\"key\":\"city\"}}}").
            with().parameter("include", "pointer.pointerParent, pointer").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIncludeComplexByPointer: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() == 0);

  }

  @Test
  public void _30findSelectNotWhere() {
    MLQuery mlQuery = MLQuery.instance();
    mlQuery.addKey("number");
    mlQuery.addKey("date");
    mlQuery.addKey("pointer");
    mlQuery.greaterThanOrEqualTo("number", 0);
    mlQuery.lessThanOrEqualTo("number", 30000000);
    mlQuery.notIn("number", 13131, 31313131, 5531515);
    mlQuery.equalTo("pointer", new MLPointer(pointerId1, pointerClass));
    MLQuery.SelectOperator selectOperator = new MLQuery.SelectOperator(pointerClass, "city");
    mlQuery.select("hometown", selectOperator);
    mlQuery.setIncludes("pointer.pointerParent, pointer");

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", mlQuery.keys()).
            with().parameter("where", LASObjectJsons.serialize(mlQuery.query())).
            with().parameter("include", mlQuery.includes()).
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findIncludeComplexByPointer: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertTrue(list.size() == 11);

  }

  @Test
  public void _30findInvalidSelect() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, \"pointer\": " +
            "{\"__type\":\"Pointer\",\"className\":\"" + pointerClass + "\",\"objectId\":\"" + pointerId1 + "\"}, " +
            "\"hometown\":{\"$dontSelect\":{\"query\":{\"classNa\":\"" + pointerClass + "\",\"where\":{\"winPct\":{\"$gt\":0.5}}},\"key\":\"city\"}}}").
            with().parameter("include", "pointer.pointerParent, pointer").
            get(path("/classes/" + className));

    Map error = response1.as(Map.class);
    Assert.assertNotNull(error);

    System.out.println("_30findInvalidSelect: " + error);

    Assert.assertEquals(LASException.INVALID_QUERY, errorCode(error));
    response1.then().spec(builder.expectStatusCode(400).build());

  }

  @Test
  public void _30findInQuery() {
    MLQuery mlQuery = MLQuery.instance();
    mlQuery.addKey("number");
    mlQuery.addKey("date");
    mlQuery.addKey("pointer");
    mlQuery.greaterThanOrEqualTo("number", 0);
    mlQuery.lessThanOrEqualTo("number", 30000000);
    mlQuery.notIn("number", 13131, 31313131, 5531515);
    mlQuery.equalTo("pointer", new MLPointer(pointerId1, pointerClass));
    MLQuery.SelectOperator selectOperator = new MLQuery.SelectOperator(pointerClass, "city");
    selectOperator.$gt("winPct", 0.5);
    mlQuery.notSelect("hometown", selectOperator);
    mlQuery.setIncludes("pointer.pointerParent, pointer");
    MLQuery.InQueryOperator inQueryOperator = new MLQuery.InQueryOperator(pointerClass);
    inQueryOperator.$exists("image", false);
    mlQuery.inQuery("pointer", inQueryOperator);

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", mlQuery.keys()).
            with().parameter("where", LASObjectJsons.serialize(mlQuery.query())).
            with().parameter("include", mlQuery.includes()).
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    List list = (List) results.get("results");
    System.out.println("_30findInQuery: results size: " + list.size());
    response1.then().spec(builder.expectStatusCode(200).build());
    Assert.assertTrue(list.size() > 5);
  }

  @Test
  public void _30findInvalidInQuery() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date, pointer").
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, " +
            "\"hometown\":{\"$dontSelect\":{\"query\":{\"className\":\"" + pointerClass + "\",\"where\":{\"winPct\":{\"$gt\":0.5}}},\"key\":\"city\"}}," +
            "\"pointer\":{\"$inQuery\":{\"where\":{\"image\":{\"$exists\": false}},\"className\":\"" + pointerClass + "\"}, \"bb\": 1}}").
            with().parameter("include", "pointer.pointerParent, pointer").
            get(path("/classes/" + className));

    Map error = response1.as(Map.class);
    Assert.assertNotNull(error);

    System.out.println("_30findInvalidInQuery: " + error);
    response1.then().spec(builder.expectStatusCode(200).build());
  }

  @Test
  public void _30findCount() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date").
            with().parameter("count", 1).
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    Object count = results.get("count");
    System.out.println("_30findCount: results size: " + count);
    response1.then().spec(builder.expectStatusCode(200).build());


    Assert.assertEquals(12, count);
  }

  @Test
  public void _30findInArray() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("where", "{\"number\":{\"$gte\":0,\"$lte\":30000000,\"$nin\":[13131, 31313131, 5531515]}, " +
            "\"hometown\":{\"$dontSelect\":{\"query\":{\"className\":\"" + pointerClass + "\",\"where\":{\"winPct\":{\"$gt\":0.5}}},\"key\":\"city\"}}," +
            "\"array\": 2," +
            "\"pointer\":{\"$inQuery\":{\"where\":{\"image\":{\"$exists\": false}},\"className\":\"" + pointerClass + "\"}, \"bb\": 1}}").
            with().parameter("include", "pointer.pointerParent, test").
            with().parameter("skip", 0).
            with().parameter("limit", 5).
            with().parameter("order", "-createdAt, updatedAt, test").
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    System.out.println("_30findComplexQuery: " + results);
    List list = (List) results.get("results");
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertEquals(5, list.size());


  }

  @Test
  public void _30findComplexQuery() {
    MLQuery mlQuery = MLQuery.instance();
    mlQuery.addKey("number");
    mlQuery.addKey("date");
    mlQuery.addKey("pointer");
    mlQuery.greaterThanOrEqualTo("number", 0);
    mlQuery.lessThanOrEqualTo("number", 30000000);
    mlQuery.notIn("number", 13131, 31313131, 5531515);
    mlQuery.equalTo("pointer", new MLPointer(pointerId1, pointerClass));
    MLQuery.SelectOperator selectOperator = new MLQuery.SelectOperator(pointerClass, "city");
    selectOperator.$gt("winPct", 0.5);
    mlQuery.notSelect("hometown", selectOperator);
    mlQuery.setIncludes("pointer.pointerParent, pointer");
    MLQuery.InQueryOperator inQueryOperator = new MLQuery.InQueryOperator(pointerClass);
    inQueryOperator.$exists("image", false);
    mlQuery.inQuery("pointer", inQueryOperator);
    mlQuery.exists("number");
    mlQuery.arrayAll("array", 2, 5, 8);
    mlQuery.sort(MLQuery.SORT_DESC, "createdAt");
    mlQuery.sort(MLQuery.SORT_ASC, "updatedAt", "test");
    mlQuery.setSkip(0);
    mlQuery.setLimit(5);

    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", mlQuery.keys()).
            with().parameter("where", LASObjectJsons.serialize(mlQuery.query())).
            with().parameter("include", mlQuery.includes()).
            with().parameter("skip", mlQuery.skip()).
            with().parameter("limit", mlQuery.limit()).
            with().parameter("order", mlQuery.sort()).
            get(path("/classes/" + className));

    Map results = response1.as(Map.class);
    Assert.assertNotNull(results);

    System.out.println("_30findComplexQuery: " + results);
    List list = (List) results.get("results");
    response1.then().spec(builder.expectStatusCode(200).build());

    Assert.assertEquals(5, list.size());

    Object data1 = ((Map) list.get(0)).get("createdAt");
    Object data2 = ((Map) list.get(1)).get("createdAt");
    Object data3 = ((Map) list.get(2)).get("createdAt");

    Assert.assertTrue(DateUtils.parseDate(data1.toString()).getTime() > DateUtils.parseDate(data2.toString()).getTime());
    Assert.assertTrue(DateUtils.parseDate(data2.toString()).getTime() > DateUtils.parseDate(data3.toString()).getTime());
    Assert.assertTrue(DateUtils.parseDate(data1.toString()).getTime() > DateUtils.parseDate(data3.toString()).getTime());

  }

  @Test
  public void _30findCountInvalid() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            with().parameter("keys", "number, date").
            with().parameter("count", "b").
            get(path("/classes/" + className));

    response1.then().spec(builder.expectStatusCode(400).build());
  }

  @Test
  public void _40delete() {
    Response response1 =
        expect().defaultParser(Parser.JSON).
            when().
            with().headers(header).
            delete(path("/classes/" + className + "/" + objectId));

    Map responseMsg1 = response1.as(Map.class);
    System.out.println("_40delete: " + responseMsg1);
    Assert.assertNotNull(responseMsg1.get("number"));
    response1.then().spec(builder.expectStatusCode(200).build());

  }

}
