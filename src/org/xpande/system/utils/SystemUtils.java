package org.xpande.system.utils;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MColumn;
import org.compiere.model.MTable;
import org.compiere.model.MWindow;
import org.compiere.model.X_AD_Ref_Table;
import org.compiere.util.DB;
import org.xpande.system.migration.ADRef_Table;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * Clase de métodos staticos referidos a funcionalidades de System.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 11/4/19.
 */
public final class SystemUtils {

    /***
     * Obtiene y setea modelo para datos de referencia de tabla.
     * Xpande. Created by Gabriel Vila on 11/4/19.
     * @param ctx
     * @param adReferenceID
     * @param trxName
     * @return
     */
    public static ADRef_Table getRefTableByReferenceID(Properties ctx, int adReferenceID, String trxName){

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        ADRef_Table adRefTable = null;

        try{
            sql = " select * from ad_ref_table where ad_reference_id =" + adReferenceID;

        	pstmt = DB.prepareStatement(sql, trxName);
        	rs = pstmt.executeQuery();

        	if (rs.next()){
        	    adRefTable = new ADRef_Table(ctx, 0, trxName);
        	    adRefTable.setAD_Org_ID(rs.getInt("ad_org_id"));
                adRefTable.setAD_Reference_ID(adReferenceID);
                adRefTable.setAD_Table_ID(rs.getInt("ad_table_id"));
                adRefTable.setAD_Key(rs.getInt("ad_key"));
                adRefTable.setAD_Display(rs.getInt("ad_display"));
                adRefTable.setIsValueDisplayed((rs.getString("IsValueDisplayed").equalsIgnoreCase("Y")) ? true : false);
                adRefTable.setWhereClause(rs.getString("whereclause"));
                adRefTable.setOrderByClause(rs.getString("orderbyclause"));
                adRefTable.setEntityType(rs.getString("entitytype"));
                adRefTable.setAD_Window_ID(rs.getInt("ad_window_id"));
                adRefTable.setIsAlert((rs.getString("IsAlert").equalsIgnoreCase("Y")) ? true : false);
                adRefTable.setDisplaySQL(rs.getString("displaysql"));
                adRefTable.setIsDisplayIdentifier((rs.getString("IsDisplayIdentifier").equalsIgnoreCase("Y")) ? true : false);

                // Seteo atributos para la migración
                MTable table = new MTable(ctx, rs.getInt("ad_table_id"), null);
                adRefTable.setNombreTabla(table.getTableName());

                if (rs.getInt("ad_window_id") > 0){
                    MWindow window = new MWindow(ctx, rs.getInt("ad_window_id"), null);
                    adRefTable.setNombreVentana(window.getName());
                }

                if (rs.getInt("ad_key") > 0){
                    MColumn columnKey = new MColumn(ctx, rs.getInt("ad_key"), null);
                    adRefTable.setNombreColKey(columnKey.getColumnName());
                }

                if (rs.getInt("ad_display") > 0){
                    MColumn columnDisplay = new MColumn(ctx, rs.getInt("ad_display"), null);
                    adRefTable.setNombreColDisplay(columnDisplay.getColumnName());
                }
            }
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
        	rs = null; pstmt = null;
        }

        return adRefTable;
    }
}
