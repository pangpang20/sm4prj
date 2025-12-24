/**
 * PLA Warning! Make sure your're older than 18
 */
package com.audaque.hiveudf;

import org.apache.hadoop.hive.common.type.HiveVarchar;
import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.io.Text;

/**
 * @author qiulin.li
 *
 */

@Description(name = "sm2_decrypt", value = "Decrypts SM2 encrypted data and returns a UTF-8 encoded string")
public class SM2Decrypt extends GenericUDF {
    private PrimitiveObjectInspector dataInspector;
    private PrimitiveObjectInspector keyInspector;

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != 2) {
            throw new UDFArgumentException("sm2_decrypt requires 2 parameters: encrypted data and key");
        }

        if (!(arguments[0] instanceof PrimitiveObjectInspector) || 
            !(arguments[1] instanceof PrimitiveObjectInspector)) {
            throw new UDFArgumentException("sm2_decrypt parameters must be primitive types");
        }

        dataInspector = (PrimitiveObjectInspector) arguments[0];
        keyInspector = (PrimitiveObjectInspector) arguments[1];

//        if (dataInspector.getPrimitiveCategory() != PrimitiveObjectInspector.PrimitiveCategory.STRING ||
//            keyInspector.getPrimitiveCategory() != PrimitiveObjectInspector.PrimitiveCategory.STRING) {
//            throw new UDFArgumentException("sm2_decrypt parameters must be string or varchar type");
//        }

        return PrimitiveObjectInspectorFactory.writableStringObjectInspector;
    }
    
    private String getStringValue(Object obj, PrimitiveObjectInspector inspector) {
        if (obj == null) {
            return null;
        }
        
        if (inspector instanceof StringObjectInspector) {
            return ((StringObjectInspector) inspector).getPrimitiveJavaObject(obj);
        } else {
            Object primitiveObj = inspector.getPrimitiveJavaObject(obj);
            if (primitiveObj instanceof HiveVarchar) {
                return ((HiveVarchar) primitiveObj).getValue();
            } else {
                return primitiveObj.toString();
            }
        }
    }

    @Override
    public Object evaluate(DeferredObject[] arguments) throws HiveException {
        // Get input parameter values
        String encryptedData = getStringValue(arguments[0].get(), dataInspector);
        String privateKeyStr = getStringValue(arguments[1].get(), keyInspector);

        // Handle null or empty values
        if (encryptedData == null || encryptedData.isEmpty() || privateKeyStr == null || privateKeyStr.isEmpty()) {
            return new Text(encryptedData);
        }

        try {
            return new Text(SM2Utils.decryptKeyHexValueBase64(privateKeyStr, encryptedData));
        } catch (Exception e) {
            throw new HiveException("SM2 decryption failed");
//            return encryptedData;
        }
    }


    @Override
    public String getDisplayString(String[] children) {
        return "sm2_decrypt(" + children[0] + ", " + children[1] + ")";
    }
    
}