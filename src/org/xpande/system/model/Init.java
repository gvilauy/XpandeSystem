package org.xpande.system.model;

import org.xpande.system.migration.ADElement;

import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 8/20/19.
 */
public class Init {

    public Init() {
    }

    public void prueba(Properties ctx){

        try{

            ADElement element = new ADElement(ctx, 1000935, null);
            FileOutputStream os = new FileOutputStream("/tmp/" + "prueba.xml");
            XMLEncoder encoder = new XMLEncoder(os);
            encoder.writeObject(element);
            encoder.close();
        }
        catch (Exception e){
            //throw new AdempiereException(e);
        }

    }
}
