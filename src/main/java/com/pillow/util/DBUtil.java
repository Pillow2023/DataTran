package com.pillow.util;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Pillow2023
 * @ClassName DBUtil
 * @Description 类的说明
 * @date 2023/3/13
 */
@Slf4j
public class DBUtil {

    public static Object executeSql(String sql,Object... args){
        if(sql==null){
            return null;
        }
        if(sql.startsWith("insert")||sql.startsWith("INSERT")||sql.startsWith("create")||sql.startsWith("CREATE")){
            return insertSql(sql,args);
        }
        if(sql.startsWith("update")||sql.startsWith("UPDATE")||sql.startsWith("alter")||sql.startsWith("ALTER")){
            return updateSql(sql,args);
        }
        if(sql.startsWith("delete")||sql.startsWith("DELETE")||sql.startsWith("drop")||sql.startsWith("DROP")){
            return deleteSql(sql,args);
        }

        return null;
    }

    public static boolean insertSql(String sql,Object... args){
        try{
            return SqlRunner.db().insert(sql,args);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSql(String sql,Object... args){
        try{
            return SqlRunner.db().delete(sql,args);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static List<Map<String,Object>> selectSql(String sql, Object... args){
        try {
            return SqlRunner.db().selectList(sql,args);
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static boolean updateSql(String sql,Object... args){
        try {
            return SqlRunner.db().update(sql,args);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }



}
