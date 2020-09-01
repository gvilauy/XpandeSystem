package org.xpande.system.process;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MMenu;
import org.compiere.model.MTree_Base;
import org.compiere.model.MTree_NodeMM;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Proceso para copiar menues.
 * Product: Adempiere ERP & CRM Smart Business Solution. Localization : Uruguay - Xpande
 * Xpande. Created by Gabriel Vila on 9/1/20.
 */
public class CopiaMenu extends SvrProcess {

    private int adTreeOrigenID = -1;
    private int adTreeDestinoID = -1;
    private String nodoCopia = null;

    private MTree_Base treeOrigen = null;
    private MTree_Base treeDestino = null;

    @Override
    protected void prepare() {

        ProcessInfoParameter[] para = getParameter();

        for (int i = 0; i < para.length; i++){

            String name = para[i].getParameterName();

            if (name != null){

                if (name.trim().equalsIgnoreCase("AD_Tree_ID")){
                    this.adTreeOrigenID = ((BigDecimal)para[i].getParameter()).intValueExact();
                }
                else if (name.trim().equalsIgnoreCase("AD_Tree_ID_To")){
                    this.adTreeDestinoID = ((BigDecimal)para[i].getParameter()).intValueExact();
                }
                else if (name.trim().equalsIgnoreCase("Name")){
                    if (para[i].getParameter() != null){
                        this.nodoCopia = ((String)para[i].getParameter()).trim();
                    }
                }
            }
        }

        this.treeOrigen = MTree_Base.get(getCtx(), this.adTreeOrigenID, get_TrxName());
        this.treeDestino = MTree_Base.get(getCtx(), this.adTreeDestinoID, get_TrxName());
    }

    @Override
    protected String doIt() throws Exception {

        String sql;

        try{

            // Nodo ID a Copiar
            int parentNodeID = 0;

            // Si me indican nodo a copiar en menú origen
            if ((this.nodoCopia != null) && (!this.nodoCopia.trim().equalsIgnoreCase(""))){
                // Busco ID de este nodo según nombre recibido, si no hay aviso y no hago nada.
                sql = " select ad_menu_id from ad_menu where lower(name) ='" + this.nodoCopia.trim().toLowerCase() + "' ";
                parentNodeID = DB.getSQLValueEx(null, sql);
                if (parentNodeID <= 0){
                    return "@Error@ " + "No existe una rama en el Menú Origen con el nombre indicado.";
                }
            }

            // Copio nodos de manera recursiva
            this.copioNodo(parentNodeID, 0);

        }
        catch (Exception e){
            throw new AdempiereException(e);
        }

        return "OK";
    }

    /***
     * Método recursivo para copiar nodos del menú.
     * Xpande. Created by Gabriel Vila on 9/1/20.
     * @param parentNodeOrigenID
     * @param parentNodeDestinoID
     */
    private void copioNodo(int parentNodeOrigenID, int parentNodeDestinoID){

        String sql = "";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            sql = " select a.node_id, a.seqno " +
                    " from ad_treenodemm a " +
                    " inner join ad_menu b on a.node_id = b.ad_menu_id " +
                    " inner join ad_menu_trl c on b.ad_menu_id = c.ad_menu_id " +
                    " where a.ad_tree_id =" + adTreeOrigenID +
                    " and a.parent_id =" + parentNodeOrigenID +
                    " order by a.seqno";

        	pstmt = DB.prepareStatement(sql, get_TrxName());
        	rs = pstmt.executeQuery();

        	while(rs.next()){
                MTree_NodeMM nodeMM = new MTree_NodeMM(this.treeDestino, rs.getInt("node_id"));
                nodeMM.setParent_ID(parentNodeDestinoID);
                nodeMM.setSeqNo(rs.getInt("seqno"));
                nodeMM.saveEx();

                // Llamada recursiva para este nodo
                this.copioNodo(rs.getInt("node_id"), nodeMM.getNode_ID());
        	}
        }
        catch (Exception e){
            throw new AdempiereException(e);
        }
        finally {
            DB.close(rs, pstmt);
        	rs = null; pstmt = null;
        }
    }

}
