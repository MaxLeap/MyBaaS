package com.maxleap.domain;


import com.maxleap.domain.base.ObjectId;
import com.maxleap.domain.mongo.BaseEntity;

/**
 * Created by.
 * User: ben
 * Date: 12/22/14
 * Time: 5:25 PM
 * Email:benkris1@gmail.com
 *
 * @ilengend
 */
public class LASAtomSeg extends BaseEntity<ObjectId> {

  public static final String TYPE = "type";
  public static final String ATOM_ID = "atomId";

  private String atomId;
  private int  type = 1;
  private String expression;




  public String getAtomId() {
    return atomId;
  }

  public void setAtomId(String atomId) {
    this.atomId = atomId;
  }


  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }


  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }
}
