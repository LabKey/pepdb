package org.scharp.atlas.pepdb;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.labkey.api.data.*;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.scharp.atlas.pepdb.model.*;

/**
 * Handles Data Operations.
 *
 * @version $Id: PeptideManager.java 34530 2009-08-10 20:06:30Z sravani $
 */
public class PepDBManager
{

    private static Logger log = Logger.getLogger(PepDBManager.class);
    private static PepDBSchema schema = PepDBSchema.getInstance();
    /**
     * Static class
     */
    private PepDBManager()
    {
    }

    public static PeptideGroup[] getPeptideGroups() throws SQLException
    {
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID);
        return Table.select(schema.getTableInfoPeptideGroups(),
                schema.getTableInfoPeptideGroups().getColumns()
                , null, sort, PeptideGroup.class);
    }

    public static PeptidePool[] getPeptidePools() throws SQLException
    {
        Sort sort = new Sort(PepDBSchema.COLUMN_PEPTIDE_POOL_ID);
        return Table.select(schema.getTableInfoPeptidePools(),
                schema.getTableInfoPeptidePools().getColumns()
                , null, sort, PeptidePool.class);
    }
    public static PoolType[] getPoolTypes() throws SQLException
    {
        Sort sort = new Sort(PepDBSchema.COLUMN_POOL_TYPE_ID);
        return Table.select(schema.getTableInfoPoolType(),
                schema.getTableInfoPoolType().getColumns()
                , null, sort, PoolType.class);
    }

    public static ProteinCategory[] getProteinCategory() throws SQLException
    {
        return Table.select(schema.getTableInfoProteinCat(),
                schema.getTableInfoProteinCat().getColumns(), null,
                new Sort(PepDBSchema.COLUMN_PROTEIN_CAT_ID), ProteinCategory.class);
    }

    public static OptimalEpitopeList[] getOptimalEpitopeList() throws SQLException
    {
        return Table.select(schema.getTableInfoOptimalEpitopeList(),
                schema.getTableInfoOptimalEpitopeList().getColumns(), null,
                new Sort(PepDBSchema.COLUMN_OPTIMAL_EPITOPE_LIST_ID), OptimalEpitopeList.class);
    }

    public static Peptides[] getPeptides() throws SQLException
    {
        TableInfo tInfo = schema.getTableInfoPeptides();
        return Table.select(tInfo,tInfo.getColumns(),
                null,new Sort("peptide_id"),Peptides.class);
    }

    /**
     * @param peptideGroup
     * @return the number of peptides in a given group
     * @throws SQLException
     */
    public static Integer getCount(Integer peptideGroup) throws SQLException
    {
        SimpleFilter containerFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID, peptideGroup);
        Integer count = Table.executeSingleton(schema.getSchema(),
                "SELECT COUNT(*) FROM "
                        + PepDBSchema.getInstance().getTableInfoViewGroupPeptides()
                        + " WHERE peptide_group_id=? ", new Object[] { peptideGroup },
                Integer.class);
        return count;
    }

    public static Peptides insertPeptide(User user, Peptides p) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptides();
        Peptides dbPeptide = peptideExists(p, tInfo);
        if(dbPeptide == null)
            dbPeptide = Table.insert(user,tInfo,p);
        return dbPeptide;
    }

    public static Peptides updatePeptide(User user, Peptides p) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptides();
        return Table.update(user,tInfo,p,p.getPeptide_id());
    }

    public static PeptidePool updatePeptidePool(User user, PeptidePool p) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptidePools();
        return Table.update(user,tInfo,p,p.getPeptide_pool_id());
    }

    public static Parent insertParent(User user,Parent parent) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoParent();
        return Table.insert(user,tInfo,parent);
    }

    public static Parent parentExists(Parent p) throws SQLException
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoParent();
        Parent dbParent = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), p.getPeptide_id());
        sFilter.addCondition(FieldKey.fromParts("linked_parent"), p.getLinked_parent());
        Parent[] dbParents = Table.select(tInfo,
                tInfo.getColumns("peptide_id,linked_parent")
                ,sFilter,null,Parent.class);
        if(dbParents != null && dbParents.length>0)
            dbParent = dbParents[0];
        return dbParent;
    }

    public static Peptides peptideExists(Peptides p,TableInfo tInfo) throws SQLException
    {
        Peptides dbPeptide = null;
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_sequence"), p.getPeptide_sequence());
        Peptides[] dbPeptides = Table.select(tInfo,
                tInfo.getColumns(),sFilter,null,Peptides.class);
        if(dbPeptides != null && dbPeptides.length>0)
            dbPeptide = dbPeptides[0];
        return dbPeptide;
    }

    public static Source insertSource(User user, Source src) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoSource();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_group_id"), src.getPeptide_group_id());
        sFilter.addCondition(FieldKey.fromParts("peptide_id"), src.getPeptide_id());
        Source dbSrc ;
        Source[] dbSrcs = Table.select(tInfo,tInfo.getColumns(),sFilter,null,Source.class);
        if(dbSrcs != null && dbSrcs.length>0)
            dbSrc = dbSrcs[0];
        else
            dbSrc = Table.insert(user,tInfo,src);
        dbSrc.setIn_current_file(true);
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String sql = "UPDATE pepdb.peptide_group_assignment SET in_current_file = true,modified = ?,modifiedby = ? WHERE peptide_group_assignment_id=?";
        Object[] params = { date,user.getUserId(),dbSrc.getPeptide_group_assignment_id() };
        Table.execute(schema.getSchema(),sql,params);
        return dbSrc;
    }

    public static PeptidePool insertPeptidePool(User u,PeptidePool pPool) throws Exception
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPeptidePools();
        PeptidePool dbPool = null;
        dbPool = Table.insert(u,tInfo,pPool);
        return dbPool;
    }

    public static PeptidePoolAssignment insertPeptidesInPool(User user,PeptidePoolAssignment src) throws Exception{
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPoolAssignment();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_pool_id"), src.getPeptide_pool_id());
        sFilter.addCondition(FieldKey.fromParts("peptide_id"), src.getPeptide_id());
        PeptidePoolAssignment dbSrc ;
        PeptidePoolAssignment[] dbSrcs = Table.select(tInfo,tInfo.getColumns(),sFilter,null,PeptidePoolAssignment.class);
        if(dbSrcs != null && dbSrcs.length>0)
            return null;
        else
            dbSrc = Table.insert(user,tInfo,src);
        return dbSrc;
    }

    public static Source[] getSourcesForPeptide(String peptideId)
    {
        Source[] sources = null;
        try
        {
            SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), Integer.parseInt(peptideId));
            TableInfo tInfo = PepDBSchema.getInstance().getTableInfoViewGroupPeptides();
            sources = Table.select(tInfo,
                    tInfo.getColumns("peptide_group_id,peptide_id_in_group,peptide_group_name,frequency_number,frequency_number_date"),
                    sfilter,
                    null, Source.class);
            if (sources == null || sources.length < 1)
                return null;
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
        return sources;
    }

    public static PeptidePool[] getPoolsForPeptide(String peptideId) throws SQLException
    {
        SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_id"), Integer.parseInt(peptideId));
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoViewPoolPeptides();
        PeptidePool[] pools = Table.select(tInfo,Table.ALL_COLUMNS,sfilter,null,PeptidePool.class);
        if (pools == null || pools.length < 1)
            return null;
        return pools;
    }

    public static PeptideGroup insertGroup(Container c, User user, PeptideGroup pg) throws SQLException {
        PeptideGroup resultGroup = Table.insert(user, PepDBSchema.getInstance().getTableInfoPeptideGroups(), pg);
        return resultGroup;
    }

    public static PeptideGroup  updatePeptideGroup(User user,PeptideGroup pg) throws SQLException{
        return Table.update(user, PepDBSchema.getInstance().getTableInfoPeptideGroups(),pg,pg.getPeptide_group_id());
    }

    public static HashMap<String,Peptides> getPeptideSequenceMap() throws SQLException
    {
        HashMap<String,Peptides> peptideSequenceMap = new HashMap<String,Peptides>();
        Peptides[]  peptides = getPeptides();
        for(Peptides peptide : peptides)
            peptideSequenceMap.put(peptide.getPeptide_sequence().trim().toUpperCase(),peptide);
        return peptideSequenceMap;
    }

    public static HashMap<String,PeptideGroup> getPeptideGroupMap() throws SQLException
    {
        HashMap<String,PeptideGroup> groupsMap = new HashMap<String,PeptideGroup>();
        PeptideGroup[] peptideGroups = getPeptideGroups();
        for(PeptideGroup pGroup:peptideGroups)
            groupsMap.put(pGroup.getPeptide_group_name().trim().toUpperCase(),pGroup);
        return groupsMap;
    }

    public static HashMap<String,PeptidePool> getPeptidePoolMap() throws SQLException
    {
        HashMap<String,PeptidePool> poolsMap = new HashMap<String,PeptidePool>();
        PeptidePool[] peptidePools = getPeptidePools();
        for(PeptidePool pPool:peptidePools)
            poolsMap.put(pPool.getPeptide_pool_name().trim().toUpperCase(),pPool);
        return poolsMap;
    }

    public static HashMap<String,PoolType> getPoolTypeMap() throws SQLException
    {
        HashMap<String,PoolType> poolTypeMap = new HashMap<String,PoolType>();
        PoolType[] poolTypes = getPoolTypes();
        for(PoolType pType : poolTypes)
            poolTypeMap.put(pType.getPool_type_desc().trim().toUpperCase(),pType);
        return poolTypeMap;
    }

    public static HashMap<String,ProteinCategory> getProteinCatMap() throws SQLException
    {
        HashMap<String,ProteinCategory> proCatMap = new HashMap<String,ProteinCategory>();
        ProteinCategory[] proCats = getProteinCategory();
        for(ProteinCategory proCat : proCats)
            proCatMap.put(proCat.getProtein_cat_desc().trim().toUpperCase(),proCat);
        return proCatMap;
    }

    public static HashMap<Integer,ProteinCategory> getProteinCatIDMap() throws SQLException
    {
        HashMap<Integer,ProteinCategory> proCatMap = new HashMap<Integer,ProteinCategory>();
        ProteinCategory[] proCats = getProteinCategory();
        for(ProteinCategory proCat : proCats)
            proCatMap.put(proCat.getProtein_cat_id(),proCat);
        return proCatMap;
    }

    public static HashMap<String,OptimalEpitopeList> getOptimalEpitopeListMap() throws SQLException
    {
        HashMap<String,OptimalEpitopeList> optListMap = new HashMap<String,OptimalEpitopeList>();
        OptimalEpitopeList[] optLists = getOptimalEpitopeList();
        for(OptimalEpitopeList optList : optLists)
            optListMap.put(optList.getOptimal_epitope_list_desc().trim().toUpperCase(),optList);
        return optListMap;
    }

    public static Peptides getPeptideById(Integer peptideId) throws SQLException
    {
        TableInfo tInfo = schema.getTableInfoPeptides();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_ID,peptideId);
        return Table.selectObject(tInfo,sFilter,null, Peptides.class);
    }

    public static Peptides[] getParentPeptides(Peptides p) throws SQLException
    {
        TableInfo tInfo = schema.getTableInfoPeptides();
        String sql = "select * from pepdb.peptides where peptides.protein_cat_id = ? and ? between peptides.amino_acid_start_pos-2 and peptides.amino_acid_end_pos\n" +
                "and ? between peptides.amino_acid_start_pos and peptides.amino_acid_end_pos+2 and child = false";
        Peptides[] peptides = Table.executeQuery(schema.getSchema(),sql,new Object[]{p.getProtein_cat_id(),p.getAmino_acid_start_pos(),p.getAmino_acid_end_pos()},Peptides.class);
        return peptides;
    }

    public static Peptides[] getHyphanatedParents(Peptides p) throws SQLException
    {
        String sql = "select * from "+schema.getTableInfoPeptides()+" where peptides.protein_cat_id = ? " +
                " and peptides.peptide_sequence LIKE '%"+p.getPeptide_sequence()+"%' " +
                " and child = false";
        Peptides[] peptides = Table.executeQuery(schema.getSchema(),sql,new Object[]{p.getProtein_cat_id()},Peptides.class);
        return peptides;
    }

    public static PeptideGroup getPeptideGroupByID(Integer groupId) throws SQLException
    {
        TableInfo tInfo = schema.getTableInfoPeptideGroups();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_GROUP_ID,groupId);
        return Table.selectObject(tInfo,sFilter,null, PeptideGroup.class);
    }

    public static ProteinCategory getProCatByID(Integer procatId) throws SQLException
    {
        TableInfo tInfo = schema.getTableInfoProteinCat();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PROTEIN_CAT_ID,procatId);
        return Table.selectObject(tInfo,sFilter,null, ProteinCategory.class);
    }

    public static PeptideGroup getPeptideGroupByName(PeptideGroup pg) throws SQLException
    {
        PeptideGroup [] groups = null;
        TableInfo tInfo = schema.getTableInfoPeptideGroups();
        SQLFragment sql = new SQLFragment("SELECT * FROM "+tInfo+" WHERE UPPER(peptide_group_name) = ? ");
        sql.add(pg.getPeptide_group_name().trim().toUpperCase());
        if(pg.getPeptide_group_id() != null)
        {
                sql.append("AND peptide_group_id != ?");
                sql.add(pg.getPeptide_group_id());
        }
        groups = Table.executeQuery(schema.getSchema(),sql, PeptideGroup.class);
        if(groups != null && groups.length >0)
        return groups[0];
        else return null;
    }

    public static PeptidePool getPeptidePoolByID(Integer poolId) throws SQLException
    {
        TableInfo tInfo = schema.getTableInfoPeptidePools();
        SimpleFilter sFilter = new SimpleFilter(PepDBSchema.COLUMN_PEPTIDE_POOL_ID,poolId);
        return Table.selectObject(tInfo,sFilter,null, PeptidePool.class);
    }

    public static int updateInCurrentFile(User user) throws SQLException
    {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        String sql = "UPDATE pepdb.peptide_group_assignment SET in_current_file = false,modified = ?,modifiedby = ?";
        Object[] params = { date,user.getUserId() };
        return Table.execute(PepDBSchema.getInstance().getSchema(),sql,params);
    }

    public static Source getSource(Integer peptideId, Integer groupId) throws SQLException
    {
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoSource();
        SimpleFilter sFilter = new SimpleFilter(FieldKey.fromParts("peptide_group_id"), groupId);
        sFilter.addCondition(FieldKey.fromParts("peptide_id"), peptideId);
        Source dbSrc ;
        Source[] dbSrcs = Table.select(tInfo,tInfo.getColumns(),sFilter,null,Source.class);
        if(dbSrcs != null && dbSrcs.length>0)
            dbSrc = dbSrcs[0];
        else
            dbSrc = null;
        return dbSrc;
    }

    public static PeptidePool[] getChildrenPools(String peptidePoolId) throws SQLException
    {
        SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("parent_pool_id"), Integer.parseInt(peptidePoolId));
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoViewPoolDetails();
        PeptidePool[] pools = Table.select(tInfo,Table.ALL_COLUMNS,sfilter,null,PeptidePool.class);
        if (pools == null || pools.length < 1)
            return null;
        return pools;
    }

    public static Integer[] getPeptidesInPool(Integer peptidePoolId) throws SQLException
    {
        SimpleFilter sfilter = new SimpleFilter(FieldKey.fromParts("peptide_pool_id"), peptidePoolId);
        TableInfo tInfo = PepDBSchema.getInstance().getTableInfoPoolAssignment();
        Integer[] peptideIds = Table.executeArray(tInfo,"peptide_id",sfilter,null,Integer.class);
        if (peptideIds == null || peptideIds.length < 1)
            return null;
        return peptideIds;
    }

}